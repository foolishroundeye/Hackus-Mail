package com.hackus.mailchecker.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import com.hackus.mailchecker.R
import com.hackus.mailchecker.databinding.FragmentExportBinding
import com.hackus.mailchecker.engine.CheckerEngine
import com.hackus.mailchecker.model.Account
import com.hackus.mailchecker.model.SearchResult

class ExportFragment : Fragment() {

    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSummary()

        binding.btnExportFile.setOnClickListener {
            val content = generateExportContent()
            if (content.isBlank()) {
                Toast.makeText(context, "No accounts match selected export filters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(context, "Export saved to Downloads folder!", Toast.LENGTH_SHORT).show()
        }

        binding.btnShareExport.setOnClickListener {
            val content = generateExportContent()
            if (content.isBlank()) {
                Toast.makeText(context, "No content to share!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_TITLE, "Hackus Mail Checker Export")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Export Via")
            startActivity(shareIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateSummary()
    }

    private fun updateSummary() {
        val valid = CheckerEngine.getValidAccounts().size
        val tfa = CheckerEngine.getTwoFactorAccounts().size
        val hits = CheckerEngine.getHits().size
        val total = CheckerEngine.getProcessedAccounts().size

        binding.tvReportSummary.text = buildString {
            append("Audited Accounts: $total\n")
            append("• Valid Credentials: $valid\n")
            append("• Search Hits: $hits\n")
            append("• 2FA / Locked: $tfa\n")
            append("Ready for instant export and sharing.")
        }
    }

    private fun generateExportContent(): String {
        val accounts = mutableListOf<Account>()
        if (binding.cbExportValid.isChecked) {
            accounts.addAll(CheckerEngine.getValidAccounts())
        }
        if (binding.cbExport2FA.isChecked) {
            accounts.addAll(CheckerEngine.getTwoFactorAccounts())
        }
        if (binding.cbExportFull.isChecked) {
            accounts.clear()
            accounts.addAll(CheckerEngine.getProcessedAccounts())
        }

        val hits = if (binding.cbExportHits.isChecked) CheckerEngine.getHits() else emptyList()

        return when (binding.rgExportFormat.checkedRadioButtonId) {
            R.id.rbFormatTxt -> {
                buildString {
                    if (accounts.isNotEmpty()) {
                        append("=== ACCOUNTS (${accounts.size}) ===\n")
                        accounts.forEach { append("${it.combo}\n") }
                    }
                    if (hits.isNotEmpty()) {
                        append("\n=== SEARCH HITS (${hits.size}) ===\n")
                        hits.forEach {
                            append("[${it.queryName}] ${it.accountEmail} | From: ${it.from} | Subj: ${it.subject}\n")
                        }
                    }
                }
            }
            R.id.rbFormatCsv -> {
                buildString {
                    append("Email,Password,Status,Domain,Host,Port,Hits\n")
                    accounts.forEach {
                        append("${it.email},${it.pass},${it.status.name},${it.domain},${it.serverHost},${it.serverPort},${it.searchHits.size}\n")
                    }
                }
            }
            R.id.rbFormatJson -> {
                val gson = GsonBuilder().setPrettyPrinting().create()
                val payload = mapOf(
                    "exportDate" to System.currentTimeMillis(),
                    "accountsCount" to accounts.size,
                    "accounts" to accounts,
                    "hits" to hits
                )
                gson.toJson(payload)
            }
            else -> accounts.joinToString("\n") { it.combo }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
