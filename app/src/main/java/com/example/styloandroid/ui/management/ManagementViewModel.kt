package com.example.styloandroid.ui.management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.management.EstablishmentRepository
import com.example.styloandroid.data.model.Service
import kotlinx.coroutines.launch

class ManagementViewModel : ViewModel() {

    private val repo = EstablishmentRepository()

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    // Lista de membros da equipe para popular o checkbox
    private val _teamMembers = MutableLiveData<List<AppUser>>()
    val teamMembers: LiveData<List<AppUser>> = _teamMembers

    private val _operationStatus = MutableLiveData<String?>()
    val operationStatus: LiveData<String?> = _operationStatus

    fun loadServices() {
        viewModelScope.launch {
            _services.value = repo.getMyServices()
        }
    }

    // Carrega a equipe para o Dialog
    fun loadTeamForSelection() {
        viewModelScope.launch {
            _teamMembers.value = repo.getMyTeamMembers()
        }
    }

    fun addService(name: String, price: Double, duration: Int, selectedEmployees: List<String>) {
        viewModelScope.launch {
            val newService = Service(
                name = name, 
                price = price, 
                durationMin = duration,
                employeeIds = selectedEmployees // Salva quem faz o serviço
            )
            
            val success = repo.addService(newService)
            if (success) {
                _operationStatus.value = "Serviço adicionado com sucesso!"
                loadServices() // Recarrega a lista
            } else {
                _operationStatus.value = "Erro ao adicionar serviço."
            }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            if(repo.deleteService(serviceId)) {
                loadServices()
                _operationStatus.value = "Serviço removido."
            }
        }
    }
}