package com.hackus.mailchecker.engine

import com.hackus.mailchecker.model.*
import com.hackus.mailchecker.protocols.HostFinder
import com.hackus.mailchecker.protocols.MailProtocolManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

object CheckerEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var job: Job? = null
    private var timerJob: Job? = null

    private val accountQueue = ConcurrentLinkedQueue<Account>()
    private val allAccounts = mutableListOf<Account>()
    private val processedAccounts = ConcurrentLinkedQueue<Account>()
    private val hitResults = ConcurrentLinkedQueue<SearchResult>()

    private val _stats = MutableStateFlow(CheckerStats())
    val statsFlow = _stats.asStateFlow()

    private val _accountProcessedFlow = MutableSharedFlow<Account>(replay = 50)
    val accountProcessedFlow = _accountProcessedFlow.asSharedFlow()

    private val _hitDetectedFlow = MutableSharedFlow<SearchResult>(replay = 10)
    val hitDetectedFlow = _hitDetectedFlow.asSharedFlow()

    // Concurrency & settings
    var threadCount: Int = 10
    var timeoutSeconds: Int = 10
    var rebruteLimit: Int = 1
    val ignoreDomains = mutableSetOf<String>()

    // Speed Tracking
    private val checksInCurrentMinute = AtomicInteger(0)
    private var lastMinuteTimestamp = System.currentTimeMillis()
    private var checkStartTimestamp = 0L

    @Volatile
    var isPaused: Boolean = false
        private set

    val currentState: CheckerState
        get() = _stats.value.state

    fun loadCombos(comboText: String): Int {
        stop()
        allAccounts.clear()
        accountQueue.clear()
        processedAccounts.clear()
        hitResults.clear()

        val lines = comboText.lines()
        for (raw in lines) {
            val line = raw.trim().replace('\t', ':').replace(';', ':')
            if (line.isBlank() || line.startsWith("#")) continue
            val parts = line.split(":")
            if (parts.size >= 2) {
                val email = parts[0].trim()
                val pass = parts.subList(1, parts.size).joinToString(":")
                if (email.contains("@")) {
                    val account = Account(email = email, pass = pass)
                    allAccounts.add(account)
                    accountQueue.add(account)
                }
            }
        }

        val s = CheckerStats(
            total = allAccounts.size,
            state = CheckerState.IDLE
        )
        _stats.value = s
        return allAccounts.size
    }

    fun start(queries: List<SearchQuery>) {
        if (allAccounts.isEmpty()) return
        if (currentState == CheckerState.PAUSED) {
            resume()
            return
        }

        job?.cancel()
        timerJob?.cancel()
        isPaused = false

        checkStartTimestamp = System.currentTimeMillis()
        lastMinuteTimestamp = checkStartTimestamp
        checksInCurrentMinute.set(0)

        updateState { it.copy(state = CheckerState.RUNNING) }

        // Start ETA and speed meter updater
        timerJob = scope.launch {
            while (isActive && currentState == CheckerState.RUNNING) {
                delay(1000)
                updateSpeedAndTimer()
            }
        }

        job = scope.launch(Dispatchers.IO) {
            val semaphore = Semaphore(threadCount.coerceIn(1, 100))
            val activeTasks = mutableListOf<Job>()

            while (isActive && accountQueue.isNotEmpty()) {
                if (isPaused) {
                    delay(300)
                    continue
                }

                val account = accountQueue.poll() ?: break

                // Check domain filter
                if (ignoreDomains.contains(account.domain)) {
                    continue
                }

                val task = launch {
                    semaphore.withPermit {
                        if (!isActive) return@withPermit
                        processAccount(account, queries)
                    }
                }
                activeTasks.add(task)
            }

            activeTasks.joinAll()
            if (isActive) {
                updateState { it.copy(state = CheckerState.FINISHED) }
                timerJob?.cancel()
            }
        }
    }

    private suspend fun processAccount(account: Account, queries: List<SearchQuery>) {
        val serverConfig = HostFinder.getServer(account.domain)
        val resultAccount = MailProtocolManager.checkAccount(
            account = account,
            serverConfig = serverConfig,
            queries = queries,
            timeoutMs = timeoutSeconds * 1000
        )

        processedAccounts.add(resultAccount)
        checksInCurrentMinute.incrementAndGet()

        // Update statistics
        updateState { cur ->
            val v = cur.valid + (if (resultAccount.status == AccountStatus.VALID) 1 else 0)
            val inv = cur.invalid + (if (resultAccount.status == AccountStatus.INVALID) 1 else 0)
            val tfa = cur.twoFactor + (if (resultAccount.status == AccountStatus.TWO_FACTOR) 1 else 0)
            val err = cur.errors + (if (resultAccount.status == AccountStatus.ERROR || resultAccount.status == AccountStatus.HOST_NOT_FOUND) 1 else 0)
            val h = cur.hits + resultAccount.searchHits.size

            cur.copy(
                checked = cur.checked + 1,
                valid = v,
                invalid = inv,
                twoFactor = tfa,
                errors = err,
                hits = h
            )
        }

        // Emit to flows
        _accountProcessedFlow.emit(resultAccount)

        if (resultAccount.hasHits) {
            for (hit in resultAccount.searchHits) {
                hitResults.add(hit)
                _hitDetectedFlow.emit(hit)
            }
        }
    }

    private fun updateSpeedAndTimer() {
        val now = System.currentTimeMillis()
        val elapsedSec = ((now - checkStartTimestamp) / 1000).coerceAtLeast(1)
        val stats = _stats.value

        val speed = stats.checked.toDouble() / elapsedSec
        val cpm = (speed * 60).toInt()

        val remaining = (stats.total - stats.checked).coerceAtLeast(0)
        val etaSec = if (speed > 0.05) (remaining / speed).toLong() else 0L

        updateState {
            it.copy(
                elapsedMs = now - checkStartTimestamp,
                speedPerSec = speed,
                cpm = cpm,
                etaSeconds = etaSec
            )
        }
    }

    fun pause() {
        if (currentState == CheckerState.RUNNING) {
            isPaused = true
            updateState { it.copy(state = CheckerState.PAUSED) }
        }
    }

    fun resume() {
        if (currentState == CheckerState.PAUSED) {
            isPaused = false
            updateState { it.copy(state = CheckerState.RUNNING) }
        }
    }

    fun stop() {
        job?.cancel()
        timerJob?.cancel()
        isPaused = false
        updateState { it.copy(state = CheckerState.STOPPED) }
    }

    private inline fun updateState(transform: (CheckerStats) -> CheckerStats) {
        _stats.value = transform(_stats.value)
    }

    fun getAllAccounts(): List<Account> = allAccounts.toList()
    fun getProcessedAccounts(): List<Account> = processedAccounts.toList()
    fun getValidAccounts(): List<Account> = processedAccounts.filter { it.status == AccountStatus.VALID }
    fun getTwoFactorAccounts(): List<Account> = processedAccounts.filter { it.status == AccountStatus.TWO_FACTOR }
    fun getHits(): List<SearchResult> = hitResults.toList()
}
