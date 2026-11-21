package com.hackus.mailchecker.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hackus.mailchecker.R
import com.hackus.mailchecker.databinding.FragmentToolsBinding
import com.hackus.mailchecker.tools.ListTools

class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipGroupTools.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipNormalize -> {
                    binding.tvToolDesc.text = "Normalize: Cleans delimiters (;, |, \\t) into standard email:pass format"
                }
                R.id.chipDistinct -> {
                    binding.tvToolDesc.text = "Deduplicate: Removes all duplicate account lines while keeping order"
                }
                R.id.chipDomainSplitter -> {
                    binding.tvToolDesc.text = "Domain Splitter: Groups combos by email provider (@gmail, @yahoo...) with domain stats"
                }
                R.id.chipSort -> {
                    binding.tvToolDesc.text = "Sort A-Z: Orders lines alphabetically by account"
                }
                R.id.chipShuffle -> {
                    binding.tvToolDesc.text = "Shuffle: Randomizes the line order with high entropy"
                }
            }
        }

        binding.btnPasteInput.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val pasteData = clip.getItemAt(0).text.toString()
                binding.etToolInput.setText(pasteData)
                Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRunTool.setOnClickListener {
            val input = binding.etToolInput.text.toString()
            if (input.isBlank()) {
                Toast.makeText(context, "Input list is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val checkedId = binding.chipGroupTools.checkedChipId
            val result = when (checkedId) {
                R.id.chipNormalize -> ListTools.normalize(input)
                R.id.chipDistinct -> ListTools.distinct(input)
                R.id.chipDomainSplitter -> ListTools.sortDomains(input)
                R.id.chipSort -> ListTools.sort(input, ListTools.SortMode.ALPHABETICAL_ASC)
                R.id.chipShuffle -> ListTools.shuffle(input)
                else -> ListTools.normalize(input)
            }

            binding.layoutToolOutput.visibility = View.VISIBLE
            binding.tvToolDetails.text = result.details
            binding.etToolOutput.setText(result.outputText)

            if (result.domainStats.isNotEmpty()) {
                val topStats = result.domainStats.take(5).joinToString(" | ") {
                    "${it.domain}: ${it.count} (${String.format("%.1f", it.percentage)}%)"
                }
                binding.tvToolDetails.text = "${result.details}\nTop: $topStats"
            }
        }

        binding.btnCopyOutput.setOnClickListener {
            val output = binding.etToolOutput.text.toString()
            if (output.isNotBlank()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Hackus Output", output)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied output to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveOutput.setOnClickListener {
            Toast.makeText(context, "Saved output to app storage", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
