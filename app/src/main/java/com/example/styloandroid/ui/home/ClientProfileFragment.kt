package com.example.styloandroid.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentClientProfileBinding

class ClientProfileFragment : Fragment(R.layout.fragment_client_profile) {

    private var _binding: FragmentClientProfileBinding? = null
    private val binding get() = _binding!!
    private val vm: ClientProfileViewModel by viewModels()

    // Seletor de Imagem
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Atualiza visualmente
            binding.ivProfile.setImageURI(uri)
            // Avisa ViewModel
            vm.selectImage(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClientProfileBinding.bind(view)

        // Configura Toolbar (se estiver numa activity que não mostra toolbar nativa, senão apenas botão voltar)
        // Aqui assumimos que o usuário pode querer voltar

        setupListeners()
        setupObservers()

        vm.loadProfile()
    }

    private fun setupListeners() {
        binding.btnChangePhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val phone = binding.etPhone.text.toString()
            vm.saveProfile(name, phone)
        }
    }

    private fun setupObservers() {
        vm.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Preenche campos apenas se estiverem vazios (primeira carga)
                // ou força atualização se desejar
                if (binding.etName.text.isNullOrEmpty()) binding.etName.setText(user.name)
                if (binding.etPhone.text.isNullOrEmpty()) binding.etPhone.setText(user.phoneNumber ?: user.businessPhone)
                binding.etEmail.setText(user.email)

                // Carrega foto (apenas se o usuário não tiver selecionado uma nova localmente ainda)
                if (!user.photoUrl.isNullOrEmpty()) {
                    binding.ivProfile.load(user.photoUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.ic_launcher_background)
                        error(R.drawable.ic_launcher_background)
                    }
                }
            }
        }

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressPhoto.isVisible = loading
            binding.btnSave.isEnabled = !loading
            binding.btnSave.text = if(loading) "Salvando..." else "Salvar Alterações"
        }

        vm.statusMsg.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                vm.clearStatus()
                if (msg.contains("sucesso", true)) {
                    // Opcional: Voltar para home após salvar
                    // findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}