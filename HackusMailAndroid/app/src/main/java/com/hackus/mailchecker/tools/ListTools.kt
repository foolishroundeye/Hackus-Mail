package com.hackus.mailchecker.tools

import java.util.*

object ListTools {

    data class DomainStats(
        val domain: String,
        val count: Int,
        val percentage: Double
    )

    data class ToolResult(
        val outputLines: List<String>,
        val originalCount: Int,
        val processedCount: Int,
        val details: String = "",
        val domainStats: List<DomainStats> = emptyList()
    ) {
        val outputText: String
            get() = outputLines.joinToString("\n")
    }

    /**
     * Normalize lines: Converts any delimiter (;, |, \t, comma) to ':',
     * removes quotes, trims spaces, validates email syntax, lowercases domain.
     */
    fun normalize(input: String): ToolResult {
        val lines = input.lines()
        val originalCount = lines.size
        val output = mutableListOf<String>()

        for (rawLine in lines) {
            val line = rawLine.trim().trim('"', '\'')
            if (line.isBlank() || line.startsWith("#")) continue

            // Identify delimiter
            val clean = line.replace('\t', ':').replace(';', ':').replace('|', ':')
            val parts = clean.split(":")
            if (parts.size >= 2) {
                val email = parts[0].trim().lowercase()
                val pass = parts.subList(1, parts.size).joinToString(":") // In case password contains colons

                if (email.contains("@") && email.substringAfter("@").contains(".")) {
                    output.add("$email:$pass")
                }
            }
        }

        return ToolResult(
            outputLines = output,
            originalCount = originalCount,
            processedCount = output.size,
            details = "Normalized ${output.size} valid combos (dropped ${originalCount - output.size} invalid/empty lines)"
        )
    }

    /**
     * Remove duplicate entries while maintaining original order.
     */
    fun distinct(input: String): ToolResult {
        val lines = input.lines().filter { it.isNotBlank() }
        val originalCount = lines.size
        val uniqueSet = LinkedHashSet<String>(lines)
        val output = uniqueSet.toList()
        val duplicatesRemoved = originalCount - output.size

        return ToolResult(
            outputLines = output,
            originalCount = originalCount,
            processedCount = output.size,
            details = "Removed $duplicatesRemoved duplicates (${output.size} unique remaining)"
        )
    }

    /**
     * Sort lines by chosen criteria: Alphabetical, Domain, or Length.
     */
    enum class SortMode {
        ALPHABETICAL_ASC,
        ALPHABETICAL_DESC,
        BY_DOMAIN,
        BY_LENGTH_ASC,
        BY_LENGTH_DESC
    }

    fun sort(input: String, mode: SortMode = SortMode.ALPHABETICAL_ASC): ToolResult {
        val lines = input.lines().filter { it.isNotBlank() }
        val sorted = when (mode) {
            SortMode.ALPHABETICAL_ASC -> lines.sorted()
            SortMode.ALPHABETICAL_DESC -> lines.sortedDescending()
            SortMode.BY_DOMAIN -> lines.sortedBy { line ->
                val email = line.substringBefore(":")
                email.substringAfter("@", "")
            }
            SortMode.BY_LENGTH_ASC -> lines.sortedBy { it.length }
            SortMode.BY_LENGTH_DESC -> lines.sortedByDescending { it.length }
        }

        return ToolResult(
            outputLines = sorted,
            originalCount = lines.size,
            processedCount = sorted.size,
            details = "Sorted ${sorted.size} lines using $mode"
        )
    }

    /**
     * Sort and group by domain, plus calculate domain stats breakdown.
     */
    fun sortDomains(input: String): ToolResult {
        val lines = input.lines().filter { it.isNotBlank() }
        val domainMap = TreeMap<String, MutableList<String>>()

        for (line in lines) {
            val email = line.substringBefore(":")
            val domain = if (email.contains("@")) email.substringAfter("@").lowercase().trim() else "unknown"
            domainMap.getOrPut(domain) { mutableListOf() }.add(line)
        }

        val output = mutableListOf<String>()
        val stats = mutableListOf<DomainStats>()
        val total = lines.size.coerceAtLeast(1)

        for ((domain, domainLines) in domainMap.entries.sortedByDescending { it.value.size }) {
            val count = domainLines.size
            val pct = (count.toDouble() / total) * 100
            stats.add(DomainStats(domain, count, pct))
            output.addAll(domainLines)
        }

        return ToolResult(
            outputLines = output,
            originalCount = lines.size,
            processedCount = output.size,
            details = "Identified ${domainMap.size} distinct domains across ${lines.size} accounts",
            domainStats = stats
        )
    }

    /**
     * Randomize/shuffle line order.
     */
    fun shuffle(input: String): ToolResult {
        val lines = input.lines().filter { it.isNotBlank() }.toMutableList()
        lines.shuffle()

        return ToolResult(
            outputLines = lines,
            originalCount = lines.size,
            processedCount = lines.size,
            details = "Randomized ${lines.size} lines"
        )
    }

    /**
     * Merge multiple lists/files with optional deduplication.
     */
    fun merge(inputs: List<String>, deduplicate: Boolean = true): ToolResult {
        val allLines = mutableListOf<String>()
        for (src in inputs) {
            allLines.addAll(src.lines().filter { it.isNotBlank() })
        }
        val originalCount = allLines.size
        val output = if (deduplicate) LinkedHashSet(allLines).toList() else allLines

        return ToolResult(
            outputLines = output,
            originalCount = originalCount,
            processedCount = output.size,
            details = "Merged ${inputs.size} sources into ${output.size} lines (deduplicate=$deduplicate)"
        )
    }
}
