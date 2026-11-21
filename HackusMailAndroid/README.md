# Hackus Mail Mobile — Reforged Android Suite

**Hackus Mail Mobile** is the next-generation, high-performance Android mobile application ported and evolved from the desktop *Hackus Mail Checker Reforged*. It combines multi-threaded mailbox auditing, multi-protocol connectivity (IMAP, POP3, Webmail APIs), an interactive mailbox client, deep keyword search with attachment extraction, list processing utilities, and a proxy rotation engine — built with modern Android architecture.

---

## 📱 Pre-Built Installable APK

The compiled Android application package is ready for direct installation (sideloading) on any Android device running Android 7.0+ (API 24 to 34):

- **Location:** [`/workspaces/Hackus-Mail/HackusMailMobile.apk`](file:///workspaces/Hackus-Mail/HackusMailMobile.apk)
- **Size:** 6.7 MB
- **Package Name:** `com.hackus.mailchecker`
- **Target SDK:** Android 14 (API level 34)

---

## 🚀 Key Features ("All This and More")

### 1. Multi-Protocol Mailbox Checking & Auto-Discovery
- **IMAP Support:** Full SSL/TLS (port 993) and STARTTLS/Plain (port 143) with custom connection and read timeouts.
- **POP3 Support:** SSL (port 995) and standard (port 110) with UIDL tracking.
- **Webmail Connectors:** Pre-configured endpoints and 2FA challenge detection for Mail.ru, Yandex, Rambler, ProtonMail, GMX, Web.de, Yahoo, AOL, and Outlook/Office365.
- **Intelligent Host Finder:** Built-in dictionary of **200+ global email providers** with automatic DNS-over-HTTPS (DoH) MX record resolution for custom and corporate domains.

### 2. Deep Search Query & Keyword Matching Engine
- **Search Criteria:** Filter by Sender (`From`), Subject line, and Body keywords / regular expressions.
- **Folder Navigation:** Target specific folders (`INBOX`, `Spam`, `Trash`, `Archive`) or execute full-mailbox recursive scans.
- **Attachment Extraction:** Inspect file extensions (`.pdf`, `.docx`, `.zip`, `.txt`), size thresholds, and attachment filenames.
- **1-Click Preset Bundles:**
  - 🎮 **Gaming Platforms:** Steam, Epic Games, PlayStation Network, Riot Games, Blizzard / Battle.net
  - 💰 **Crypto & Finance:** Binance, Coinbase, Kraken, PayPal, Stripe
  - 🛒 **E-Commerce:** Amazon, Apple Store, eBay
  - 📱 **Social & Streaming:** Netflix, Spotify, Discord
  - 💻 **Cloud & Dev:** AWS, Google Cloud, GitHub

### 3. Built-In Interactive Mailbox Viewer (Client)
- Browse all folders in the connected mailbox.
- View real-time message summaries (sender, subject, date, unread indicator).
- Full message reader with rich HTML rendering, plain text toggle, raw RFC822 headers inspector, and attachment downloader.

### 4. Combo & List Processing Utilities (Tools Suite)
- **Normalize:** Converts non-standard delimiters (`;`, `|`, `\t`, commas) into standardized `email:password`, strips quotes, trims spaces, and lowercases domain names.
- **Distinct (Deduplicate):** High-speed `LinkedHashSet` deduplication that preserves the original entry order.
- **Domain Splitter & Sorter:** Groups combos by domain (e.g. `@gmail.com`, `@yahoo.com`, `@outlook.com`) with instant statistical frequency breakdown (count and percentage).
- **Sorter:** Sorts combos alphabetically (A-Z, Z-A), by domain, or by string length.
- **Shuffle:** High-entropy Fisher-Yates randomization.
- **Merger:** Combines multiple combo lists with optional automatic deduplication.

### 5. Proxy Management & Health Monitoring
- Supports **SOCKS5, SOCKS4, HTTP, and HTTPS** proxies.
- Concurrent ping / latency tester (measures round-trip time in milliseconds).
- Live URL auto-fetcher (fetch proxy pools from external API endpoints).
- Thread-safe proxy rotation (Round-Robin with dead-proxy skipping).
- Direct connection toggle (bypass proxies on demand).

### 6. Android Platform & Mobile-Exclusive Enhancements ("And More")
- **Android Foreground Service (`CheckerForegroundService`):** Ensures continuous background checking even when the screen is off or another app is open, without battery optimization kill.
- **Live Notification Actions:** Real-time CPM, hits counter, and quick "Pause" / "Stop" buttons right from the Android notification shade.
- **Haptic & Sound Alerts:** Notifies the user immediately upon detecting target search hits (e.g. crypto/gaming confirmations).
- **Universal Export:** Export verified accounts, categorized hits, or complete audit logs into **Plain TXT**, **CSV tables**, or formatted **JSON**.
- **Modern Cyber Dark UI:** Styled with Material 3 dark tokens, neon cyan (`#00E5FF`) and purple (`#7C4DFF`) accents, responsive card layouts, and progress meters.

---

## 🛠 Project Structure

```
HackusMailAndroid/
├── app/
│   ├── build.gradle              # App build config, SDK 34, dependencies
│   ├── proguard-rules.pro        # Proguard rules for JavaMail & networking
│   └── src/main/
│       ├── AndroidManifest.xml   # Permissions (Foreground Service, Network, Storage)
│       ├── java/com/hackus/mailchecker/
│       │   ├── engine/           # Multi-threaded Coroutine Engine & Speedometer
│       │   │   └── CheckerEngine.kt
│       │   ├── model/            # Data models, Enums, Stats & PresetBundles
│       │   │   ├── Account.kt
│       │   │   ├── CheckerStats.kt
│       │   │   ├── Enums.kt
│       │   │   ├── PresetBundles.kt
│       │   │   ├── ProxyItem.kt
│       │   │   ├── SearchQuery.kt
│       │   │   ├── SearchResult.kt
│       │   │   └── ServerConfig.kt
│       │   ├── protocols/        # Mail protocol implementations & Host Finder
│       │   │   ├── HostFinder.kt
│       │   │   └── MailProtocolManager.kt
│       │   ├── proxy/            # Proxy pool, latency tester, URL fetcher
│       │   │   └── ProxyRotator.kt
│       │   ├── service/          # Android Foreground Service & notifications
│       │   │   └── CheckerForegroundService.kt
│       │   ├── tools/            # Normalize, Distinct, Domain Splitter, Sort, Shuffle
│       │   │   └── ListTools.kt
│       │   └── ui/               # Activities, Fragments, and RecyclerView Adapters
│       │       ├── MainActivity.kt
│       │       ├── DashboardFragment.kt
│       │       ├── ViewerFragment.kt
│       │       ├── RulesFragment.kt
│       │       ├── ToolsFragment.kt
│       │       ├── ProxyFragment.kt
│       │       ├── ExportFragment.kt
│       │       └── adapters/
│       │           ├── AccountLogAdapter.kt
│       │           ├── MessageAdapter.kt
│       │           ├── ProxyAdapter.kt
│       │           └── RulesAdapter.kt
│       └── res/                  # Material 3 layouts, drawables, colors, themes, menus
├── web-simulator/                # Live interactive browser simulator & APK server
│   ├── index.html
│   └── server.py
├── build.gradle                  # Root build script
├── settings.gradle               # Project repository settings
└── local.properties              # Android SDK location
```

---

## 🔨 Building from Source

To build a fresh release or debug APK from the command line:

```bash
cd /workspaces/Hackus-Mail/HackusMailAndroid
./gradlew assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🌐 Live Interactive Mobile Simulator

For immediate testing in your browser without requiring a connected Android device:

```bash
python3 /workspaces/Hackus-Mail/HackusMailAndroid/web-simulator/server.py
```
Open `http://localhost:8080` in your browser.
