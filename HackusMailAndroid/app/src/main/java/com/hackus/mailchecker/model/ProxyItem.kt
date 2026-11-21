package com.hackus.mailchecker.model

import java.io.Serializable

data class ProxyItem(
    val host: String,
    val port: Int,
    val type: ProxyType = ProxyType.SOCKS5,
    val username: String? = null,
    val password: String? = null,
    var latencyMs: Long = -1L,
    var isAlive: Boolean = true,
    var lastChecked: Long = 0L
) : Serializable {
    val address: String
        get() = "$host:$port"

    val displayString: String
        get() = "[${type.name}] $host:$port ${if (latencyMs > 0) "${latencyMs}ms" else ""}"
}
