package com.example.styloandroid.data.model

data class Service(
    val id: String = "",
    val name: String = "",         // Ex: "Corte Degrade"
    val description: String = "",  // Ex: "Corte na tesoura e máquina"
    val price: Double = 0.0,       // Ex: 35.00
    val durationMin: Int = 30      // Duração em minutos (importante para a agenda)
)