package com.example.styloandroid.ui.client.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentClientHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider

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

        // Garante que busca os dados atualizados ao abrir
        homeViewModel.fetchProviders()
    }

    private fun setupUI() {
        // Configura o Adapter com dois callbacks:
        // 1. Clique no Card (Navegar para Detalhes)
        // 2. Clique no Coração (Favoritar)
        providerAdapter = ProviderAdapter(
            providers = emptyList(),
            onClick = { provider ->
                val bundle = Bundle().apply {
                    putString("providerId", provider.id)
                    putString("businessName", provider.businessName)
                }
                findNavController().navigate(R.id.action_client_home_to_detail, bundle)
            },
            onFavoriteClick = { provider ->
                homeViewModel.toggleFavorite(provider)
            }
        )

        binding.rvProviders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = providerAdapter
        }

        // Listener do Chip "Apenas Favoritos"
        binding.chipFavorites.setOnCheckedChangeListener { _, isChecked ->
            homeViewModel.toggleShowFavoritesOnly(isChecked)
            if (isChecked) {
                // Opcional: Limpar texto ao focar nos favoritos
                binding.etSearch.setText("")
            }
        }

        // Listener do Botão de Filtros (Abre o BottomSheet)
        binding.btnOpenFilters.setOnClickListener {
            showFilterBottomSheet()
        }

        // Navegação Inferior (FAB)
        binding.btnMyAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_client_home_to_appointments)
        }

        // Botões do Cabeçalho
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_client_home_to_profile)
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
                homeViewModel.filterByText(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        // Nome do Usuário
        homeViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvWelcomeClient.text = "Bem-vindo(a), $name"
        }

        // Lista de Prestadores
        homeViewModel.providers.observe(viewLifecycleOwner) { list ->
            binding.progressBar.isVisible = false

            if (list.isEmpty()) {
                binding.tvEmptyState.isVisible = true
                binding.rvProviders.isVisible = false

                // Mensagem personalizada dependendo do contexto
                if (binding.chipFavorites.isChecked) {
                    binding.tvEmptyState.text = "Você ainda não tem favoritos."
                } else {
                    binding.tvEmptyState.text = "Nenhum estabelecimento encontrado com esses filtros."
                }
            } else {
                binding.tvEmptyState.isVisible = false
                binding.rvProviders.isVisible = true
                providerAdapter.updateList(list)
            }
        }
    }

    // --- LÓGICA DO BOTTOM SHEET DE FILTROS ---
    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_filters, null)
        dialog.setContentView(view)

        // Referências aos componentes do BottomSheet
        val cgCities = view.findViewById<ChipGroup>(R.id.chipGroupCities)
        val cgCategories = view.findViewById<ChipGroup>(R.id.chipGroupCategories)
        val sliderRating = view.findViewById<Slider>(R.id.sliderRating)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilters)
        val btnClear = view.findViewById<Button>(R.id.btnClearFilters)

        // 1. Popula Cidades Dinamicamente (Vindo do ViewModel)
        homeViewModel.availableCities.value?.forEach { city ->
            val chip = Chip(requireContext()).apply {
                text = city
                isCheckable = true
                isClickable = true
                tag = city
            }
            cgCities.addView(chip)
        }

        // 2. Popula Categorias Dinamicamente
        homeViewModel.availableCategories.value?.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat
                isCheckable = true
                isClickable = true
                tag = cat
            }
            cgCategories.addView(chip)
        }

        // 3. Ação Aplicar
        btnApply.setOnClickListener {
            // Recupera cidade selecionada
            val selectedCityId = cgCities.checkedChipId
            val selectedCity = if (selectedCityId != View.NO_ID) {
                view.findViewById<Chip>(selectedCityId).tag as String
            } else null

            // Recupera categoria selecionada
            val selectedCatId = cgCategories.checkedChipId
            val selectedCategory = if (selectedCatId != View.NO_ID) {
                view.findViewById<Chip>(selectedCatId).tag as String
            } else null

            val minRating = sliderRating.value

            // Envia para o ViewModel aplicar
            homeViewModel.applyAdvancedFilters(selectedCity, selectedCategory, minRating)
            dialog.dismiss()
        }

        // 4. Ação Limpar
        btnClear.setOnClickListener {
            homeViewModel.clearAdvancedFilters()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}