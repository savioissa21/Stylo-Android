// savioissa21/stylo-android/Stylo-Android-2f9947328211a7c424c8cdf8199117b3fe4515ae/app/src/main/java/com/example/styloandroid/ui/home/HomeViewModel.kt

package com.example.styloandroid.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.ProviderCardData
import com.example.styloandroid.data.auth.AuthRepository
import com.example.styloandroid.data.home.HomeRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    // NOVO: LiveData para o nome do usuário
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _providers = MutableLiveData<List<ProviderCardData>>()
    val providers: LiveData<List<ProviderCardData>> = _providers
    private val homeRepo = HomeRepository()

    init {
        fetchUserName()
        fetchProviders() // Busca os profissionais ao iniciar
    }

    fun fetchProviders() {
        viewModelScope.launch {
            val appUsers = homeRepo.getProfessionalProviders()

            // Mapeia AppUser -> ProviderCardData
            val cards = appUsers.map { user ->
                ProviderCardData(
                    id = user.uid, // Ou user.id se tiver mapeado
                    businessName = user.businessName ?: user.name ?: "Sem Nome",
                    areaOfWork = user.areaOfWork ?: "Serviços Gerais", // Use o campo 'category' do registro
                    rating = 5.0, // Valor padrão por enquanto (futuro: buscar reviews)
                    reviewCount = 0, // Valor padrão
                    profileImageUrl = null // user.photoUrl (se tiver salvo)
                )
            }

            _providers.value = cards
        }
    }

    private fun fetchUserName() {
        viewModelScope.launch {
            val user = repo.getAppUser()
            // Se o usuário e o nome existirem, atualiza o LiveData
            _userName.value = user?.name ?: "Usuário"
        }
    }

    // Simplificando: a função de logout não precisa de LiveData se for só para deslogar
    // e navegar, pois a navegação será chamada no Fragment.
    fun logout() {
        repo.logout()
    }
}