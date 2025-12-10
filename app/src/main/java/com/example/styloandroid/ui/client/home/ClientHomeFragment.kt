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

        homeViewModel.fetchProviders()
    }

    private fun setupUI() {
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

        binding.chipFavorites.setOnCheckedChangeListener { _, isChecked ->
            homeViewModel.toggleShowFavoritesOnly(isChecked)
            if (isChecked) {
                binding.etSearch.setText("")
            }
        }

        binding.btnOpenFilters.setOnClickListener {
            showFilterBottomSheet()
        }

        binding.btnMyAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_client_home_to_appointments)
        }

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
        homeViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvWelcomeClient.text = "Bem-vindo(a), $name"
        }

        homeViewModel.providers.observe(viewLifecycleOwner) { list ->
            binding.progressBar.isVisible = false

            if (list.isEmpty()) {
                binding.tvEmptyState.isVisible = true
                binding.rvProviders.isVisible = false

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

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_filters, null)
        dialog.setContentView(view)

        val cgCities = view.findViewById<ChipGroup>(R.id.chipGroupCities)
        val cgCategories = view.findViewById<ChipGroup>(R.id.chipGroupCategories)
        val sliderRating = view.findViewById<Slider>(R.id.sliderRating)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilters)
        val btnClear = view.findViewById<Button>(R.id.btnClearFilters)

        homeViewModel.availableCities.value?.forEach { city ->
            val chip = Chip(requireContext()).apply {
                text = city
                isCheckable = true
                isClickable = true
                tag = city
            }
            cgCities.addView(chip)
        }

        homeViewModel.availableCategories.value?.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat
                isCheckable = true
                isClickable = true
                tag = cat
            }
            cgCategories.addView(chip)
        }

        btnApply.setOnClickListener {
            val selectedCityId = cgCities.checkedChipId
            val selectedCity = if (selectedCityId != View.NO_ID) {
                view.findViewById<Chip>(selectedCityId).tag as String
            } else null

            val selectedCatId = cgCategories.checkedChipId
            val selectedCategory = if (selectedCatId != View.NO_ID) {
                view.findViewById<Chip>(selectedCatId).tag as String
            } else null

            val minRating = sliderRating.value

            homeViewModel.applyAdvancedFilters(selectedCity, selectedCategory, minRating)
            dialog.dismiss()
        }

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