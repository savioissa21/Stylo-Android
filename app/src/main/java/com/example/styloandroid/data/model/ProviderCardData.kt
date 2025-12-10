package com.example.styloandroid.data.model

data class ProviderCardData(
    val id: String,
    val businessName: String,
    val areaOfWork: String, 
    val rating: Double,
    val reviewCount: Int,
    val profileImageUrl: String? = null,
    var isFavorite: Boolean = false,
    val city: String = ""
)