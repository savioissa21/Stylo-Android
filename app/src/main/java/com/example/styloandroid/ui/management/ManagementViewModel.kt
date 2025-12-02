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

    private val _isReadOnly = MutableLiveData<Boolean>()
    val isReadOnly: LiveData<Boolean> = _isReadOnly

    // Lista de membros da equipe para popular o checkbox
    private val _teamMembers = MutableLiveData<List<AppUser>>()
    val teamMembers: LiveData<List<AppUser>> = _teamMembers

    private val _operationStatus = MutableLiveData<String?>()
    val operationStatus: LiveData<String?> = _operationStatus

    fun loadServices() {
        viewModelScope.launch {
            // 1. Pega os dados do usuário atual para saber quem ele é
            val user = repo.getMyProfile()

            if (user != null) {
                if (user.role == "FUNCIONARIO" && !user.establishmentId.isNullOrEmpty()) {
                    // É funcionário: Busca serviços do PATRÃO e bloqueia edição
                    _isReadOnly.value = true
                    _services.value = repo.getServices(user.establishmentId)
                } else {
                    // É Gestor: Busca serviços dele mesmo e libera edição
                    _isReadOnly.value = false
                    _services.value = repo.getServices(user.uid)
                }
            }
        }
    }

    fun loadTeamForSelection() {
        viewModelScope.launch { _teamMembers.value = repo.getMyTeamMembers() }
    }

    fun addService(name: String, price: Double, duration: Int, selectedEmployees: List<String>) {
        viewModelScope.launch {
            val newService = Service(name = name, price = price, durationMin = duration, employeeIds = selectedEmployees)
            if (repo.addService(newService)) {
                _operationStatus.value = "Serviço adicionado!"
                loadServices()
            } else { _operationStatus.value = "Erro." }
        }
    }


    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            if(repo.deleteService(serviceId)) {
                loadServices()
                _operationStatus.value = "Removido."
            }
        }
    }

    fun updateService(service: Service) {
        viewModelScope.launch {
            if (repo.updateService(service)) {
                _operationStatus.value = "Atualizado!"
                loadServices()
            }
        }
    }
    fun updateServiceTeam(serviceId: String, selectedEmployees: List<String>) {
        viewModelScope.launch {
            if (repo.updateServiceEmployees(serviceId, selectedEmployees)) {
                _operationStatus.value = "Equipe atualizada!"
                loadServices()
            }
        }
    }
}