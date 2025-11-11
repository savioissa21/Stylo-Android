package com.example.styloandroid.data.auth

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "cliente",     // "cliente" | "profissional" | "admin"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
