package com.example.styloandroid.data.model

data class Appointment(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val providerId: String = "",      // Antes podia estar como 'establishmentId'
    val businessName: String = "",    // Este campo pode estar faltando
    val serviceId: String = "",
    val serviceName: String = "",
    val price: Double = 0.0,
    val date: Long = 0L,              // Antes podia estar como 'dateTimestamp'
    val status: String = "pending"
)