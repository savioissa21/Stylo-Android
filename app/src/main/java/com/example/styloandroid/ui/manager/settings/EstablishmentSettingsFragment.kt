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
import com.example.styloandroid.ui.management.EstablishmentSettingsViewModel
import java.util.Calendar
import java.util.Locale

class EstablishmentSettingsFragment : Fragment(R.layout.fragment_establishment_settings) {

    private var _binding: FragmentEstablishmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val vm: EstablishmentSettingsViewModel by viewModels()

    // Lançador para abrir a galeria (Novo padrão Android)
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            vm.updateProfileImage(uri)
        } else {
            Toast.makeText(requireContext(), "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEstablishmentSettingsBinding.bind(view)

        setupTimePickers()
        setupSaveButton()
        setupImagePicker() // Novo

        vm.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Carrega a imagem com Coil
                if (!user.photoUrl.isNullOrEmpty()) {
                    binding.ivProfile.load(user.photoUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.ic_launcher_background)
                        error(R.drawable.ic_launcher_background)
                    }
                }

                // Preenche horários (código existente)
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

        // Loading da imagem
        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressPhoto.isVisible = loading
            binding.btnChangePhoto.isEnabled = !loading
        }

        vm.statusMsg.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                vm.clearStatus()
            }
        }

        vm.loadCurrentSettings()
    }

    private fun setupImagePicker() {
        binding.btnChangePhoto.setOnClickListener {
            // Abre o seletor de fotos (apenas imagens)
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setupTimePickers() {
        binding.btnOpenTime.setOnClickListener {
            showTimePicker(binding.tvOpenTime)
        }
        binding.btnCloseTime.setOnClickListener {
            showTimePicker(binding.tvCloseTime)
        }
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

    private fun setupSaveButton() {
        binding.btnSaveSettings.setOnClickListener {
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

            vm.saveSettings(open, close, days)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}