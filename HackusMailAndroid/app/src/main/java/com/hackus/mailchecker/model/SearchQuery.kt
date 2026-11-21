package com.hackus.mailchecker.model

import java.io.Serializable

data class SearchQuery(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var senderFilter: String = "",
    var subjectFilter: String = "",
    var bodyFilter: String = "",
    var dateFrom: String? = null,
    var dateTo: String? = null,
    var searchAllFolders: Boolean = false,
    var targetFolders: List<String> = listOf("INBOX"),
    var requireAttachment: Boolean = false,
    var attachmentExtensions: List<String> = emptyList(),
    var isEnabled: Boolean = true
) : Serializable {
    fun matches(from: String, subject: String, body: String): Boolean {
        if (!isEnabled) return false
        val matchSender = senderFilter.isBlank() || from.contains(senderFilter, ignoreCase = true)
        val matchSubject = subjectFilter.isBlank() || subject.contains(subjectFilter, ignoreCase = true)
        val matchBody = bodyFilter.isBlank() || body.contains(bodyFilter, ignoreCase = true)
        return matchSender && matchSubject && matchBody
    }
}
