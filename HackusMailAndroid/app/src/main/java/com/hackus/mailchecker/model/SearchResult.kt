package com.hackus.mailchecker.model

import java.io.Serializable

data class SearchResult(
    val accountEmail: String,
    val queryName: String,
    val folder: String,
    val subject: String,
    val from: String,
    val date: String,
    val snippet: String = "",
    val hasAttachments: Boolean = false,
    val attachmentNames: List<String> = emptyList(),
    val messageId: String = ""
) : Serializable
