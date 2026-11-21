package com.hackus.mailchecker.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hackus.mailchecker.R
import com.hackus.mailchecker.databinding.FragmentDashboardBinding
import com.hackus.mailchecker.engine.CheckerEngine
import com.hackus.mailchecker.model.CheckerState
import com.hackus.mailchecker.service.CheckerForegroundService
import com.hackus.mailchecker.ui.adapters.AccountLogAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var logAdapter: AccountLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logAdapter = AccountLogAdapter()
        binding.rvLiveAccounts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = logAdapter
        }

        setupControls()
        observeEngine()
    }

    private fun setupControls() {
        binding.btnLoadCombos.setOnClickListener {
            showLoadCombosDialog()
        }

        binding.btnStart.setOnClickListener {
            if (CheckerEngine.currentState == CheckerState.PAUSED) {
                CheckerEngine.resume()
            } else {
                val queries = (activity as? MainActivity)?.getSearchRules() ?: emptyList()
                val count = CheckerEngine.getAllAccounts().size
                if (count == 0) {
                    Toast.makeText(context, "Please load combos first!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                CheckerEngine.threadCount = binding.sliderThreads.value.toInt()
                CheckerEngine.start(queries)
                context?.let { ctx -> CheckerForegroundService.start(ctx) }
            }
        }

        binding.btnPause.setOnClickListener {
            CheckerEngine.pause()
        }

        binding.btnStop.setOnClickListener {
            CheckerEngine.stop()
            context?.let { ctx -> CheckerForegroundService.stop(ctx) }
        }

        binding.sliderThreads.addOnChangeListener { _, value, _ ->
            val threads = value.toInt()
            binding.tvThreads.text = "Threads: $threads"
            CheckerEngine.threadCount = threads
        }
    }

    private fun showLoadCombosDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Paste combos here (email:password)..."
            isSingleLine = false
            minLines = 8
            maxLines = 15
            textSize = 12f
            setBackgroundResource(R.color.bg_surface_elevated)
            setPadding(24, 24, 24, 24)
            setTextColor(resources.getColor(R.color.text_primary, null))
        }

        AlertDialog.Builder(requireContext(), com.google.android.material.R.style.Theme_Material3_Dark_Dialog)
            .setTitle("Load Account Combos")
            .setView(input)
            .setPositiveButton("Load") { _, _ ->
                val text = input.text.toString()
                if (text.isNotBlank()) {
                    val count = CheckerEngine.loadCombos(text)
                    logAdapter.clear()
                    Toast.makeText(context, "Loaded $count accounts into queue", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeEngine() {
        viewLifecycleOwner.lifecycleScope.launch {
            CheckerEngine.statsFlow.collectLatest { stats ->
                binding.tvCountTotal.text = stats.total.toString()
                binding.tvCountGood.text = stats.valid.toString()
                binding.tvCountBad.text = stats.invalid.toString()
                binding.tvCount2FA.text = stats.twoFactor.toString()
                binding.tvCountErrors.text = stats.errors.toString()
                binding.tvCountHits.text = stats.hits.toString()

                binding.progressBar.progress = stats.progressPercent
                binding.tvProgressPercent.text = "${stats.progressPercent}%"
                binding.tvProgressCount.text = "${stats.checked} / ${stats.total}"

                binding.tvCPM.text = stats.cpm.toString()
                binding.tvElapsed.text = stats.elapsedFormatted
                binding.tvETA.text = stats.etaFormatted

                when (stats.state) {
                    CheckerState.RUNNING -> {
                        binding.btnStart.isEnabled = false
                        binding.btnPause.isEnabled = true
                        binding.btnStop.isEnabled = true
                        binding.btnLoadCombos.isEnabled = false
                    }
                    CheckerState.PAUSED -> {
                        binding.btnStart.isEnabled = true
                        binding.btnStart.text = "RESUME"
                        binding.btnPause.isEnabled = false
                        binding.btnStop.isEnabled = true
                    }
                    else -> {
                        binding.btnStart.isEnabled = true
                        binding.btnStart.text = getString(R.string.btn_start)
                        binding.btnPause.isEnabled = false
                        binding.btnStop.isEnabled = false
                        binding.btnLoadCombos.isEnabled = true
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            CheckerEngine.accountProcessedFlow.collect { account ->
                logAdapter.addAccount(account)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
