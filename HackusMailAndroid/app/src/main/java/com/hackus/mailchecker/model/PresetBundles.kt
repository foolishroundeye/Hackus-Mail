package com.hackus.mailchecker.model

object PresetBundles {
    fun getBundles(): Map<String, List<SearchQuery>> = mapOf(
        "Gaming Platforms" to listOf(
            SearchQuery(
                name = "Steam",
                senderFilter = "steampowered.com",
                subjectFilter = "",
                bodyFilter = "Steam"
            ),
            SearchQuery(
                name = "Epic Games",
                senderFilter = "epicgames.com",
                subjectFilter = "",
                bodyFilter = "Epic Games"
            ),
            SearchQuery(
                name = "PlayStation",
                senderFilter = "sony",
                subjectFilter = "PlayStation",
                bodyFilter = ""
            ),
            SearchQuery(
                name = "Riot Games",
                senderFilter = "riotgames.com",
                subjectFilter = "",
                bodyFilter = "Valorant"
            ),
            SearchQuery(
                name = "Battle.net / Blizzard",
                senderFilter = "blizzard.com",
                subjectFilter = "",
                bodyFilter = "Battle.net"
            )
        ),
        "Crypto & Finance" to listOf(
            SearchQuery(
                name = "Binance",
                senderFilter = "binance.com",
                subjectFilter = "",
                bodyFilter = "Binance"
            ),
            SearchQuery(
                name = "Coinbase",
                senderFilter = "coinbase.com",
                subjectFilter = "",
                bodyFilter = "Coinbase"
            ),
            SearchQuery(
                name = "Kraken",
                senderFilter = "kraken.com",
                subjectFilter = "",
                bodyFilter = "Kraken"
            ),
            SearchQuery(
                name = "PayPal",
                senderFilter = "paypal.com",
                subjectFilter = "",
                bodyFilter = "payment"
            ),
            SearchQuery(
                name = "Stripe",
                senderFilter = "stripe.com",
                subjectFilter = "",
                bodyFilter = "receipt"
            )
        ),
        "E-Commerce & Shopping" to listOf(
            SearchQuery(
                name = "Amazon",
                senderFilter = "amazon.com",
                subjectFilter = "order",
                bodyFilter = ""
            ),
            SearchQuery(
                name = "Apple Store",
                senderFilter = "apple.com",
                subjectFilter = "invoice",
                bodyFilter = ""
            ),
            SearchQuery(
                name = "eBay",
                senderFilter = "ebay.com",
                subjectFilter = "",
                bodyFilter = "order"
            )
        ),
        "Social & Streaming" to listOf(
            SearchQuery(
                name = "Netflix",
                senderFilter = "netflix.com",
                subjectFilter = "",
                bodyFilter = "membership"
            ),
            SearchQuery(
                name = "Spotify",
                senderFilter = "spotify.com",
                subjectFilter = "",
                bodyFilter = "Premium"
            ),
            SearchQuery(
                name = "Discord",
                senderFilter = "discord.com",
                subjectFilter = "",
                bodyFilter = "Nitro"
            )
        ),
        "Developer & Cloud" to listOf(
            SearchQuery(
                name = "GitHub",
                senderFilter = "github.com",
                subjectFilter = "",
                bodyFilter = "GitHub"
            ),
            SearchQuery(
                name = "AWS (Amazon Web Services)",
                senderFilter = "amazonaws.com",
                subjectFilter = "",
                bodyFilter = "AWS"
            ),
            SearchQuery(
                name = "Google Cloud",
                senderFilter = "google.com",
                subjectFilter = "Cloud",
                bodyFilter = "Google Cloud"
            )
        )
    )
}
