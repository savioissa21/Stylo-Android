package com.example.styloandroid.data.repository

import android.net.Uri
import com.example.styloandroid.data.auth.AuthResult
import com.example.styloandroid.data.model.AppUser
import com.example.styloandroid.ui.auth.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    suspend fun login(email: String, pass: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                // 1. Tenta login normal no Firebase
                val res = auth.signInWithEmailAndPassword(email, pass).await()
                val uid =
                    res.user?.uid ?: return@withContext AuthResult.Error("Usuário não encontrado")
                return@withContext fetchUserRole(uid)

            } catch (e: Exception) {
                // 2. SE FALHAR: Verifica se é uma conta pré-criada pelo Gestor (Lógica existente)
                try {
                    val tempDoc = db.collection("temp_accounts").document(email).get().await()

                    if (tempDoc.exists() && tempDoc.getString("password") == pass) {
                        val name = tempDoc.getString("name") ?: "Funcionário"
                        val managerId = tempDoc.getString("managerId")

                        val createRes = auth.createUserWithEmailAndPassword(email, pass).await()
                        val user = createRes.user!!

                        user.updateProfile(userProfileChangeRequest {
                            displayName = name
                        }).await()

                        val appUser = AppUser(
                            uid = user.uid,
                            name = name,
                            email = email,
                            role = "FUNCIONARIO",
                            establishmentId = managerId
                        )
                        db.collection("users").document(user.uid).set(appUser).await()
                        db.collection("temp_accounts").document(email).delete()

                        return@withContext AuthResult.Success(user.uid, "FUNCIONARIO")
                    }
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }

                AuthResult.Error("Login falhou. Verifique e-mail e senha.")
            }
        }

    // Enviar e-mail de redefinição de senha
    suspend fun sendPasswordReset(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Atualizar Perfil Genérico (Cliente ou qualquer user)
    suspend fun updateUserProfile(name: String, phone: String, photoUri: Uri?): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "phoneNumber" to phone
            )

            // Se tiver foto nova, faz upload e pega URL
            if (photoUri != null) {
                val ref = storage.reference.child("profile_images/$uid.jpg")
                ref.putFile(photoUri).await()
                val url = ref.downloadUrl.await().toString()
                updates["photoUrl"] = url
            }

            db.collection("users").document(uid).update(updates).await()

            // Atualiza também no Auth Profile para consistência
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            auth.currentUser?.updateProfile(profileUpdates)?.await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun fetchUserRole(uid: String): AuthResult {
        val userDoc = db.collection("users").document(uid).get().await()
        val appUser = userDoc.toObject(AppUser::class.java)
        val role = appUser?.role ?: return AuthResult.Error("Perfil incompleto.")
        return AuthResult.Success(uid, role)
    }

    suspend fun register(data: RegisterViewModel.RegisterData): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val res = auth.createUserWithEmailAndPassword(data.email, data.pass).await()
                val user = res.user!!
                val finalRole = if (data.role == "profissional") "GESTOR" else "CLIENTE"

                // Mapeia os dados do AppUser corretamente
                val appUser = AppUser(
                    uid = user.uid,
                    name = data.name,
                    email = data.email,
                    role = finalRole,

                    // Mapeia campos do Gestor se necessário
                    businessName = data.businessName,
                    areaOfWork = data.areaOfWork,
                    cnpj = data.cnpj,
                    businessPhone = data.businessPhone,
                    socialLinks = data.socialLinks,
                    paymentMethods = data.paymentMethods,
                    businessAddress = data.businessAddress
                )

                db.collection("users").document(user.uid).set(appUser).await()

                AuthResult.Success(user.uid, finalRole)
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Erro")
            }
        }

    fun currentUserId(): String? = auth.currentUser?.uid
    suspend fun getAppUser(): AppUser? = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext null
        db.collection("users").document(uid).get().await().toObject(AppUser::class.java)
    }
    fun logout() = auth.signOut()
}