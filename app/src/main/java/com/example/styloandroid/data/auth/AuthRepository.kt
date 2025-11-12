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

    /**
     * Faz registro e cria documento no Firestore usando o RegisterData
     */
    suspend fun register(user: AppUser, pass: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                // 1. Registro no Firebase Auth
                val res = auth.createUserWithEmailAndPassword(user.email, pass).await()

                // O UID é gerado aqui. Se for nulo, algo deu errado no Auth.
                val uid = res.user?.uid ?: return@withContext AuthResult.Error("Usuário não encontrado após o registro.")

                // ⚡️ CORREÇÃO: Cria uma cópia do AppUser e insere o UID gerado
                val userWithUid = user.copy(uid = uid)

                // 2. Criação do Documento no Firestore, usando o UID como ID do Documento
                // E salvando o objeto atualizado (userWithUid)
                db.collection("users").document(uid).set(userWithUid).await()

                // Retorna sucesso, incluindo o 'role' para ser usado na navegação
                AuthResult.Success(uid, userWithUid.role)

            } catch (e: Exception) {
                // ... (seu tratamento de erro)
                AuthResult.Error(e.message ?: "Falha no registro")
            }
        }

    fun currentUserId(): String? = auth.currentUser?.uid

    fun logout() = auth.signOut()
}