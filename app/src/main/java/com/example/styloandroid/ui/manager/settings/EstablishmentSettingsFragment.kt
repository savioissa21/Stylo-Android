package com.example.styloandroid.ui.manager.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentEstablishmentSettingsBinding
import com.example.styloandroid.ui.manager.settings.EstablishmentSettingsViewModel
import java.util.Calendar
import java.util.Locale

class EstablishmentSettingsFragment : Fragment(R.layout.fragment_establishment_settings) {

    private var _binding: FragmentEstablishmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val vm: EstablishmentSettingsViewModel by viewModels()

    private val pickProfile = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.updateProfileImage(uri)
    }

    private val pickBanner = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.updateBannerImage(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEstablishmentSettingsBinding.bind(view)

        setupListeners()
        setupObservers()

        vm.loadCurrentSettings()
    }

    private fun setupListeners() {
        binding.btnChangePhoto.setOnClickListener {
            pickProfile.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.btnChangeBanner.setOnClickListener {
            pickBanner.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnOpenTime.setOnClickListener { showTimePicker(binding.tvOpenTime) }
        binding.btnCloseTime.setOnClickListener { showTimePicker(binding.tvCloseTime) }

        binding.btnSaveSettings.setOnClickListener {
            saveAllData()
        }
    }

    private fun setupObservers() {
        vm.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                if (!user.photoUrl.isNullOrEmpty()) {
                    binding.ivProfile.load(user.photoUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.ic_launcher_background)
                        error(R.drawable.ic_launcher_background)
                    }
                }
                if (!user.bannerUrl.isNullOrEmpty()) {
                    binding.ivBanner.load(user.bannerUrl) {
                        crossfade(true)
                        placeholder(android.R.color.darker_gray)
                    }
                }

                binding.etBusinessName.setText(user.businessName)
                binding.etBusinessPhone.setText(user.businessPhone)

                user.businessAddress?.let { addr ->
                    binding.etStreet.setText(addr.street)
                    binding.etNumber.setText(addr.number)
                    binding.etNeighborhood.setText(addr.neighborhood)
                    binding.etCity.setText(addr.city)
                    binding.etState.setText(addr.state)
                }

                user.socialLinks?.let { social ->
                    binding.etInstagram.setText(social.instagram)
                    binding.etFacebook.setText(social.facebook)
                }

                binding.tvOpenTime.text = user.openTime ?: "09:00"
                binding.tvCloseTime.text = user.closeTime ?: "20:00"

                val days = user.workDays ?: listOf(2,3,4,5,6,7)
                binding.chipDom.isChecked = days.contains(Calendar.SUNDAY)
                binding.chipSeg.isChecked = days.contains(Calendar.MONDAY)
                binding.chipTer.isChecked = days.contains(Calendar.TUESDAY)
                binding.chipQua.isChecked = days.contains(Calendar.WEDNESDAY)
                binding.chipQui.isChecked = days.contains(Calendar.THURSDAY)
                binding.chipSex.isChecked = days.contains(Calendar.FRIDAY)
                binding.chipSab.isChecked = days.contains(Calendar.SATURDAY)
            }
        }

        vm.isLoadingPhoto.observe(viewLifecycleOwner) { loading ->
            binding.progressPhoto.isVisible = loading
            binding.btnChangePhoto.isEnabled = !loading
        }
        vm.isLoadingBanner.observe(viewLifecycleOwner) { loading ->
            binding.progressBanner.isVisible = loading
            binding.btnChangeBanner.isEnabled = !loading
        }
        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.btnSaveSettings.isEnabled = !loading
            binding.btnSaveSettings.text = if(loading) "Salvando..." else "Salvar Alterações"
        }

        vm.statusMsg.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                vm.clearStatus()
            }
        }
    }

    private fun saveAllData() {
        val name = binding.etBusinessName.text.toString()
        val phone = binding.etBusinessPhone.text.toString()
        
        val street = binding.etStreet.text.toString()
        val num = binding.etNumber.text.toString()
        val neigh = binding.etNeighborhood.text.toString()
        val city = binding.etCity.text.toString()
        val state = binding.etState.text.toString()

        val insta = binding.etInstagram.text.toString()
        val face = binding.etFacebook.text.toString()

        val open = binding.tvOpenTime.text.toString()
        val close = binding.tvCloseTime.text.toString()

        val days = mutableListOf<Int>()
        if (binding.chipDom.isChecked) days.add(Calendar.SUNDAY)
        if (binding.chipSeg.isChecked) days.add(Calendar.MONDAY)
        if (binding.chipTer.isChecked) days.add(Calendar.TUESDAY)
        if (binding.chipQua.isChecked) days.add(Calendar.WEDNESDAY)
        if (binding.chipQui.isChecked) days.add(Calendar.THURSDAY)
        if (binding.chipSex.isChecked) days.add(Calendar.FRIDAY)
        if (binding.chipSab.isChecked) days.add(Calendar.SATURDAY)

        vm.saveFullProfile(
            name, phone, street, num, neigh, city, state, insta, face, open, close, days
        )
    }

    private fun showTimePicker(tv: TextView) {
        val current = tv.text.toString().split(":")
        val h = current[0].toIntOrNull() ?: 9
        val m = current[1].toIntOrNull() ?: 0

        TimePickerDialog(requireContext(), { _, hour, minute ->
            val formatted = String.Companion.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            tv.text = formatted
        }, h, m, true).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}