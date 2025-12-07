package com.example.styloandroid.data.model

data class Review(
    val id: String = "",
    val appointmentId: String = "",
    val providerId: String = "", 
    val employeeId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val rating: Float = 0f,     
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)