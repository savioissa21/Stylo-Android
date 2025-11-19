package com.example.styloandroid.data

/**
 * Modelo de dados simplificado para exibição no card de busca do cliente.
 * * @param id O ID do estabelecimento/usuário (AppUser UID)
 * @param businessName Nome do Estabelecimento (ex: Barbearia do Tony)
 * @param areaOfWork Área de atuação (ex: Barbearia e Corte Masculino)
 * @param rating Média de avaliação (ex: 4.8)
 * @param reviewCount Número de avaliações (ex: 120)
 * @param profileImageUrl URL da foto do perfil/logo
 */
data class ProviderCardData(
    val id: String,
    val businessName: String,
    val areaOfWork: String,
    val rating: Double,
    val reviewCount: Int,
    val profileImageUrl: String? = null
)