package com.example.styloandroid.data.auth
/**
 * Modelo de dados para o Endereço Comercial (baseado no Step 4 do React)
 */
data class BusinessAddress(
    val zipCode: String = "",
    val street: String = "",
    val number: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val state: String = "",
    val lat: Double? = null, // Latitude
    val lng: Double? = null  // Longitude
)

/**
 * Modelo de dados para Links Sociais (baseado no Step 3 do React)
 */
data class SocialLinks(
    val instagram: String? = null,
    val facebook: String? = null,
    val website: String? = null
)

/**
 * Modelo principal do Usuário, agora com campos de Cliente e Prestador.
 */
data class AppUser(
    // --- Campos de Acesso (Step 1) ---
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "cliente" ou "profissional"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // --- Campos do Prestador (Step 2) ---
    val businessName: String? = null,
    val cnpj: String? = null,
    val businessPhone: String? = null,
    val areaOfWork: String? = null,

    // --- Campos do Prestador (Step 3) ---
    val socialLinks: SocialLinks? = null,
    val paymentMethods: List<String>? = null, // Lista com "pix", "credit_card", "cash"

    // --- Campos do Prestador (Step 4) ---
    val businessAddress: BusinessAddress? = null,

    // --- Outros ---
    val subscriptionStatus: String? = "trial" // Pego do seu código React
)