package com.hackus.mailchecker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hackus.mailchecker.R
import com.hackus.mailchecker.model.SearchResult

class MessageAdapter(
    private val onMessageClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val items = mutableListOf<SearchResult>()

    fun setMessages(messages: List<SearchResult>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = items[position]
        holder.bind(msg)
        holder.itemView.setOnClickListener { onMessageClick(msg) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView = itemView.findViewById(R.id.tvMailSender)
        private val tvSubject: TextView = itemView.findViewById(R.id.tvMailSubject)
        private val tvDate: TextView = itemView.findViewById(R.id.tvMailDate)

        fun bind(result: SearchResult) {
            tvSender.text = result.from
            tvSubject.text = result.subject
            tvDate.text = result.date
        }
    }
}
