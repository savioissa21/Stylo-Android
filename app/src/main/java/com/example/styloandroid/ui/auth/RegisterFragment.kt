package com.example.styloandroid.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AuthResult
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

        b.rgUserType.setOnCheckedChangeListener { _, checkedId ->
            b.providerFieldsGroup.isVisible = (checkedId == R.id.rbProfissional)
        }

        b.btnRegister.setOnClickListener {
            // Leitura dos dados da UI (Igual ao anterior)
            val name = b.etName.text?.toString().orEmpty()
            val email = b.etEmail.text?.toString().orEmpty()
            val pass = b.etPassword.text?.toString().orEmpty()
            val confirm = b.etConfirm.text?.toString().orEmpty()

            val role = if (b.rbProfissional.isChecked) "profissional" else "cliente"

            var businessName: String? = null
            var areaOfWork: String? = null
            var cnpj: String? = null
            var businessPhone: String? = null
            var socialLinks: SocialLinks? = null
            var paymentMethods: List<String>? = null
            var businessAddress: BusinessAddress? = null

            if (role == "profissional") {
                businessName = b.etBusinessName.text?.toString()
                areaOfWork = b.etAreaOfWork.text?.toString()
                cnpj = b.etCnpj.text?.toString()
                businessPhone = b.etBusinessPhone.text?.toString()
                
                // ... (restante da leitura dos campos do prestador) ...
                 socialLinks = SocialLinks(
                    instagram = b.etInstagram.text?.toString(),
                    facebook = b.etFacebook.text?.toString(),
                    website = b.etWebsite.text?.toString()
                )
                 val methods = mutableListOf<String>()
                if (b.cbPix.isChecked) methods.add("pix")
                if (b.cbCreditCard.isChecked) methods.add("credit_card")
                if (b.cbCash.isChecked) methods.add("cash")
                paymentMethods = methods

                businessAddress = BusinessAddress(
                    zipCode = b.etZipCode.text?.toString().orEmpty(),
                    street = b.etStreet.text?.toString().orEmpty(),
                    number = b.etNumber.text?.toString().orEmpty(),
                    neighborhood = b.etNeighborhood.text?.toString().orEmpty(),
                    city = b.etCity.text?.toString().orEmpty(),
                    state = b.etState.text?.toString().orEmpty()
                )
            }

            val registerData = RegisterViewModel.RegisterData(
                name = name, email = email, pass = pass, confirm = confirm, role = role,
                businessName = businessName, areaOfWork = areaOfWork, cnpj = cnpj,
                businessPhone = businessPhone, socialLinks = socialLinks,
                paymentMethods = paymentMethods, businessAddress = businessAddress
            )
            vm.register(registerData)
        }

        vm.state.observe(viewLifecycleOwner) { res ->
            when (res) {
                is AuthResult.Loading -> b.btnRegister.isEnabled = false
                is AuthResult.Success -> {
                    b.btnRegister.isEnabled = true
                    
                    // Limpa backstack para não voltar ao registro
                    val navOptions = navOptions { popUpTo(R.id.nav_graph) { inclusive = true } }

                    when (res.role) {
                        "GESTOR" -> findNavController().navigate(R.id.homeFragment, null, navOptions)
                        "FUNCIONARIO" -> {
                             Toast.makeText(requireContext(), "Convite aceito! Bem-vindo à equipe.", Toast.LENGTH_LONG).show()
                             findNavController().navigate(R.id.providerAgendaFragment, null, navOptions)
                        }
                        "CLIENTE" -> findNavController().navigate(R.id.clientHomeFragment, null, navOptions)
                        else -> Toast.makeText(requireContext(), "Erro: Role desconhecido ${res.role}", Toast.LENGTH_SHORT).show()
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