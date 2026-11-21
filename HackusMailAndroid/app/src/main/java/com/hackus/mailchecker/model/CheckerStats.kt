package com.hackus.mailchecker.model

data class CheckerStats(
    var total: Int = 0,
    var checked: Int = 0,
    var valid: Int = 0,
    var invalid: Int = 0,
    var twoFactor: Int = 0,
    var errors: Int = 0,
    var hits: Int = 0,
    var cpm: Int = 0,
    var speedPerSec: Double = 0.0,
    var elapsedMs: Long = 0L,
    var etaSeconds: Long = 0L,
    var state: CheckerState = CheckerState.IDLE
) {
    val progressPercent: Int
        get() = if (total > 0) ((checked.toDouble() / total) * 100).toInt().coerceIn(0, 100) else 0

    val elapsedFormatted: String
        get() {
            val totalSec = elapsedMs / 1000
            val hours = totalSec / 3600
            val minutes = (totalSec % 3600) / 60
            val seconds = totalSec % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

    val etaFormatted: String
        get() {
            if (etaSeconds <= 0 || state != CheckerState.RUNNING) return "--:--:--"
            val hours = etaSeconds / 3600
            val minutes = (etaSeconds % 3600) / 60
            val seconds = etaSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

    fun reset() {
        total = 0
        checked = 0
        valid = 0
        invalid = 0
        twoFactor = 0
        errors = 0
        hits = 0
        cpm = 0
        speedPerSec = 0.0
        elapsedMs = 0L
        etaSeconds = 0L
        state = CheckerState.IDLE
    }
}
