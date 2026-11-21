package com.hackus.mailchecker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hackus.mailchecker.R
import com.hackus.mailchecker.databinding.FragmentProxyBinding
import com.hackus.mailchecker.model.ProxyType
import com.hackus.mailchecker.proxy.ProxyRotator
import com.hackus.mailchecker.ui.adapters.ProxyAdapter
import kotlinx.coroutines.launch

class ProxyFragment : Fragment() {

    private var _binding: FragmentProxyBinding? = null
    private val binding get() = _binding!!
    private lateinit var proxyAdapter: ProxyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProxyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        proxyAdapter = ProxyAdapter()
        binding.rvProxies.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = proxyAdapter
        }

        setupControls()
        updateProxyList()
    }

    private fun setupControls() {
        binding.switchProxyEnabled.setOnCheckedChangeListener { _, isChecked ->
            ProxyRotator.isEnabled = isChecked
            Toast.makeText(context, if (isChecked) "Proxy rotation enabled" else "Direct connection mode", Toast.LENGTH_SHORT).show()
        }

        binding.btnLoadProxies.setOnClickListener {
            val text = binding.etProxyInput.text.toString()
            if (text.isNotBlank()) {
                val type = getSelectedProxyType()
                val proxies = ProxyRotator.parseProxyList(text, type)
                ProxyRotator.setProxies(proxies)
                updateProxyList()
                binding.etProxyInput.text?.clear()
                Toast.makeText(context, "Added ${proxies.size} proxies", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnFetchUrl.setOnClickListener {
            val url = binding.etProxyUrl.text?.toString()?.trim() ?: ""
            if (url.startsWith("http", ignoreCase = true)) {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val type = getSelectedProxyType()
                        val proxies = ProxyRotator.fetchFromUrl(url, type)
                        ProxyRotator.setProxies(proxies)
                        updateProxyList()
                        Toast.makeText(context, "Fetched ${proxies.size} proxies from URL", Toast.LENGTH_SHORT).show()
                    } catch (ex: Exception) {
                        Toast.makeText(context, "Fetch failed: ${ex.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Enter a valid HTTP/HTTPS URL", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnTestProxies.setOnClickListener {
            val proxies = ProxyRotator.getProxies()
            if (proxies.isEmpty()) {
                Toast.makeText(context, "No proxies loaded to test!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(context, "Testing proxy ping latencies...", Toast.LENGTH_SHORT).show()
            viewLifecycleOwner.lifecycleScope.launch {
                for (p in proxies) {
                    ProxyRotator.testProxy(p)
                    proxyAdapter.notifyDataSetChanged()
                }
                updateProxyList()
            }
        }
    }

    private fun getSelectedProxyType(): ProxyType {
        return when (binding.rgProxyType.checkedRadioButtonId) {
            R.id.rbSocks5 -> ProxyType.SOCKS5
            R.id.rbSocks4 -> ProxyType.SOCKS4
            R.id.rbHttp -> ProxyType.HTTP
            else -> ProxyType.SOCKS5
        }
    }

    private fun updateProxyList() {
        val proxies = ProxyRotator.getProxies()
        proxyAdapter.setProxies(proxies)
        val alive = ProxyRotator.getAliveCount()
        binding.tvProxyCountHeader.text = "ACTIVE PROXIES (${proxies.size} TOTAL / $alive ALIVE)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
