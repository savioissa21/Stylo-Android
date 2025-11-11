// ARQUIVO: com.example.styloandroid.data.services/AuthService.kt

package com.example.styloandroid.data.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthService {
    // Inicialização dos serviços do Firebase
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    // Enum selado para passar o resultado de forma segura
    sealed class AuthResult {
        data object Success : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    /**
     * Tenta fazer o login.
     */
    fun login(email: String, password: String, callback: (AuthResult) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(AuthResult.Success)
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Erro de login desconhecido."
                    callback(AuthResult.Error(errorMessage))
                }
            }
    }

    /**
     * Tenta registrar um novo cliente (Cria Auth e salva no Firestore).
     */
    fun registerClient(email: String, password: String, name: String, callback: (AuthResult) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val userId = authTask.result?.user?.uid
                    val user = hashMapOf(
                        "uid" to userId,
                        "name" to name,
                        "email" to email,
                        "role" to "client",
                        "createdAt" to System.currentTimeMillis()
                    )

                    if (userId != null) {
                        db.collection("users").document(userId).set(user)
                            .addOnSuccessListener {
                                callback(AuthResult.Success)
                            }
                            .addOnFailureListener { firestoreError ->
                                auth.currentUser?.delete()
                                callback(AuthResult.Error("Cadastro falhou: ${firestoreError.localizedMessage}"))
                            }
                    } else {
                        callback(AuthResult.Error("UID do usuário não encontrado."))
                    }

                } else {
                    val errorMessage = authTask.exception?.localizedMessage ?: "Erro de registro desconhecido."
                    callback(AuthResult.Error(errorMessage))
                }
            }
    }
}