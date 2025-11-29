package com.example.styloandroid.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentClientHomeBinding
// IMPORTANTE: Se HomeViewModel estiver em outro pacote, adicione o import aqui:
// import com.example.styloandroid.viewmodel.home.HomeViewModel (exemplo)

class ClientHomeFragment : Fragment(R.layout.fragment_client_home) {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var providerAdapter: ProviderAdapter
    private var _binding: FragmentClientHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClientHomeBinding.bind(view)

        setupUI()
        setupObservers()
        setupSearchLogic()
        
        // Garante que busca os dados ao abrir
        homeViewModel.fetchProviders()
    }

    private fun setupUI() {
        providerAdapter = ProviderAdapter(emptyList()) { provider ->
            val bundle = Bundle().apply {
                putString("providerId", provider.id)
                putString("businessName", provider.businessName)
            }
            findNavController().navigate(R.id.action_client_home_to_detail, bundle)
        }

        binding.rvProviders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = providerAdapter
        }

        binding.btnMyAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_client_home_to_appointments)
        }

        binding.btnLogout.setOnClickListener {
            homeViewModel.logout()
            findNavController().navigate(R.id.action_client_home_to_login)
        }
    }

    private fun setupSearchLogic() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                homeViewModel.filterProviders(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        homeViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvWelcomeClient.text = "Bem-vindo(a), $name"
        }

        homeViewModel.providers.observe(viewLifecycleOwner) { list ->
            binding.progressBar.isVisible = false
            Log.d("ClientHome", "Atualizando lista: ${list.size} itens")

            if (list.isEmpty()) {
                binding.tvEmptyState.isVisible = true
                binding.rvProviders.isVisible = false
            } else {
                binding.tvEmptyState.isVisible = false
                binding.rvProviders.isVisible = true
                providerAdapter.updateList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}