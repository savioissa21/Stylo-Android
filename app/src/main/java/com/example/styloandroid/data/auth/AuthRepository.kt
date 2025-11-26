package com.example.styloandroid.data.auth

import android.util.Log
import com.example.styloandroid.ui.auth.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun login(email: String, pass: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                // 1. Tenta login normal no Firebase
                val res = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = res.user?.uid ?: return@withContext AuthResult.Error("Usuário não encontrado")
                return@withContext fetchUserRole(uid)

            } catch (e: Exception) {
                // 2. SE FALHAR: Verifica se é uma conta pré-criada pelo Gestor
                try {
                    val tempDoc = db.collection("temp_accounts").document(email).get().await()

                    if (tempDoc.exists() && tempDoc.getString("password") == pass) {
                        // ACHOU! Cria a conta real agora automaticamente
                        val name = tempDoc.getString("name") ?: "Funcionário"
                        val managerId = tempDoc.getString("managerId")

                        // Cria no Auth
                        val createRes = auth.createUserWithEmailAndPassword(email, pass).await()
                        val user = createRes.user!!

                        user.updateProfile(userProfileChangeRequest { displayName = name }).await()

                        // Cria User no Firestore
                        val appUser = AppUser(
                            uid = user.uid,
                            name = name,
                            email = email,
                            role = "FUNCIONARIO",
                            establishmentId = managerId
                        )
                        db.collection("users").document(user.uid).set(appUser).await()

                        // Deleta a temp
                        db.collection("temp_accounts").document(email).delete()

                        return@withContext AuthResult.Success(user.uid, "FUNCIONARIO")
                    }
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }

                AuthResult.Error("Login falhou. Verifique e-mail e senha.")
            }
        }

    private suspend fun fetchUserRole(uid: String): AuthResult {
        val userDoc = db.collection("users").document(uid).get().await()
        val appUser = userDoc.toObject(AppUser::class.java)
        val role = appUser?.role ?: return AuthResult.Error("Perfil incompleto.")
        return AuthResult.Success(uid, role)
    }

    // ... (Mantenha register, getAppUser, etc) ...
    suspend fun register(data: RegisterViewModel.RegisterData): AuthResult = withContext(Dispatchers.IO) {
        // Mantenha sua lógica original de registro aqui, ou use a simplificada se preferir
        try {
            val res = auth.createUserWithEmailAndPassword(data.email, data.pass).await()
            val user = res.user!!
            val finalRole = if (data.role == "profissional") "GESTOR" else "CLIENTE"

            val appUser = AppUser(uid = user.uid, name = data.name, email = data.email, role = finalRole)
            db.collection("users").document(user.uid).set(appUser).await()

            AuthResult.Success(user.uid, finalRole)
        } catch (e: Exception) { AuthResult.Error(e.message ?: "Erro") }
    }

    fun currentUserId(): String? = auth.currentUser?.uid
    suspend fun getAppUser(): AppUser? = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext null
        db.collection("users").document(uid).get().await().toObject(AppUser::class.java)
    }
    fun logout() = auth.signOut()
}