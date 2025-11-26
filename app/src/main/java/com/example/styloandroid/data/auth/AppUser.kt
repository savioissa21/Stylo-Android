package com.example.styloandroid.data.auth

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
 *
 * ROLES (Tipos de Usuário):
 * 1. "CLIENTE": Usuário comum que agenda serviços.
 * 2. "GESTOR": Dono do estabelecimento (antigo "profissional"). Tem acesso total.
 * 3. "FUNCIONARIO": Prestador que trabalha para um Gestor. Vê apenas sua agenda.
 */
data class AppUser(
    // --- Campos Comuns (Todos os usuários) ---
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    
    /**
     * Define o tipo de acesso: "CLIENTE", "GESTOR" ou "FUNCIONARIO"
     */
    val role: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val photoUrl: String? = null, // Foto de perfil do usuário

    // --- Campos Específicos do FUNCIONÁRIO ---
    /**
     * ID do Gestor/Estabelecimento ao qual este funcionário pertence.
     * Se for GESTOR ou CLIENTE, este campo fica null.
     */
    val establishmentId: String? = null,

    // --- Campos Específicos do GESTOR (Dono do Negócio) ---
    val businessName: String? = null,
    val cnpj: String? = null,
    val businessPhone: String? = null,
    val areaOfWork: String? = null, // Ex: Barbearia, Salão de Beleza

    val socialLinks: SocialLinks? = null,
    val paymentMethods: List<String>? = null, // ["pix", "credit_card", "cash"]
    val businessAddress: BusinessAddress? = null,

    val subscriptionStatus: String? = "trial" // trial, active, expired
)