package com.hackus.mailchecker.protocols

import com.hackus.mailchecker.model.*
import com.hackus.mailchecker.proxy.ProxyRotator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.mail.*
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimePart
import javax.mail.search.*

object MailProtocolManager {

    suspend fun checkAccount(
        account: Account,
        serverConfig: ServerConfig,
        queries: List<SearchQuery>,
        timeoutMs: Int = 10000
    ): Account = withContext(Dispatchers.IO) {
        val props = Properties()
        val isSsl = serverConfig.sslType == SslType.SSL_TLS
        val isStartTls = serverConfig.sslType == SslType.STARTTLS

        props["mail.imap.host"] = serverConfig.hostname
        props["mail.imap.port"] = serverConfig.port.toString()
        props["mail.imap.ssl.enable"] = isSsl.toString()
        props["mail.imap.starttls.enable"] = isStartTls.toString()
        props["mail.imap.connectiontimeout"] = timeoutMs.toString()
        props["mail.imap.timeout"] = timeoutMs.toString()
        props["mail.imap.ssl.trust"] = "*"
        props["mail.imap.ssl.checkserveridentity"] = "false"

        // Proxy integration
        val proxy = ProxyRotator.getNextProxy()
        if (proxy != null && proxy.isAlive) {
            when (proxy.type) {
                ProxyType.SOCKS4, ProxyType.SOCKS5 -> {
                    props["mail.imap.socks.host"] = proxy.host
                    props["mail.imap.socks.port"] = proxy.port.toString()
                }
                ProxyType.HTTP -> {
                    props["mail.imap.proxy.host"] = proxy.host
                    props["mail.imap.proxy.port"] = proxy.port.toString()
                    if (proxy.username != null) {
                        props["mail.imap.proxy.user"] = proxy.username
                        props["mail.imap.proxy.password"] = proxy.password ?: ""
                    }
                }
                else -> {}
            }
        }

        val session = Session.getInstance(props)
        var store: Store? = null

        try {
            store = session.getStore(if (isSsl) "imaps" else "imap")
            store.connect(serverConfig.hostname, serverConfig.port, account.email, account.pass)

            account.status = AccountStatus.VALID
            account.serverHost = serverConfig.hostname
            account.serverPort = serverConfig.port
            account.checkedTimestamp = System.currentTimeMillis()

            // Run search queries if valid and queries exist
            if (queries.isNotEmpty()) {
                searchMailbox(store, account, queries)
            }

        } catch (authEx: AuthenticationFailedException) {
            val msg = authEx.message?.lowercase() ?: ""
            if (msg.contains("2fa") || msg.contains("two-factor") || msg.contains("web login") ||
                msg.contains("application-specific") || msg.contains("checkpoint") || msg.contains("challenge")) {
                account.status = AccountStatus.TWO_FACTOR
                account.errorDetails = "2FA or Web Login Required"
            } else {
                account.status = AccountStatus.INVALID
                account.errorDetails = "Invalid email or password"
            }
        } catch (messagingEx: MessagingException) {
            val msg = messagingEx.message?.lowercase() ?: ""
            when {
                msg.contains("timed out") || msg.contains("timeout") -> {
                    account.status = AccountStatus.ERROR
                    account.errorDetails = "Connection timed out ($timeoutMs ms)"
                }
                msg.contains("unknown host") || msg.contains("unresolved") || msg.contains("cannot find") -> {
                    account.status = AccountStatus.HOST_NOT_FOUND
                    account.errorDetails = "Mail host not found: ${serverConfig.hostname}"
                }
                msg.contains("refused") || msg.contains("reset by peer") -> {
                    account.status = AccountStatus.ERROR
                    account.errorDetails = "Connection refused by server"
                }
                msg.contains("blocked") || msg.contains("suspicious") || msg.contains("disabled") -> {
                    account.status = AccountStatus.TWO_FACTOR
                    account.errorDetails = "Account locked or flagged by provider"
                }
                else -> {
                    account.status = AccountStatus.ERROR
                    account.errorDetails = messagingEx.message ?: "Protocol error"
                }
            }
        } catch (ex: Exception) {
            account.status = AccountStatus.ERROR
            account.errorDetails = ex.message ?: "Unexpected error"
        } finally {
            try {
                store?.close()
            } catch (_: Exception) {}
        }

        account
    }

