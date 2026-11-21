package com.hackus.mailchecker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.hackus.mailchecker.databinding.FragmentRulesBinding
import com.hackus.mailchecker.model.PresetBundles
import com.hackus.mailchecker.model.SearchQuery
import com.hackus.mailchecker.ui.adapters.RulesAdapter

class RulesFragment : Fragment() {

    private var _binding: FragmentRulesBinding? = null
    private val binding get() = _binding!!
    private lateinit var rulesAdapter: RulesAdapter
    private val rulesList = mutableListOf<SearchQuery>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRulesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rulesAdapter = RulesAdapter(
            onDeleteClick = { rule ->
                rulesList.remove(rule)
                rulesAdapter.setRules(rulesList)
                (activity as? MainActivity)?.updateSearchRules(rulesList)
            },
            onToggle = { _, _ ->
                (activity as? MainActivity)?.updateSearchRules(rulesList)
            }
        )

        binding.rvRules.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rulesAdapter
        }

        setupPresets()
        setupAddCustomRule()

        // Load existing rules from MainActivity
        (activity as? MainActivity)?.let { act ->
            rulesList.clear()
            rulesList.addAll(act.getSearchRules())
            rulesAdapter.setRules(rulesList)
        }
    }

    private fun setupPresets() {
        val bundles = PresetBundles.getBundles()
        for ((bundleName, queries) in bundles) {
            val chip = Chip(requireContext()).apply {
                text = "+ $bundleName"
                isClickable = true
                isCheckable = false
                setOnClickListener {
                    for (q in queries) {
                        if (rulesList.none { it.name.equals(q.name, ignoreCase = true) }) {
                            rulesList.add(q)
                        }
                    }
                    rulesAdapter.setRules(rulesList)
                    (activity as? MainActivity)?.updateSearchRules(rulesList)
                    Toast.makeText(context, "Added $bundleName rules", Toast.LENGTH_SHORT).show()
                }
            }
            binding.chipGroupPresets.addView(chip)
        }
    }

    private fun setupAddCustomRule() {
        binding.btnAddRule.setOnClickListener {
            val name = binding.etRuleName.text?.toString()?.trim() ?: ""
            val sender = binding.etRuleSender.text?.toString()?.trim() ?: ""
            val subject = binding.etRuleSubject.text?.toString()?.trim() ?: ""

            if (name.isBlank()) {
                Toast.makeText(context, "Enter a rule name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newRule = SearchQuery(
                name = name,
                senderFilter = sender,
                subjectFilter = subject
            )

            rulesList.add(newRule)
            rulesAdapter.setRules(rulesList)
            (activity as? MainActivity)?.updateSearchRules(rulesList)

            binding.etRuleName.text?.clear()
            binding.etRuleSender.text?.clear()
            binding.etRuleSubject.text?.clear()
            Toast.makeText(context, "Rule '$name' added", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
