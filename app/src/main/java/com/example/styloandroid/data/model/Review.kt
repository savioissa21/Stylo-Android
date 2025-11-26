package com.example.styloandroid.data.model

data class Review(
    val id: String = "",
    val appointmentId: String = "",
    val providerId: String = "", // ID do Dono/Estabelecimento
    val employeeId: String = "", // ID de quem atendeu (opcional, mas bom para métricas)
    val clientId: String = "",
    val clientName: String = "",
    val rating: Float = 0f,      // 1.0 a 5.0
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)