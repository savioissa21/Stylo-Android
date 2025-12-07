package com.example.styloandroid.ui.client.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.model.ProviderCardData
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.repository.AuthRepository
import com.example.styloandroid.data.repository.BookingRepository
import com.example.styloandroid.data.repository.HomeRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel : ViewModel() {

    private val authRepo = AuthRepository()
    private val homeRepo = HomeRepository()
    private val bookingRepo = BookingRepository()

    // --- DADOS DO USUÁRIO (COMUM) ---
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _businessName = MutableLiveData<String>()
    val businessName: LiveData<String> = _businessName

    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole

    // --- DASHBOARD (PROFISSIONAL/GESTOR) ---
    private val _todayCount = MutableLiveData<Int>()
    val todayCount: LiveData<Int> = _todayCount

    private val _todayRevenue = MutableLiveData<Double>()
    val todayRevenue: LiveData<Double> = _todayRevenue

    private val _nextAppointment = MutableLiveData<Appointment?>()
    val nextAppointment: LiveData<Appointment?> = _nextAppointment

    // --- BUSCA E FILTROS (CLIENTE) ---
    private var allProvidersList: List<ProviderCardData> = emptyList()

    private val _providers = MutableLiveData<List<ProviderCardData>>()
    val providers: LiveData<List<ProviderCardData>> = _providers

    private val _availableCities = MutableLiveData<List<String>>()
    val availableCities: LiveData<List<String>> = _availableCities

    private val _availableCategories = MutableLiveData<List<String>>()
    val availableCategories: LiveData<List<String>> = _availableCategories

    // Estado dos Filtros
    private var currentQuery = ""
    private var isFavoritesOnly = false
    private var filterCity: String? = null
    private var filterCategory: String? = null
    private var filterMinRating: Float = 0f

    init {
        loadUserData()
        // Não chamamos fetchProviders() ou loadDashboardData() aqui automaticamente
        // pois depende de qual fragmento está ativo (Cliente ou Gestor).
        // Cada fragmento chama sua função de carga no onViewCreated.
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = authRepo.getAppUser()
            _userName.value = user?.name ?: "Usuário"
            _businessName.value = user?.businessName ?: "Stylo"
            _userRole.value = user?.role ?: ""
        }
    }


    fun loadDashboardData() {
        viewModelScope.launch {
            loadUserData() // Garante dados atualizados
            val allAppointments = bookingRepo.getProviderAppointments()
            calculateStats(allAppointments)
        }
    }

    private fun calculateStats(list: List<Appointment>) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        val todayList = list.filter { app ->
            calendar.timeInMillis = app.date
            val appDay = calendar.get(Calendar.DAY_OF_YEAR)
            val appYear = calendar.get(Calendar.YEAR)

            // Inclui pendentes e confirmados (não cancelados)
            appDay == currentDay && appYear == currentYear && app.status != "canceled"
        }

        _todayCount.value = todayList.size
        _todayRevenue.value = todayList.sumOf { it.price }

        // Próximo agendamento (Futuro e não cancelado)
        val next = list.filter { it.date > now && it.status != "canceled" }
            .minByOrNull { it.date }

        _nextAppointment.value = next
    }

    fun fetchProviders() {
        viewModelScope.launch {
            val appUsers = homeRepo.getProfessionalProviders()
            val favoriteIds = homeRepo.getUserFavoriteIds()

            val cards = mutableListOf<ProviderCardData>()
            val citiesSet = mutableSetOf<String>()
            val categoriesSet = mutableSetOf<String>()

            for (user in appUsers) {
                val stats = bookingRepo.getReviewsStats(user.uid)
                val isFav = favoriteIds.contains(user.uid)

                val city = user.businessAddress?.city?.trim() ?: "Outros"
                val category = user.areaOfWork?.trim() ?: "Geral"

                if (city.isNotEmpty()) citiesSet.add(city)
                if (category.isNotEmpty()) categoriesSet.add(category)

                cards.add(
                    ProviderCardData(
                        id = user.uid,
                        businessName = user.businessName ?: user.name ?: "Sem Nome",
                        areaOfWork = category, // Usando areaOfWork como categoria
                        rating = stats.first,
                        reviewCount = stats.second,
                        profileImageUrl = user.photoUrl,
                        isFavorite = isFav,
                        city = city
                    )
                )
            }

            allProvidersList = cards

            // Popula filtros
            _availableCities.value = citiesSet.toList().sorted()
            _availableCategories.value = categoriesSet.toList().sorted()

            applyFilters()
        }
    }

    fun filterByText(query: String) {
        currentQuery = query
        applyFilters()
    }

    fun toggleShowFavoritesOnly(enable: Boolean) {
        isFavoritesOnly = enable
        applyFilters()
    }

    fun applyAdvancedFilters(city: String?, category: String?, minRating: Float) {
        filterCity = city
        filterCategory = category
        filterMinRating = minRating
        applyFilters()
    }

    fun clearAdvancedFilters() {
        filterCity = null
        filterCategory = null
        filterMinRating = 0f
        applyFilters()
    }

    fun toggleFavorite(provider: ProviderCardData) {
        viewModelScope.launch {
            // Otimista
            val updatedList = _providers.value?.map {
                if (it.id == provider.id) it.copy(isFavorite = !it.isFavorite) else it
            } ?: emptyList()
            _providers.value = updatedList

            // Cache
            allProvidersList = allProvidersList.map {
                if (it.id == provider.id) it.copy(isFavorite = !it.isFavorite) else it
            }

            homeRepo.toggleFavorite(provider.id)
            if (isFavoritesOnly) applyFilters()
        }
    }

    private fun applyFilters() {
        var result = allProvidersList

        // 1. Texto
        if (currentQuery.isNotEmpty()) {
            result = result.filter {
                it.businessName.contains(currentQuery, ignoreCase = true) ||
                        it.areaOfWork.contains(currentQuery, ignoreCase = true)
            }
        }

        // 2. Favoritos
        if (isFavoritesOnly) {
            result = result.filter { it.isFavorite }
        }

        // 3. Filtros Avançados
        if (filterCity != null) {
            result = result.filter { it.city.equals(filterCity, ignoreCase = true) }
        }
        if (filterCategory != null) {
            result = result.filter { it.areaOfWork.equals(filterCategory, ignoreCase = true) }
        }
        if (filterMinRating > 0) {
            result = result.filter { it.rating >= filterMinRating }
        }

        _providers.value = result
    }

    fun logout() {
        authRepo.logout()
    }
}