package com.example.styloandroid.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext



sealed class AuthResult {
    data object Loading : AuthResult()
    data class Success(val uid: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Faz login com Firebase Auth
    suspend fun login(email: String, pass: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val res = auth.signInWithEmailAndPassword(email, pass).await()
                val user = res.user ?: return@withContext AuthResult.Error("Usuário não encontrado")
                AuthResult.Success(user.uid)
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Falha no login")
            }
        }

    // Faz registro e cria documento no Firestore
    suspend fun register(name: String, email: String, pass: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val res = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = res.user ?: return@withContext AuthResult.Error("Usuário nulo")

                // Atualiza nome no perfil Firebase
                val profile = userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profile).await()

                // Cria documento no Firestore
                val appUser = AppUser(
                    uid = user.uid,
                    name = name,
                    email = email,
                    role = "cliente"
                )

                db.collection("users").document(user.uid).set(appUser).await()

                AuthResult.Success(user.uid)
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Falha no registro")
            }
        }

    fun currentUserId(): String? = auth.currentUser?.uid

    fun logout() = auth.signOut()
}
