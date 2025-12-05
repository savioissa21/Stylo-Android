package com.example.styloandroid.data.model

/**
 * Modelo atualizado com campo de Cidade e Categoria para filtros.
 */
data class ProviderCardData(
    val id: String,
    val businessName: String,
    val areaOfWork: String, // Usado como Categoria
    val rating: Double,
    val reviewCount: Int,
    val profileImageUrl: String? = null,
    var isFavorite: Boolean = false,
    val city: String = "" // NOVO CAMPO: Necessário para o filtro de localização
)