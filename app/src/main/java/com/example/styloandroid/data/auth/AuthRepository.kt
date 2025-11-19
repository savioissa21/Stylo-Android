package com.example.styloandroid.data.auth

import android.R.attr.data
import android.util.Log
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

                // 1. Buscar o documento do usuário no Firestore
                val userDoc = db.collection("users").document(uid).get().await()
                val appUser = userDoc.toObject(AppUser::class.java)

                // 2. Extrair o role (função)
                val role = appUser?.role ?: return@withContext AuthResult.Error("Perfil de usuário incompleto.")

                // 3. Retornar Sucesso com o role!
                AuthResult.Success(uid, role)

            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Falha no login")
            }
        }

    suspend fun getAppUser(): AppUser? = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext null
        try {
            val doc = db.collection("users").document(uid).get().await()
            return@withContext doc.toObject(AppUser::class.java)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Erro ao buscar AppUser: ${e.message}")
            return@withContext null
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
                    uid = user.uid, // O UID AGORA É USADO AQUI
                    name = data.name,
                    email = data.email,
                    role = data.role,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
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

                // 🟢 CORREÇÃO: Passa o UID e o ROLE, resolvendo o erro no AuthRepository.
                AuthResult.Success(user.uid, data.role)
            } catch (e: Exception) {
                // ... (tratamento de erro)
                AuthResult.Error(e.message ?: "Falha no registro")
            }
        }


    fun currentUserId(): String? = auth.currentUser?.uid

    fun logout() = auth.signOut()
}