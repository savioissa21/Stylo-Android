package com.example.styloandroid.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.ProviderCardData
import com.example.styloandroid.data.auth.AuthRepository
import com.example.styloandroid.data.auth.HomeRepository
import com.example.styloandroid.data.booking.BookingRepository
import com.example.styloandroid.data.model.Appointment
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel : ViewModel() {

    private val authRepo = AuthRepository()
    private val homeRepo = HomeRepository()
    private val bookingRepo = BookingRepository()

    // Dados do Usuário
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _businessName = MutableLiveData<String>()
    val businessName: LiveData<String> = _businessName

    // NOVO: Cargo do usuário para controle de visibilidade na tela
    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole

    // --- DASHBOARD (PROFISSIONAL) ---
    private val _todayCount = MutableLiveData<Int>()
    val todayCount: LiveData<Int> = _todayCount

    private val _todayRevenue = MutableLiveData<Double>()
    val todayRevenue: LiveData<Double> = _todayRevenue

    private val _nextAppointment = MutableLiveData<Appointment?>()
    val nextAppointment: LiveData<Appointment?> = _nextAppointment

    // --- BUSCA (CLIENTE) ---
    private var allProvidersList: List<ProviderCardData> = emptyList()

    private val _providers = MutableLiveData<List<ProviderCardData>>()
    val providers: LiveData<List<ProviderCardData>> = _providers

    init {
        loadUserData()
        // Se for cliente, busca os prestadores
        fetchProviders()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = authRepo.getAppUser()
            _userName.value = user?.name ?: "Usuário"
            _businessName.value = user?.businessName ?: "Meu Negócio"
            // Salva o role para a UI usar
            _userRole.value = user?.role ?: ""
        }
    }

    // --- LÓGICA DO CLIENTE (BUSCA) ---
    fun fetchProviders() {
        viewModelScope.launch {
            val appUsers = homeRepo.getProfessionalProviders()
            val cards = mutableListOf<ProviderCardData>()

            for (user in appUsers) {
                val stats = bookingRepo.getReviewsStats(user.uid)
                cards.add(
                    ProviderCardData(
                        id = user.uid,
                        businessName = user.businessName ?: user.name ?: "Sem Nome",
                        areaOfWork = user.areaOfWork ?: "Geral",
                        rating = stats.first,
                        reviewCount = stats.second,
                        profileImageUrl = user.photoUrl
                    )
                )
            }

            allProvidersList = cards
            _providers.value = cards
        }
    }

    fun filterProviders(query: String) {
        val filtered = if (query.isEmpty()) {
            allProvidersList
        } else {
            allProvidersList.filter {
                it.businessName.contains(query, ignoreCase = true) ||
                        it.areaOfWork.contains(query, ignoreCase = true)
            }
        }
        _providers.value = filtered
    }

    // --- LÓGICA DO PROFISSIONAL (DASHBOARD) ---
    fun loadDashboardData() {
        viewModelScope.launch {
            loadUserData()
            // O repositório já filtra automaticamente:
            // Se for GESTOR -> Pega tudo do estabelecimento
            // Se for FUNCIONARIO -> Pega tudo onde employeeId == uid
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

            // Inclui pendentes e confirmados (aceitos)
            appDay == currentDay && appYear == currentYear && app.status != "canceled"
        }

        _todayCount.value = todayList.size
        _todayRevenue.value = todayList.sumOf { it.price }

        val next = list.filter { it.date > now && it.status != "canceled" }
            .minByOrNull { it.date }

        _nextAppointment.value = next
    }

    fun logout() {
        authRepo.logout()
    }
}