package com.example.styloandroid.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.ProviderCardData
import com.example.styloandroid.data.auth.AuthRepository
// Certifique-se que o pacote do HomeRepository está correto. 
// Se der erro aqui, verifique em qual pacote você criou o HomeRepository.kt
import com.example.styloandroid.data.home.HomeRepository 
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.booking.BookingRepository
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

    // --- DASHBOARD (PROFISSIONAL) ---
    private val _todayCount = MutableLiveData<Int>()
    val todayCount: LiveData<Int> = _todayCount

    private val _todayRevenue = MutableLiveData<Double>()
    val todayRevenue: LiveData<Double> = _todayRevenue

    private val _nextAppointment = MutableLiveData<Appointment?>()
    val nextAppointment: LiveData<Appointment?> = _nextAppointment

    // --- BUSCA (CLIENTE) ---
    // Lista Original (Backup para o filtro)
    private var allProvidersList: List<ProviderCardData> = emptyList()

    // Lista Exibida (Filtrada)
    private val _providers = MutableLiveData<List<ProviderCardData>>()
    val providers: LiveData<List<ProviderCardData>> = _providers

    init {
        loadUserData()
        // Se for cliente, já busca os prestadores
        fetchProviders()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = authRepo.getAppUser()
            _userName.value = user?.name ?: "Usuário"
            _businessName.value = user?.businessName ?: "Meu Negócio"
        }
    }

    // --- LÓGICA DO CLIENTE (BUSCA) ---
    fun fetchProviders() {
        viewModelScope.launch {
            val appUsers = homeRepo.getProfessionalProviders()

            val cards = appUsers.map { user ->
                ProviderCardData(
                    id = user.uid,
                    businessName = user.businessName ?: user.name ?: "Sem Nome",
                    areaOfWork = user.areaOfWork ?: "Geral",
                    rating = 5.0, // Futuro: Implementar média real
                    reviewCount = 0,
                    profileImageUrl = user.photoUrl
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
            val allAppointments = bookingRepo.getProviderAppointments()
            calculateStats(allAppointments)
        }
    }

    private fun calculateStats(list: List<Appointment>) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        // Filtra para HOJE
        val todayList = list.filter { app ->
            calendar.timeInMillis = app.date
            val appDay = calendar.get(Calendar.DAY_OF_YEAR)
            val appYear = calendar.get(Calendar.YEAR)
            
            appDay == currentDay && appYear == currentYear && app.status != "canceled"
        }

        _todayCount.value = todayList.size
        _todayRevenue.value = todayList.sumOf { it.price }

        // Pega o PRÓXIMO agendamento
        val next = list.filter { it.date > now && it.status != "canceled" }
            .minByOrNull { it.date }
        
        _nextAppointment.value = next
    }

    fun logout() {
        authRepo.logout()
    }
}