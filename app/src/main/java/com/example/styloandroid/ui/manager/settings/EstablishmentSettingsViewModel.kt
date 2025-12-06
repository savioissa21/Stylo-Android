// Caminho: app/src/main/java/com/example/styloandroid/ui/manager/settings/EstablishmentSettingsViewModel.kt

package com.example.styloandroid.ui.manager.settings

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.model.AppUser
import com.example.styloandroid.data.model.BusinessAddress
import com.example.styloandroid.data.model.SocialLinks
import com.example.styloandroid.data.repository.EstablishmentRepository
import kotlinx.coroutines.launch

class EstablishmentSettingsViewModel : ViewModel() {
    private val repo = EstablishmentRepository()

    private val _currentUser = MutableLiveData<AppUser?>()
    val currentUser: LiveData<AppUser?> = _currentUser

    private val _statusMsg = MutableLiveData<String?>()
    val statusMsg: LiveData<String?> = _statusMsg

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Status específicos para loads de imagem
    private val _isLoadingPhoto = MutableLiveData<Boolean>()
    val isLoadingPhoto: LiveData<Boolean> = _isLoadingPhoto

    private val _isLoadingBanner = MutableLiveData<Boolean>()
    val isLoadingBanner: LiveData<Boolean> = _isLoadingBanner

    fun loadCurrentSettings() {
        viewModelScope.launch {
            _currentUser.value = repo.getMyProfile()
        }
    }

    fun updateProfileImage(uri: Uri) {
        _isLoadingPhoto.value = true
        viewModelScope.launch {
            val url = repo.uploadProfileImage(uri)
            if (url != null) {
                if (repo.updateUserPhotoUrl(url)) {
                    _statusMsg.value = "Foto de perfil atualizada!"
                    loadCurrentSettings()
                } else {
                    _statusMsg.value = "Erro ao salvar URL da foto."
                }
            } else {
                _statusMsg.value = "Erro no upload da imagem."
            }
            _isLoadingPhoto.value = false
        }
    }

    // Atualiza Banner
    fun updateBannerImage(uri: Uri) {
        _isLoadingBanner.value = true
        viewModelScope.launch {
            val url = repo.uploadBannerImage(uri)
            if (url != null) {
                if (repo.updateUserBannerUrl(url)) {
                    _statusMsg.value = "Banner atualizado!"
                    loadCurrentSettings()
                } else {
                    _statusMsg.value = "Erro ao salvar URL do banner."
                }
            } else {
                _statusMsg.value = "Erro no upload do banner."
            }
            _isLoadingBanner.value = false
        }
    }

    // Salva TUDO (Dados + Endereço + Horários)
    fun saveFullProfile(
        businessName: String,
        phone: String,
        street: String,
        number: String,
        neighborhood: String,
        city: String,
        state: String,
        instagram: String,
        facebook: String,
        openTime: String,
        closeTime: String,
        workDays: List<Int>
    ) {
        if (businessName.isBlank()) {
            _statusMsg.value = "O nome do negócio é obrigatório."
            return
        }
        if (workDays.isEmpty()) {
            _statusMsg.value = "Selecione pelo menos um dia de funcionamento."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val address = BusinessAddress(
                street = street,
                number = number,
                neighborhood = neighborhood,
                city = city,
                state = state
            )
            val socials = SocialLinks(
                instagram = instagram,
                facebook = facebook
            )

            val success = repo.updateFullProfile(
                businessName, phone, address, socials, openTime, closeTime, workDays
            )

            if (success) {
                _statusMsg.value = "Perfil atualizado com sucesso!"
                loadCurrentSettings()
            } else {
                _statusMsg.value = "Erro ao salvar alterações."
            }
            _isLoading.value = false
        }
    }

    fun clearStatus() { _statusMsg.value = null }
}