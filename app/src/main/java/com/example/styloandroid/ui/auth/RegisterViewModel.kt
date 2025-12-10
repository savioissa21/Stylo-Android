package com.example.styloandroid.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.auth.AuthResult
import com.example.styloandroid.data.model.BusinessAddress
import com.example.styloandroid.data.model.SocialLinks
import com.example.styloandroid.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableLiveData<AuthResult>()
    val state: LiveData<AuthResult> = _state

    data class RegisterData(
        val name: String,
        val email: String,
        val pass: String,
        val confirm: String,
        val role: String,
        val businessName: String?,
        val areaOfWork: String?,
        val cnpj: String?,
        val businessPhone: String?,
        val socialLinks: SocialLinks?,
        val paymentMethods: List<String>?,
        val businessAddress: BusinessAddress?
    )

    fun register(data: RegisterData) {
        when {
            data.name.isBlank() -> _state.value = AuthResult.Error("Nome obrigatório")
            !Patterns.EMAIL_ADDRESS.matcher(data.email).matches() -> _state.value = AuthResult.Error("E-mail inválido")
            data.pass.length < 6 -> _state.value = AuthResult.Error("Senha mínima 6")
            data.pass != data.confirm -> _state.value = AuthResult.Error("Senhas não conferem")
            data.role.isBlank() -> _state.value = AuthResult.Error("Selecione o tipo de conta")

            data.role == "profissional" -> {
                when {
                    data.businessName.isNullOrBlank() ->
                        _state.value = AuthResult.Error("Nome do negócio é obrigatório")
                    data.areaOfWork.isNullOrBlank() ->
                        _state.value = AuthResult.Error("Área de atuação é obrigatória")
                    data.cnpj.isNullOrBlank() ->
                        _state.value = AuthResult.Error("CNPJ é obrigatório")
                    data.businessPhone.isNullOrBlank() ->
                        _state.value = AuthResult.Error("Telefone do negócio é obrigatório")
                    data.businessAddress == null || data.businessAddress.zipCode.isBlank() ->
                        _state.value = AuthResult.Error("CEP é obrigatório")
                    data.businessAddress.street.isBlank() ->
                        _state.value = AuthResult.Error("Rua é obrigatória")
                    data.businessAddress.number.isBlank() ->
                        _state.value = AuthResult.Error("Número é obrigatório")
                    data.businessAddress.neighborhood.isBlank() ->
                        _state.value = AuthResult.Error("Bairro é obrigatório")
                    data.businessAddress.city.isBlank() ->
                        _state.value = AuthResult.Error("Cidade é obrigatória")
                    data.businessAddress.state.isBlank() || data.businessAddress.state.length != 2 ->
                        _state.value = AuthResult.Error("UF (Estado) inválido")

                    else -> executeRegistration(data)
                }
            }

            data.role == "cliente" -> executeRegistration(data)
        }
    }

    private fun executeRegistration(data: RegisterData) {
        viewModelScope.launch {
            _state.value = AuthResult.Loading
            try {
                val result = repo.register(data)

                _state.value = result

            } catch (e: Exception) {
                _state.value = AuthResult.Error(e.message ?: "Falha no registro")
            }
        }
    }
}