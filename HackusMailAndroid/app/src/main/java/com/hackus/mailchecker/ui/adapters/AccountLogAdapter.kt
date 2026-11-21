package com.hackus.mailchecker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hackus.mailchecker.R
import com.hackus.mailchecker.model.Account
import com.hackus.mailchecker.model.AccountStatus

class AccountLogAdapter : RecyclerView.Adapter<AccountLogAdapter.ViewHolder>() {

    private val items = mutableListOf<Account>()

    fun addAccount(account: Account) {
        items.add(0, account)
        if (items.size > 200) {
            items.removeAt(items.lastIndex)
        }
        notifyItemInserted(0)
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStatus: TextView = itemView.findViewById(R.id.tvItemStatus)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvItemEmail)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvItemDetails)
        private val tvHits: TextView = itemView.findViewById(R.id.tvItemHits)

        fun bind(account: Account) {
            tvEmail.text = account.combo
            val context = itemView.context

            when (account.status) {
                AccountStatus.VALID -> {
                    tvStatus.text = "VALID"
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_good))
                    tvDetails.text = "${account.serverHost}:${account.serverPort}"
                }
                AccountStatus.INVALID -> {
                    tvStatus.text = "INVALID"
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_bad))
                    tvDetails.text = account.errorDetails.ifEmpty { "Wrong credentials" }
                }
                AccountStatus.TWO_FACTOR -> {
                    tvStatus.text = "2FA"
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_2fa))
                    tvDetails.text = account.errorDetails.ifEmpty { "2FA Challenge" }
                }
                AccountStatus.ERROR -> {
                    tvStatus.text = "ERROR"
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error))
                    tvDetails.text = account.errorDetails.ifEmpty { "Connection error" }
                }
                AccountStatus.HOST_NOT_FOUND -> {
                    tvStatus.text = "NO HOST"
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error))
                    tvDetails.text = "No mail server found for ${account.domain}"
                }
                AccountStatus.UNCHECKED -> {
                    tvStatus.text = "QUEUED"
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                    tvDetails.text = "Pending audit"
                }
            }

            if (account.searchHits.isNotEmpty()) {
                tvHits.visibility = View.VISIBLE
                tvHits.text = "${account.searchHits.size} Hit${if (account.searchHits.size > 1) "s" else ""}"
            } else {
                tvHits.visibility = View.GONE
            }
        }
    }
}
