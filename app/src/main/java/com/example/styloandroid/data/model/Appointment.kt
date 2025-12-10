package com.example.styloandroid.data.model

data class Appointment(
    val id: String = "",
    
    // Cliente
    val clientId: String = "",
    val clientName: String = "",

    // Estabelecimento (Dono/Gestor)
    val providerId: String = "",
    val businessName: String = "",
    
    // Profissional que executará o serviço (Pode ser o Dono ou um Funcionário)
    val employeeId: String = "",      
    val employeeName: String = "",

    // Serviço
    val serviceId: String = "",
    val serviceName: String = "",
    val price: Double = 0.0,
    val durationMin: Int = 30, // Importante salvar a duração para calcular horário de término
    
    // Dados do Agendamento
    val date: Long = 0L,              
    val status: String = "pending", // pending, confirmed, finished, canceled
    val hasReview: Boolean = false  // Para o passo 5
)