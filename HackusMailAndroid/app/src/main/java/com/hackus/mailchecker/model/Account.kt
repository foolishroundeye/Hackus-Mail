package com.hackus.mailchecker.model

import java.io.Serializable

data class Account(
    val email: String,
    val pass: String,
    val domain: String = email.substringAfter("@", "").lowercase().trim(),
    var status: AccountStatus = AccountStatus.UNCHECKED,
    var protocol: ProtocolType = ProtocolType.IMAP,
    var errorDetails: String = "",
    var serverHost: String = "",
    var serverPort: Int = 993,
    val searchHits: MutableList<SearchResult> = mutableListOf(),
    var checkedTimestamp: Long = 0L
) : Serializable {
    val combo: String
        get() = "$email:$pass"

    val isGood: Boolean
        get() = status == AccountStatus.VALID

    val hasHits: Boolean
        get() = searchHits.isNotEmpty()
}
