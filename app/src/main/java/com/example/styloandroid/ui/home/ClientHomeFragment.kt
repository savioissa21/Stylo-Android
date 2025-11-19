package com.example.styloandroid.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.ProviderCardData
import com.example.styloandroid.databinding.FragmentClientHomeBinding

// DADOS DE TESTE (MOCK) - Para garantir que o card apareça!
private val mockProviders = listOf(
    ProviderCardData(
        id = "1",
        businessName = "Barbearia Black",
        areaOfWork = "Barbearia e Design de Cabelo",
        rating = 4.9,
        reviewCount = 350,
        profileImageUrl = null
    ),
    ProviderCardData(
        id = "2",
        businessName = "Salão Fashion Star",
        areaOfWork = "Salão de Beleza e Estética",
        rating = 4.2,
        reviewCount = 85,
        profileImageUrl = null
    ),
    ProviderCardData(
        id = "3",
        businessName = "Nails by Ju",
        areaOfWork = "Manicure e Pedicure",
        rating = 5.0,
        reviewCount = 112,
        profileImageUrl = null
    )
)

class ClientHomeFragment : Fragment(R.layout.fragment_client_home) {

    private val homeViewModel: HomeViewModel by viewModels()

    private var _binding: FragmentClientHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClientHomeBinding.bind(view)

        // 🚨 ONDE TUDO É CHAMADO:
        setupUserInfoObserver()
        setupRecyclerViewWithMocks()
        setupActionListeners()
    }

    private fun setupUserInfoObserver() {
        homeViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvWelcomeClient.text = "Bem-vindo(a), $name!"
        }
    }

    private fun setupRecyclerViewWithMocks() {
        // Log.d("TESTE", "Tamanho da lista: ${mockProviders.size}") // Já verificamos que é 3

        val providerAdapter = ProviderAdapter(mockProviders) { provider ->
            Toast.makeText(requireContext(), "Agendar com ${provider.businessName}", Toast.LENGTH_SHORT).show()
        }

        binding.rvProviders.apply {
            // Este manager garante a rolagem vertical, mesmo que já esteja no XML
            layoutManager = LinearLayoutManager(requireContext())
            adapter = providerAdapter
        }
    }

    private fun setupActionListeners() {
        binding.btnLogoutClient.setOnClickListener {
            homeViewModel.logout()
            Toast.makeText(requireContext(), "Desconectado!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.splashFragment)
        }

        binding.btnProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir menu de Perfil/Configurações", Toast.LENGTH_SHORT).show()
        }

        binding.tilSearch.setEndIconOnClickListener {
            val query = binding.etSearch.text.toString()
            Toast.makeText(requireContext(), "Buscando por: $query", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}