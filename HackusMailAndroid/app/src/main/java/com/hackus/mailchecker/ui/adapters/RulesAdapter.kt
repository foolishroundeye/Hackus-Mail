package com.hackus.mailchecker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.hackus.mailchecker.R
import com.hackus.mailchecker.model.SearchQuery

class RulesAdapter(
    private val onDeleteClick: (SearchQuery) -> Unit,
    private val onToggle: (SearchQuery, Boolean) -> Unit
) : RecyclerView.Adapter<RulesAdapter.ViewHolder>() {

    private val items = mutableListOf<SearchQuery>()

    fun setRules(rules: List<SearchQuery>) {
        items.clear()
        items.addAll(rules)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rule = items[position]
        holder.bind(rule, onDeleteClick, onToggle)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvRuleName)
        private val tvCriteria: TextView = itemView.findViewById(R.id.tvRuleCriteria)
        private val switchEnabled: SwitchMaterial = itemView.findViewById(R.id.switchRuleEnabled)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteRule)

        fun bind(
            rule: SearchQuery,
            onDelete: (SearchQuery) -> Unit,
            onToggle: (SearchQuery, Boolean) -> Unit
        ) {
            tvName.text = rule.name
            val criteria = buildString {
                if (rule.senderFilter.isNotBlank()) append("From: ${rule.senderFilter}  ")
                if (rule.subjectFilter.isNotBlank()) append("Subj: ${rule.subjectFilter}  ")
                if (rule.bodyFilter.isNotBlank()) append("Body: ${rule.bodyFilter}")
            }
            tvCriteria.text = criteria.ifEmpty { "Matches all messages" }

            switchEnabled.setOnCheckedChangeListener(null)
            switchEnabled.isChecked = rule.isEnabled
            switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                rule.isEnabled = isChecked
                onToggle(rule, isChecked)
            }

            btnDelete.setOnClickListener { onDelete(rule) }
        }
    }
}
