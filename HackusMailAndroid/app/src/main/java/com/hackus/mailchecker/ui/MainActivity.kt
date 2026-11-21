package com.hackus.mailchecker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.hackus.mailchecker.R
import com.hackus.mailchecker.databinding.ActivityMainBinding
import com.hackus.mailchecker.engine.CheckerEngine
import com.hackus.mailchecker.model.CheckerState
import com.hackus.mailchecker.model.PresetBundles
import com.hackus.mailchecker.model.SearchQuery
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val searchRules = mutableListOf<SearchQuery>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize default search presets (Gaming + Finance)
        PresetBundles.getBundles()["Gaming Platforms"]?.let { searchRules.addAll(it) }
        PresetBundles.getBundles()["Crypto & Finance"]?.let { searchRules.addAll(it) }

        setupViewPager()
        setupBottomNav()
        setupToolbarActions()
        observeEngineState()
    }

    private fun setupViewPager() {
        val fragments: List<Fragment> = listOf(
            DashboardFragment(),
            ViewerFragment(),
            RulesFragment(),
            ToolsFragment(),
            ProxyFragment()
        )

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> binding.viewPager.setCurrentItem(0, true)
                R.id.nav_viewer -> binding.viewPager.setCurrentItem(1, true)
                R.id.nav_rules -> binding.viewPager.setCurrentItem(2, true)
                R.id.nav_tools -> binding.viewPager.setCurrentItem(3, true)
                R.id.nav_proxy -> binding.viewPager.setCurrentItem(4, true)
            }
            true
        }
    }

    private fun setupToolbarActions() {
        binding.btnToolbarExport.setOnClickListener {
            showExportDialog()
        }
    }

    private fun showExportDialog() {
        val options = arrayOf(
            "Share Valid Accounts (TXT)",
            "Share Search Hits (TXT)",
            "Share Full Audit (CSV)",
            "Share Full Report (JSON)"
        )

        androidx.appcompat.app.AlertDialog.Builder(this, com.google.android.material.R.style.Theme_Material3_Dark_Dialog)
            .setTitle("Export & Share Audit Results")
            .setItems(options) { _, which ->
                val content = when (which) {
                    0 -> {
                        val valids = CheckerEngine.getValidAccounts()
                        if (valids.isEmpty()) "No valid accounts yet."
                        else valids.joinToString("\n") { it.combo }
                    }
                    1 -> {
                        val hits = CheckerEngine.getHits()
                        if (hits.isEmpty()) "No search hits yet."
                        else hits.joinToString("\n") { "[${it.queryName}] ${it.accountEmail} | From: ${it.from} | Subj: ${it.subject}" }
                    }
                    2 -> {
                        val all = CheckerEngine.getProcessedAccounts()
                        buildString {
                            append("Email,Password,Status,Domain,Host,Port,Hits\n")
                            all.forEach {
                                append("${it.email},${it.pass},${it.status.name},${it.domain},${it.serverHost},${it.serverPort},${it.searchHits.size}\n")
                            }
                        }
                    }
                    3 -> {
                        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                        val payload = mapOf(
                            "exportDate" to System.currentTimeMillis(),
                            "stats" to CheckerEngine.statsFlow.value,
                            "accounts" to CheckerEngine.getProcessedAccounts(),
                            "hits" to CheckerEngine.getHits()
                        )
                        gson.toJson(payload)
                    }
                    else -> ""
                }

                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, content)
                    putExtra(android.content.Intent.EXTRA_TITLE, "Hackus Mail Export")
                    type = "text/plain"
                }
                startActivity(android.content.Intent.createChooser(sendIntent, "Share Export Via"))
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun observeEngineState() {
        lifecycleScope.launch {
            CheckerEngine.statsFlow.collectLatest { stats ->
                binding.headerStateBadge.text = stats.state.name
                val color = when (stats.state) {
                    CheckerState.RUNNING -> R.color.status_good
                    CheckerState.PAUSED -> R.color.status_2fa
                    CheckerState.FINISHED -> R.color.primary_cyan
                    CheckerState.STOPPED -> R.color.status_bad
                    CheckerState.IDLE -> R.color.text_muted
                }
                binding.headerStateBadge.setTextColor(ContextCompat.getColor(this@MainActivity, color))
            }
        }
    }

    fun getSearchRules(): List<SearchQuery> = searchRules.toList()

    fun updateSearchRules(rules: List<SearchQuery>) {
        searchRules.clear()
        searchRules.addAll(rules)
    }
}
