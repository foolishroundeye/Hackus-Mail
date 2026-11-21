package com.hackus.mailchecker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hackus.mailchecker.R
import com.hackus.mailchecker.model.ProxyItem

class ProxyAdapter : RecyclerView.Adapter<ProxyAdapter.ViewHolder>() {

    private val items = mutableListOf<ProxyItem>()

    fun setProxies(proxies: List<ProxyItem>) {
        items.clear()
        items.addAll(proxies)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_proxy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvType: TextView = itemView.findViewById(R.id.tvProxyType)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvProxyAddress)
        private val tvLatency: TextView = itemView.findViewById(R.id.tvProxyLatency)

        fun bind(item: ProxyItem) {
            tvType.text = item.type.name
            tvAddress.text = item.address
            val context = itemView.context

            if (item.latencyMs > 0) {
                tvLatency.text = "${item.latencyMs}ms"
                val color = if (item.latencyMs < 300) R.color.status_good else R.color.status_2fa
                tvLatency.setTextColor(ContextCompat.getColor(context, color))
            } else if (!item.isAlive) {
                tvLatency.text = "DEAD"
                tvLatency.setTextColor(ContextCompat.getColor(context, R.color.status_bad))
            } else {
                tvLatency.text = "UNTESTED"
                tvLatency.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
            }
        }
    }
}
