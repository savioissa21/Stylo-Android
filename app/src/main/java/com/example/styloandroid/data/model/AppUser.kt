package com.example.styloandroid.data.model

/**
 * Modelo de dados para o Endereço Comercial (Para o GESTOR)
 */
data class BusinessAddress(
    val zipCode: String = "",
    val street: String = "",
    val number: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val state: String = "",
    val lat: Double? = null,
    val lng: Double? = null
)

/**
 * Modelo de dados para Links Sociais (Para o GESTOR)
 */
data class SocialLinks(
    val instagram: String? = null,
    val facebook: String? = null,
    val website: String? = null
)

/**
 * Modelo principal do Usuário.
 */
data class AppUser(
    // --- Campos Comuns ---
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val photoUrl: String? = null,

    // NOVO: Telefone pessoal (para Cliente ou Funcionário)
    val phoneNumber: String? = null,

    // --- Campos Específicos do FUNCIONÁRIO ---
    val establishmentId: String? = null,

    // --- Campos Específicos do GESTOR (Dono do Negócio) ---
    val businessName: String? = null,
    val cnpj: String? = null,
    val businessPhone: String? = null, // Telefone comercial
    val areaOfWork: String? = null,
    val socialLinks: SocialLinks? = null,
    val paymentMethods: List<String>? = null,
    val businessAddress: BusinessAddress? = null,
    val subscriptionStatus: String? = "trial",

    // --- CONFIGURAÇÕES DE HORÁRIO E AGENDA ---
    val openTime: String? = "09:00",
    val closeTime: String? = "20:00",
    val lunchStartTime: String? = null,
    val lunchEndTime: String? = null,
    val workDays: List<Int>? = listOf(2,3,4,5,6,7),
    val blockedDates: List<String>? = emptyList()
)