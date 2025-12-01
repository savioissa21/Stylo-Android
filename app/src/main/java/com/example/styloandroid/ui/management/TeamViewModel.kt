package com.example.styloandroid.ui.management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.management.TeamRepository
import kotlinx.coroutines.launch
import com.example.styloandroid.data.model.Service
import com.example.styloandroid.data.management.EstablishmentRepository

class TeamViewModel : ViewModel() {
    private val repo = TeamRepository()

    private val establishmentRepo = EstablishmentRepository()

    private val _teamList = MutableLiveData<List<AppUser>>()
    val teamList: LiveData<List<AppUser>> = _teamList

    private val _statusMsg = MutableLiveData<String?>()
    val statusMsg: LiveData<String?> = _statusMsg

    private val _servicesList = MutableLiveData<List<Service>>()

    val servicesList: LiveData<List<Service>> = _servicesList

    fun loadServicesForDialog() {
        viewModelScope.launch {
            _servicesList.value = establishmentRepo.getMyServices()
        }
    }

    fun loadTeam() {
        viewModelScope.launch {
            _teamList.value = repo.getMyTeam()
        }
    }

    fun createEmployee(name: String, email: String, pass: String, selectedServiceIds: List<String>) {
        if (name.isBlank() || email.isBlank() || pass.length < 6) {
            _statusMsg.value = "Preencha tudo corretamente. Senha min 6 caracteres."
            return
        }

        viewModelScope.launch {
            val success = repo.createEmployeeAccount(name, email, pass)

            if (success) {
                selectedServiceIds.forEach { serviceId ->
                    val currentServices = establishmentRepo.getMyServices()
                    val service = currentServices.find { it.id == serviceId }

                    if (service != null) {
                        val newEmployeeList = service.employeeIds.toMutableList()
                        newEmployeeList.add(email)
                        establishmentRepo.updateServiceEmployees(service.id, newEmployeeList)
                    }
                }

                _statusMsg.value = "Funcionário criado e vinculado!"
                loadTeam() // Recarrega a lista
            } else {
                _statusMsg.value = "Erro ao criar funcionário."
            }
        }
    }

    fun removeEmployee(employee: AppUser) {
        viewModelScope.launch {
            val success = repo.removeEmployee(employee)
            if (success) {
                _statusMsg.value = "Funcionário removido da equipe."
                loadTeam() // Atualiza lista
            } else {
                _statusMsg.value = "Erro ao remover funcionário."
            }
        }
    }

    fun updatePendingName(employee: AppUser, newName: String) {
        if (employee.uid.isNotEmpty()) {
            _statusMsg.value = "Não é possível editar nome de usuário registrado."
            return
        }
        viewModelScope.launch {
            if(repo.updatePendingEmployeeName(employee.email, newName)) {
                _statusMsg.value = "Nome atualizado!"
                loadTeam()
            }
        }
    }

    fun clearStatus() { _statusMsg.value = null }
}