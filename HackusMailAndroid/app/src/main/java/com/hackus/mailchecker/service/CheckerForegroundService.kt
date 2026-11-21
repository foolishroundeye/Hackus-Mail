package com.hackus.mailchecker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hackus.mailchecker.R
import com.hackus.mailchecker.engine.CheckerEngine
import com.hackus.mailchecker.model.CheckerState
import com.hackus.mailchecker.ui.MainActivity
import kotlinx.coroutines.*

class CheckerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var statsJob: Job? = null
    private var hitsJob: Job? = null

    companion object {
        const val CHANNEL_ID = "mail_checker_service_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, CheckerForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CheckerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, buildNotification("Starting checker...", 0, 100))
                observeEngine()
            }
            ACTION_PAUSE -> {
                CheckerEngine.pause()
            }
            ACTION_RESUME -> {
                CheckerEngine.resume()
            }
            ACTION_STOP -> {
                CheckerEngine.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun observeEngine() {
        statsJob?.cancel()
        hitsJob?.cancel()

        statsJob = serviceScope.launch {
            CheckerEngine.statsFlow.collect { stats ->
                if (stats.state == CheckerState.FINISHED || stats.state == CheckerState.STOPPED) {
                    val finishNotif = buildNotification(
                        "Finished: ${stats.valid} Valid, ${stats.hits} Hits, ${stats.invalid} Invalid",
                        stats.checked,
                        stats.total.coerceAtLeast(1)
                    )
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, finishNotif)
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                } else {
                    val statusText = "Valid: ${stats.valid} | Hits: ${stats.hits} | CPM: ${stats.cpm}"
                    val notification = buildNotification(statusText, stats.checked, stats.total.coerceAtLeast(1))
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, notification)
                }
            }
        }
    }

    private fun buildNotification(contentText: String, progress: Int, max: Int): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseIntent = Intent(this, CheckerForegroundService::class.java).apply {
            action = if (CheckerEngine.isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val pendingPause = PendingIntent.getService(
            this, 1, pauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, CheckerForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseLabel = if (CheckerEngine.isPaused) "Resume" else "Pause"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hackus Mail Mobile")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_mail_viewer)
            .setContentIntent(pendingOpen)
            .setProgress(max, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_pause, pauseLabel, pendingPause)
            .addAction(R.drawable.ic_stop, "Stop", pendingStop)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
