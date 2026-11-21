package com.hackus.mailchecker.model

enum class AccountStatus {
    UNCHECKED,
    VALID,
    INVALID,
    TWO_FACTOR,
    ERROR,
    HOST_NOT_FOUND
}

enum class ProtocolType {
    IMAP,
    POP3,
    WEB
}

enum class SslType {
    SSL_TLS,
    STARTTLS,
    PLAIN
}

enum class ProxyType {
    DIRECT,
    HTTP,
    SOCKS4,
    SOCKS5
}

enum class CheckerState {
    IDLE,
    RUNNING,
    PAUSED,
    STOPPED,
    FINISHED
}