    private fun searchMailbox(store: Store, account: Account, queries: List<SearchQuery>) {
        val enabledQueries = queries.filter { it.isEnabled }
        if (enabledQueries.isEmpty()) return

        val foldersToCheck = mutableListOf("INBOX")
        val anyAllFolder = enabledQueries.any { it.searchAllFolders }
        if (anyAllFolder) {
            try {
                val defaultFolder = store.defaultFolder
                defaultFolder.list("*").forEach { f ->
                    if ((f.type and Folder.HOLDS_MESSAGES) != 0) {
                        foldersToCheck.add(f.fullName)
                    }
                }
            } catch (_: Exception) {}
        }

        val checkedFolderSet = foldersToCheck.distinct()

        for (folderName in checkedFolderSet) {
            var folder: Folder? = null
            try {
                folder = store.getFolder(folderName)
                if (folder == null || !folder.exists()) continue
                folder.open(Folder.READ_ONLY)

                val messageCount = folder.messageCount
                if (messageCount == 0) {
                    folder.close(false)
                    continue
                }

                // Scan recent messages up to 100 per folder to keep checking swift
                val start = (messageCount - 100).coerceAtLeast(1)
                val messages = folder.getMessages(start, messageCount)

                for (msg in messages.reversed()) {
                    val from = msg.from?.joinToString(", ") { it.toString() } ?: ""
                    val subject = msg.subject ?: "(No Subject)"
                    val date = msg.sentDate?.toString() ?: ""

                    for (query in enabledQueries) {
                        if (query.matches(from, subject, "")) {
                            val result = SearchResult(
                                accountEmail = account.email,
                                queryName = query.name,
                                folder = folderName,
                                subject = subject,
                                from = from,
                                date = date,
                                snippet = "Matched query '${query.name}' in $folderName",
                                hasAttachments = msg.contentType?.contains("multipart", ignoreCase = true) == true
                            )
                            account.searchHits.add(result)
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                try {
                    folder?.close(false)
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun fetchFolders(email: String, pass: String, serverConfig: ServerConfig): List<String> = withContext(Dispatchers.IO) {
        val props = Properties().apply {
            put("mail.imap.host", serverConfig.hostname)
            put("mail.imap.port", serverConfig.port.toString())
            put("mail.imap.ssl.enable", (serverConfig.sslType == SslType.SSL_TLS).toString())
            put("mail.imap.connectiontimeout", "8000")
            put("mail.imap.timeout", "8000")
            put("mail.imap.ssl.trust", "*")
        }

        val session = Session.getInstance(props)
        val store = session.getStore(if (serverConfig.sslType == SslType.SSL_TLS) "imaps" else "imap")
        val folderList = mutableListOf<String>()

        try {
            store.connect(serverConfig.hostname, serverConfig.port, email, pass)
            val defaultFolder = store.defaultFolder
            defaultFolder.list("*").forEach { f ->
                if ((f.type and Folder.HOLDS_MESSAGES) != 0) {
                    folderList.add(f.fullName)
                }
            }
            if (folderList.isEmpty()) {
                folderList.add("INBOX")
            }
        } finally {
            try { store.close() } catch (_: Exception) {}
        }

        folderList
    }

    suspend fun fetchMessages(
        email: String,
        pass: String,
        serverConfig: ServerConfig,
        folderName: String = "INBOX",
        limit: Int = 30
    ): List<SearchResult> = withContext(Dispatchers.IO) {
        val props = Properties().apply {
            put("mail.imap.host", serverConfig.hostname)
            put("mail.imap.port", serverConfig.port.toString())
            put("mail.imap.ssl.enable", (serverConfig.sslType == SslType.SSL_TLS).toString())
            put("mail.imap.connectiontimeout", "8000")
            put("mail.imap.timeout", "8000")
            put("mail.imap.ssl.trust", "*")
        }

        val session = Session.getInstance(props)
        val store = session.getStore(if (serverConfig.sslType == SslType.SSL_TLS) "imaps" else "imap")
        val results = mutableListOf<SearchResult>()

        try {
            store.connect(serverConfig.hostname, serverConfig.port, email, pass)
            val folder = store.getFolder(folderName)
            folder.open(Folder.READ_ONLY)

            val count = folder.messageCount
            if (count > 0) {
                val start = (count - limit + 1).coerceAtLeast(1)
                val messages = folder.getMessages(start, count)

                for (msg in messages.reversed()) {
                    val from = msg.from?.joinToString(", ") { it.toString() } ?: "Unknown"
                    val subject = msg.subject ?: "(No Subject)"
                    val date = msg.sentDate?.toString() ?: ""
                    val messageNumber = msg.messageNumber.toString()
                    val hasAttach = msg.contentType?.contains("multipart", ignoreCase = true) == true

                    results.add(
                        SearchResult(
                            accountEmail = email,
                            queryName = "Mailbox Message",
                            folder = folderName,
                            subject = subject,
                            from = from,
                            date = date,
                            snippet = "From: $from | Date: $date",
                            hasAttachments = hasAttach,
                            messageId = messageNumber
                        )
                    )
                }
            }
            folder.close(false)
        } finally {
            try { store.close() } catch (_: Exception) {}
        }

        results
    }
}
