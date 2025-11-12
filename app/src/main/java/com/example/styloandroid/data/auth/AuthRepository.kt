package com.example.styloandroid.data.auth

import com.example.styloandroid.ui.auth.RegisterViewModel // Importe o RegisterData
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
    // ... (função login não muda) ...
    suspend fun login(email: String, pass: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val res = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = res.user?.uid ?: return@withContext AuthResult.Error("Usuário não encontrado")
                AuthResult.Success(uid)
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Falha no login")
            }
        }

    /**
     * Faz registro e cria documento no Firestore usando o RegisterData
     */
    suspend fun register(data: RegisterViewModel.RegisterData): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                // 1. Cria usuário no Firebase Auth
                val res = auth.createUserWithEmailAndPassword(data.email, data.pass).await()
                val user = res.user ?: return@withContext AuthResult.Error("Usuário nulo")

                // 2. Atualiza nome no perfil Firebase
                val profile = userProfileChangeRequest { displayName = data.name }
                user.updateProfile(profile).await()

                // 3. Cria o objeto AppUser completo para o Firestore
                val appUser = AppUser(
                    uid = user.uid,
                    name = data.name,
                    email = data.email,
                    role = data.role,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),

                    // Mapeia os dados do Prestador
                    businessName = data.businessName,
                    cnpj = data.cnpj,
                    businessPhone = data.businessPhone,
                    areaOfWork = data.areaOfWork,
                    socialLinks = data.socialLinks,
                    paymentMethods = data.paymentMethods,
                    businessAddress = data.businessAddress,
                    subscriptionStatus = "trial"
                )

                // 4. Salva no Firestore
                db.collection("users").document(user.uid).set(appUser).await()

                AuthResult.Success(user.uid)
            } catch (e: Exception) {
                // Se falhar, tenta deletar o usuário do Auth para não ficar órfão
                auth.currentUser?.delete()?.await()
                AuthResult.Error(e.message ?: "Falha no registro")
            }
        }

    fun currentUserId(): String? = auth.currentUser?.uid

    fun logout() = auth.signOut()
}