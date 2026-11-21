package com.hackus.mailchecker.proxy

import com.hackus.mailchecker.model.ProxyItem
import com.hackus.mailchecker.model.ProxyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object ProxyRotator {
    private val proxies = CopyOnWriteArrayList<ProxyItem>()
    private val counter = AtomicInteger(0)
    var isEnabled: Boolean = false
    var currentType: ProxyType = ProxyType.DIRECT

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    fun setProxies(list: List<ProxyItem>) {
        proxies.clear()
        proxies.addAll(list)
        counter.set(0)
    }

    fun getProxies(): List<ProxyItem> = proxies.toList()

    fun getCount(): Int = proxies.size

    fun getAliveCount(): Int = proxies.count { it.isAlive }

    fun clear() {
        proxies.clear()
        counter.set(0)
    }

    fun getNextProxy(): ProxyItem? {
        if (!isEnabled || proxies.isEmpty()) return null
        val active = proxies.filter { it.isAlive }
        if (active.isEmpty()) return null
        val index = (counter.getAndIncrement() and Int.MAX_VALUE) % active.size
        return active[index]
    }

    suspend fun testProxy(item: ProxyItem, timeoutMs: Int = 5000): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                val socketAddress = InetSocketAddress(item.host, item.port)
                socket.connect(socketAddress, timeoutMs)
                val latency = System.currentTimeMillis() - start
                item.latencyMs = latency
                item.isAlive = true
                item.lastChecked = System.currentTimeMillis()
                latency
            }
        } catch (_: Exception) {
            item.latencyMs = -1
            item.isAlive = false
            item.lastChecked = System.currentTimeMillis()
            -1L
        }
    }

    suspend fun fetchFromUrl(url: String, defaultType: ProxyType = ProxyType.SOCKS5): List<ProxyItem> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""
        parseProxyList(body, defaultType)
    }

    fun parseProxyList(text: String, defaultType: ProxyType = ProxyType.SOCKS5): List<ProxyItem> {
        val list = mutableListOf<ProxyItem>()
        text.lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isNotBlank() && !line.startsWith("#")) {
                var cleanLine = line
                var type = defaultType

                if (cleanLine.startsWith("socks5://", ignoreCase = true)) {
                    type = ProxyType.SOCKS5
                    cleanLine = cleanLine.substring(9)
                } else if (cleanLine.startsWith("socks4://", ignoreCase = true)) {
                    type = ProxyType.SOCKS4
                    cleanLine = cleanLine.substring(9)
                } else if (cleanLine.startsWith("http://", ignoreCase = true)) {
                    type = ProxyType.HTTP
                    cleanLine = cleanLine.substring(7)
                }

                val parts = cleanLine.split(":")
                if (parts.size >= 2) {
                    val host = parts[0].trim()
                    val port = parts[1].trim().toIntOrNull()
                    if (port != null && port in 1..65535) {
                        val user = if (parts.size >= 4) parts[2].trim() else null
                        val pass = if (parts.size >= 4) parts[3].trim() else null
                        list.add(ProxyItem(host, port, type, user, pass))
                    }
                }
            }
        }
        return list
    }
}
