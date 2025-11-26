package com.example.styloandroid.data.management

import com.example.styloandroid.data.auth.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeamRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Envia um convite (Cria documento na coleção 'invites')
    suspend fun inviteEmployee(email: String): Boolean {
        val managerId = auth.currentUser?.uid ?: return false
        
        // Dados do convite
        val inviteData = hashMapOf(
            "email" to email,
            "managerId" to managerId,
            "status" to "pending", // pendente, aceito, recusado
            "createdAt" to System.currentTimeMillis()
        )

        return try {
            // Usamos o e-mail como ID do documento para evitar duplicatas fáceis
            db.collection("invites").document(email).set(inviteData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Busca funcionários que já aceitaram (já são da equipe)
    suspend fun getMyTeam(): List<AppUser> {
        val managerId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("establishmentId", managerId)
                .whereEqualTo("role", "FUNCIONARIO") // Garante que é funcionário
                .get()
                .await()
            snapshot.toObjects(AppUser::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}