package com.hackus.mailchecker.model

data class ServerConfig(
    val domain: String,
    val hostname: String,
    val port: Int = 993,
    val protocol: ProtocolType = ProtocolType.IMAP,
    val sslType: SslType = SslType.SSL_TLS,
    val timeoutMs: Int = 10000
)
