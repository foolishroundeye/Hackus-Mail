package com.hackus.mailchecker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hackus.mailchecker.databinding.FragmentViewerBinding
import com.hackus.mailchecker.model.SearchResult
import com.hackus.mailchecker.protocols.HostFinder
import com.hackus.mailchecker.protocols.MailProtocolManager
import com.hackus.mailchecker.ui.adapters.MessageAdapter
import kotlinx.coroutines.launch

class ViewerFragment : Fragment() {

    private var _binding: FragmentViewerBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageAdapter = MessageAdapter { msg ->
            showMessageDetail(msg)
        }

        binding.rvViewerMessages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
        }

        binding.btnViewerConnect.setOnClickListener {
            connectMailbox()
        }
    }

    private fun connectMailbox() {
        val email = binding.etViewerEmail.text?.toString()?.trim() ?: ""
        val pass = binding.etViewerPassword.text?.toString()?.trim() ?: ""

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(context, "Enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        val domain = email.substringAfter("@", "").lowercase()
        val serverConfig = HostFinder.getServer(domain)

        binding.viewerProgressBar.visibility = View.VISIBLE
        binding.btnViewerConnect.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val folders = MailProtocolManager.fetchFolders(email, pass, serverConfig)
                binding.spinnerFolders.visibility = View.VISIBLE
                val folderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, folders)
                binding.spinnerFolders.adapter = folderAdapter

                binding.spinnerFolders.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                        loadMessages(email, pass, serverConfig, folders[position])
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }

                // Load initial INBOX
                loadMessages(email, pass, serverConfig, "INBOX")

            } catch (ex: Exception) {
                Toast.makeText(context, "Connection failed: ${ex.message}", Toast.LENGTH_LONG).show()
                binding.viewerProgressBar.visibility = View.GONE
                binding.btnViewerConnect.isEnabled = true
            }
        }
    }

    private fun loadMessages(email: String, pass: String, serverConfig: com.hackus.mailchecker.model.ServerConfig, folder: String) {
        binding.viewerProgressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val messages = MailProtocolManager.fetchMessages(email, pass, serverConfig, folder)
                messageAdapter.setMessages(messages)
            } catch (ex: Exception) {
                Toast.makeText(context, "Error fetching folder messages: ${ex.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.viewerProgressBar.visibility = View.GONE
                binding.btnViewerConnect.isEnabled = true
            }
        }
    }

    private fun showMessageDetail(msg: SearchResult) {
        binding.layoutMessageDetail.visibility = View.VISIBLE
        binding.tvDetailSubject.text = msg.subject
        binding.tvDetailFrom.text = "From: ${msg.from}"
        binding.tvDetailDate.text = "Date: ${msg.date}"

        val htmlContent = """
            <html>
            <head><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
            <body style="background-color: #1E293B; color: #F8FAFC; font-family: sans-serif; padding: 12px;">
                <p><strong>Folder:</strong> ${msg.folder}</p>
                <hr style="border: 0; border-top: 1px solid #334155;"/>
                <p>${msg.snippet}</p>
                <p><em>(Full message body parsed securely via IMAP SSL connection)</em></p>
            </body>
            </html>
        """.trimIndent()

        binding.webViewBody.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
