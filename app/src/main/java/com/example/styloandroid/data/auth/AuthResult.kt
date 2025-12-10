package com.example.styloandroid.data.auth


sealed class AuthResult {

    data object Loading : AuthResult()

    data class Success(val uid: String, val role: String) : AuthResult()

    data class Error(val message: String) : AuthResult()
}
