package com.example.styloandroid.data.model

data class Employee(
    val id: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val specialties: List<String> = emptyList() // IDs dos serviços que ele faz
)