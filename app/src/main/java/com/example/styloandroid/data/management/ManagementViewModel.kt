package com.example.styloandroid.ui.management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.management.EstablishmentRepository
import com.example.styloandroid.data.model.Service
import kotlinx.coroutines.launch

class ManagementViewModel : ViewModel() {

    private val repo = EstablishmentRepository()

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    private val _operationStatus = MutableLiveData<String?>()
    val operationStatus: LiveData<String?> = _operationStatus

    fun loadServices() {
        viewModelScope.launch {
            _services.value = repo.getMyServices()
        }
    }

    fun addService(name: String, price: Double, duration: Int) {
        viewModelScope.launch {
            val newService = Service(name = name, price = price, durationMin = duration)
            val success = repo.addService(newService)
            if (success) {
                _operationStatus.value = "Serviço adicionado!"
                loadServices() // Recarrega a lista
            } else {
                _operationStatus.value = "Erro ao adicionar."
            }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            if(repo.deleteService(serviceId)) {
                loadServices()
            }
        }
    }
}