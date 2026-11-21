package com.hackus.mailchecker.protocols

import com.hackus.mailchecker.model.ProtocolType
import com.hackus.mailchecker.model.ServerConfig
import com.hackus.mailchecker.model.SslType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object HostFinder {
    private val cache = ConcurrentHashMap<String, ServerConfig>()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val KNOWN_HOSTS = mapOf(
        // Google
        "gmail.com" to ServerConfig("gmail.com", "imap.gmail.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "googlemail.com" to ServerConfig("googlemail.com", "imap.gmail.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Microsoft
        "outlook.com" to ServerConfig("outlook.com", "outlook.office365.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "hotmail.com" to ServerConfig("hotmail.com", "outlook.office365.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "live.com" to ServerConfig("live.com", "outlook.office365.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "msn.com" to ServerConfig("msn.com", "outlook.office365.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "windowslive.com" to ServerConfig("windowslive.com", "outlook.office365.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Yahoo & AOL
        "yahoo.com" to ServerConfig("yahoo.com", "imap.mail.yahoo.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "yahoo.co.uk" to ServerConfig("yahoo.co.uk", "imap.mail.yahoo.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "ymail.com" to ServerConfig("ymail.com", "imap.mail.yahoo.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "rocketmail.com" to ServerConfig("rocketmail.com", "imap.mail.yahoo.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "aol.com" to ServerConfig("aol.com", "imap.aol.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Apple
        "icloud.com" to ServerConfig("icloud.com", "imap.mail.me.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "me.com" to ServerConfig("me.com", "imap.mail.me.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "mac.com" to ServerConfig("mac.com", "imap.mail.me.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Mail.ru
        "mail.ru" to ServerConfig("mail.ru", "imap.mail.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "inbox.ru" to ServerConfig("inbox.ru", "imap.mail.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "bk.ru" to ServerConfig("bk.ru", "imap.mail.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "list.ru" to ServerConfig("list.ru", "imap.mail.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "internet.ru" to ServerConfig("internet.ru", "imap.mail.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Yandex
        "yandex.ru" to ServerConfig("yandex.ru", "imap.yandex.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "yandex.com" to ServerConfig("yandex.com", "imap.yandex.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "ya.ru" to ServerConfig("ya.ru", "imap.yandex.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "yandex.by" to ServerConfig("yandex.by", "imap.yandex.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "yandex.kz" to ServerConfig("yandex.kz", "imap.yandex.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "yandex.ua" to ServerConfig("yandex.ua", "imap.yandex.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Rambler
        "rambler.ru" to ServerConfig("rambler.ru", "imap.rambler.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "lenta.ru" to ServerConfig("lenta.ru", "imap.rambler.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "autorambler.ru" to ServerConfig("autorambler.ru", "imap.rambler.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "myrambler.ru" to ServerConfig("myrambler.ru", "imap.rambler.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "ro.ru" to ServerConfig("ro.ru", "imap.rambler.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // United Internet (GMX & Web.de)
        "gmx.com" to ServerConfig("gmx.com", "imap.gmx.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "gmx.net" to ServerConfig("gmx.net", "imap.gmx.net", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "gmx.de" to ServerConfig("gmx.de", "imap.gmx.net", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "web.de" to ServerConfig("web.de", "imap.web.de", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Poland
        "onet.pl" to ServerConfig("onet.pl", "poczta.onet.pl", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "op.pl" to ServerConfig("op.pl", "poczta.onet.pl", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "vp.pl" to ServerConfig("vp.pl", "poczta.onet.pl", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "interia.pl" to ServerConfig("interia.pl", "poczta.interia.pl", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "interia.eu" to ServerConfig("interia.eu", "poczta.interia.pl", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "wp.pl" to ServerConfig("wp.pl", "poczta.wp.pl", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "o2.pl" to ServerConfig("o2.pl", "poczta.o2.pl", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Czech
        "seznam.cz" to ServerConfig("seznam.cz", "imap.seznam.cz", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "email.cz" to ServerConfig("email.cz", "imap.seznam.cz", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "post.cz" to ServerConfig("post.cz", "imap.seznam.cz", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // France
        "orange.fr" to ServerConfig("orange.fr", "imap.orange.fr", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "wanadoo.fr" to ServerConfig("wanadoo.fr", "imap.orange.fr", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "free.fr" to ServerConfig("free.fr", "imap.free.fr", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "sfr.fr" to ServerConfig("sfr.fr", "imap.sfr.fr", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Germany
        "t-online.de" to ServerConfig("t-online.de", "secureimap.t-online.de", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "freenet.de" to ServerConfig("freenet.de", "mx.freenet.de", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Italy
        "libero.it" to ServerConfig("libero.it", "imapmail.libero.it", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "virgilio.it" to ServerConfig("virgilio.it", "in.virgilio.it", 993, ProtocolType.IMAP, SslType.SSL_TLS),

        // Other Global
        "zoho.com" to ServerConfig("zoho.com", "imap.zoho.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "fastmail.com" to ServerConfig("fastmail.com", "imap.fastmail.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "rediffmail.com" to ServerConfig("rediffmail.com", "imap.rediffmail.com", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "comcast.net" to ServerConfig("comcast.net", "imap.comcast.net", 993, ProtocolType.IMAP, SslType.SSL_TLS),
        "att.net" to ServerConfig("att.net", "imap.mail.att.net", 993, ProtocolType.IMAP, SslType.SSL_TLS)
    )

    fun getServer(domain: String): ServerConfig {
        val cleanDomain = domain.lowercase().trim()

        // 1. Check local cache
        cache[cleanDomain]?.let { return it }

        // 2. Check known hosts map
        KNOWN_HOSTS[cleanDomain]?.let {
            cache[cleanDomain] = it
            return it
        }

        // 3. Dynamic MX / DoH Lookup or standard fallback
        val dynamicConfig = resolveDomainHost(cleanDomain)
        cache[cleanDomain] = dynamicConfig
        return dynamicConfig
    }

    private fun resolveDomainHost(domain: String): ServerConfig {
        try {
            // Query Cloudflare DNS-over-HTTPS for MX records
            val url = "https://cloudflare-dns.com/dns-query?name=$domain&type=MX"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/dns-json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                if (json.has("Answer")) {
                    val answers = json.getJSONArray("Answer")
                    if (answers.length() > 0) {
                        val mxData = answers.getJSONObject(0).optString("data", "")
                        val mxHost = mxData.substringAfter(" ").trimEnd('.').lowercase()
                        if (mxHost.isNotBlank()) {
                            // Map MX record to known IMAP servers
                            when {
                                mxHost.contains("google") || mxHost.contains("aspmx") ->
                                    return ServerConfig(domain, "imap.gmail.com", 993, ProtocolType.IMAP, SslType.SSL_TLS)
                                mxHost.contains("outlook") || mxHost.contains("protection.outlook") ->
                                    return ServerConfig(domain, "outlook.office365.com", 993, ProtocolType.IMAP, SslType.SSL_TLS)
                                mxHost.contains("yahoodns") ->
                                    return ServerConfig(domain, "imap.mail.yahoo.com", 993, ProtocolType.IMAP, SslType.SSL_TLS)
                                mxHost.contains("zoho") ->
                                    return ServerConfig(domain, "imap.zoho.com", 993, ProtocolType.IMAP, SslType.SSL_TLS)
                                mxHost.contains("yandex") ->
                                    return ServerConfig(domain, "imap.yandex.com", 993, ProtocolType.IMAP, SslType.SSL_TLS)
                                mxHost.contains("mail.ru") ->
                                    return ServerConfig(domain, "imap.mail.ru", 993, ProtocolType.IMAP, SslType.SSL_TLS)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // DNS lookup failed, fallback to standard imap.<domain>
        }

        // Standard default heuristic
        return ServerConfig(domain, "imap.$domain", 993, ProtocolType.IMAP, SslType.SSL_TLS)
    }
}
