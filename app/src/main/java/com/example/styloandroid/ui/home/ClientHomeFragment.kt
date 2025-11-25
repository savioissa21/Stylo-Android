package com.example.styloandroid.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentClientHomeBinding

class ClientHomeFragment : Fragment(R.layout.fragment_client_home) {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var providerAdapter: ProviderAdapter
    private var _binding: FragmentClientHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClientHomeBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Dispara o carregamento (caso o ViewModel não faça no init)
        binding.progressBar.isVisible = true
        homeViewModel.fetchProviders()
    }

    private fun setupRecyclerView() {
        providerAdapter = ProviderAdapter(emptyList()) { provider ->
            // Navegação para Detalhes
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
    }

    private fun setupObservers() {
        // Nome do Cliente
        homeViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvWelcomeClient.text = "Bem-vindo(a), $name"
        }

        // Lista de Prestadores
        homeViewModel.providers.observe(viewLifecycleOwner) { providersList ->
            binding.progressBar.isVisible = false // Esconde loading

            Log.d("ClientHome", "Prestadores recebidos: ${providersList.size}")

            if (providersList.isNullOrEmpty()) {
                binding.tvEmptyState.isVisible = true
                binding.rvProviders.isVisible = false
            } else {
                binding.tvEmptyState.isVisible = false
                binding.rvProviders.isVisible = true
                providerAdapter.updateList(providersList)
            }
        }
    }

    private fun setupListeners() {
        // Busca
        binding.tilSearch.setEndIconOnClickListener {
            val query = binding.etSearch.text.toString()
            Toast.makeText(requireContext(), "Filtro por: $query (A implementar)", Toast.LENGTH_SHORT).show()
            // Dica: Aqui você chamaria homeViewModel.filterProviders(query)
        }

        // Perfil / Logout
        binding.btnProfile.setOnClickListener {
            homeViewModel.logout()
            findNavController().navigate(R.id.action_client_home_to_appointments)        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}