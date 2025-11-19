package com.example.styloandroid.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible // Importe isso
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AuthResult
// Importe os novos modelos de dados
import com.example.styloandroid.data.auth.BusinessAddress
import com.example.styloandroid.data.auth.SocialLinks
import com.example.styloandroid.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private var _b: FragmentRegisterBinding? = null
    private val b get() = _b!!
    private val vm: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentRegisterBinding.bind(view)

        // --- Mostra/Esconde os campos do Prestador ---
        b.rgUserType.setOnCheckedChangeListener { _, checkedId ->
            b.providerFieldsGroup.isVisible = (checkedId == R.id.rbProfissional)
        }

        b.btnRegister.setOnClickListener {
            // --- Lê os dados do Step 1 (Acesso) ---
            val name = b.etName.text?.toString().orEmpty()
            val email = b.etEmail.text?.toString().orEmpty()
            val pass = b.etPassword.text?.toString().orEmpty()
            val confirm = b.etConfirm.text?.toString().orEmpty()

            val selectedRoleId = b.rgUserType.checkedRadioButtonId
            val role = when (selectedRoleId) {
                R.id.rbCliente -> "cliente"
                R.id.rbProfissional -> "profissional"
                else -> ""
            }

            // Prepara as variáveis do Prestador
            var businessName: String? = null
            var areaOfWork: String? = null
            var cnpj: String? = null
            var businessPhone: String? = null
            var socialLinks: SocialLinks? = null
            var paymentMethods: List<String>? = null
            var businessAddress: BusinessAddress? = null

            // --- Se for profissional, lê todos os outros campos ---
            if (role == "profissional") {
                // --- Lê dados do Step 2 (Negócio) ---
                businessName = b.etBusinessName.text?.toString()
                areaOfWork = b.etAreaOfWork.text?.toString()
                cnpj = b.etCnpj.text?.toString()
                businessPhone = b.etBusinessPhone.text?.toString()

                // --- Lê dados do Step 3 (Divulgação) ---
                socialLinks = SocialLinks(
                    instagram = b.etInstagram.text?.toString().takeIf { !it.isNullOrBlank() },
                    facebook = b.etFacebook.text?.toString().takeIf { !it.isNullOrBlank() },
                    website = b.etWebsite.text?.toString().takeIf { !it.isNullOrBlank() }
                )

                val methods = mutableListOf<String>()
                if (b.cbPix.isChecked) methods.add("pix")
                if (b.cbCreditCard.isChecked) methods.add("credit_card")
                if (b.cbCash.isChecked) methods.add("cash")
                paymentMethods = methods

                // --- Lê dados do Step 4 (Endereço) ---
                businessAddress = BusinessAddress(
                    zipCode = b.etZipCode.text?.toString().orEmpty(),
                    street = b.etStreet.text?.toString().orEmpty(),
                    number = b.etNumber.text?.toString().orEmpty(),
                    neighborhood = b.etNeighborhood.text?.toString().orEmpty(),
                    city = b.etCity.text?.toString().orEmpty(),
                    state = b.etState.text?.toString().orEmpty()
                )
            }

            // Cria um objeto de dados para limpeza
            val registerData = RegisterViewModel.RegisterData(
                name = name,
                email = email,
                pass = pass,
                confirm = confirm,
                role = role,
                businessName = businessName,
                areaOfWork = areaOfWork,
                cnpj = cnpj,
                businessPhone = businessPhone,
                socialLinks = socialLinks,
                paymentMethods = paymentMethods,
                businessAddress = businessAddress
            )

            // Envia o objeto para o ViewModel
            vm.register(registerData)
        }

        vm.state.observe(viewLifecycleOwner) { res ->
            when (res) {
                is AuthResult.Loading -> b.btnRegister.isEnabled = false
                is AuthResult.Success -> {
                    b.btnRegister.isEnabled = true
                    // Navega para a home (limpando a pilha de login/registro)
                    // ✨ CORREÇÃO AQUI: Verificar o role para navegar corretamente
                    when (res.role) {
                        "profissional" -> {
                            // Redireciona para a Home do Prestador
                            findNavController().navigate(R.id.action_register_to_home)
                        }
                        "cliente" -> {
                            // Redireciona para a Home do Cliente (fragment_client_home)
                            // A rota 'action_register_to_client_home' não está no seu nav_graph,
                            // mas vamos usar a ação que navega para a Home Profissional temporariamente,
                            // pois o LoginFragment usa o destino 'clientHomeFragment'.
                            // O ideal é adicionar a action correta no nav_graph.

                            // 💡 ALTERNATIVA TEMPORÁRIA: Navegar para o destino final (clientHomeFragment)
                            // e limpar o back stack, como o LoginFragment faz.
                            findNavController().navigate(R.id.clientHomeFragment, null,
                                // Limpa o back stack para que o usuário não volte para o registro
                                navOptions { popUpTo(R.id.nav_graph) { inclusive = true } }
                            )

                        }
                        else -> {
                            Toast.makeText(requireContext(), "Erro: Tipo de usuário inválido.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is AuthResult.Error -> {
                    b.btnRegister.isEnabled = true
                    Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}