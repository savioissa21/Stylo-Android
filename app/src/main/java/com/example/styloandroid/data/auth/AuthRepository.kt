package com.example.styloandroid.data.auth

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

    suspend fun login(email: String, pass: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val res = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = res.user?.uid ?: return@withContext AuthResult.Error("Usuário não encontrado")

                val userDoc = db.collection("users").document(uid).get().await()
                val appUser = userDoc.toObject(AppUser::class.java)
                val role = appUser?.role ?: return@withContext AuthResult.Error("Perfil de usuário incompleto.")

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
     * REGISTRO INTELIGENTE:
     * Verifica convites antes de criar o usuário final.
     */
    suspend fun register(data: RegisterViewModel.RegisterData): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                // 1. Cria usuário no Firebase Auth
                val res = auth.createUserWithEmailAndPassword(data.email, data.pass).await()
                val user = res.user ?: return@withContext AuthResult.Error("Usuário nulo")

                // 2. Atualiza nome no perfil
                val profile = userProfileChangeRequest { displayName = data.name }
                user.updateProfile(profile).await()

                // --- LÓGICA DE CONVITE E PAPEIS ---
                var finalRole = data.role
                var establishmentId: String? = null

                // Normaliza roles da UI para o Banco
                if (finalRole == "profissional") finalRole = "GESTOR"
                if (finalRole == "cliente") finalRole = "CLIENTE"

                // 3. Verifica se existe convite para este email
                val inviteRef = db.collection("invites").document(data.email)
                val inviteDoc = inviteRef.get().await()

                if (inviteDoc.exists()) {
                    val status = inviteDoc.getString("status")
                    // Se houver convite pendente, vira FUNCIONARIO automaticamente
                    if (status == "pending") {
                        finalRole = "FUNCIONARIO"
                        establishmentId = inviteDoc.getString("managerId") // Vincula ao patrão

                        // Marca convite como aceito
                        inviteRef.update("status", "accepted")
                    }
                }

                // 4. Cria o objeto AppUser completo
                val appUser = AppUser(
                    uid = user.uid,
                    name = data.name,
                    email = data.email,
                    role = finalRole, // GESTOR, FUNCIONARIO ou CLIENTE
                    establishmentId = establishmentId, // Preenchido se for funcionário
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    
                    // Campos do Gestor (podem ir nulos se for Funcionário ou Cliente)
                    businessName = data.businessName,
                    cnpj = data.cnpj,
                    businessPhone = data.businessPhone,
                    areaOfWork = data.areaOfWork,
                    socialLinks = data.socialLinks,
                    paymentMethods = data.paymentMethods,
                    businessAddress = data.businessAddress,
                    subscriptionStatus = "trial"
                )

                // 5. Salva no Firestore
                db.collection("users").document(user.uid).set(appUser).await()

                // Retorna sucesso com o Role final definido
                AuthResult.Success(user.uid, finalRole)
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Falha no registro")
            }
        }

    fun currentUserId(): String? = auth.currentUser?.uid
    fun logout() = auth.signOut()
}