package com.seuprojeto.stylo.data.auth

/**
 * Representa o estado de uma operação de autenticação (login ou registro)
 * no fluxo MVVM. Usada pelos ViewModels para atualizar a UI.
 */
sealed class AuthResult {

    /**
     * Estado de carregamento (usado enquanto o login ou registro está em andamento).
     */
    data object Loading : AuthResult()

    /**
     * Estado de sucesso, contendo o ID do usuário autenticado (UID do Firebase, por exemplo).
     */
    data class Success(val uid: String) : AuthResult()

    /**
     * Estado de erro, contendo a mensagem de falha (ex: credenciais inválidas, rede, etc.).
     */
    data class Error(val message: String) : AuthResult()
}
