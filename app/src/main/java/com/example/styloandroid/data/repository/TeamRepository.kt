package com.example.styloandroid.data.repository

import com.example.styloandroid.data.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeamRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // CRIA A CONTA "VIRTUAL" DO FUNCIONÁRIO
    suspend fun createEmployeeAccount(name: String, email: String, pass: String): Boolean {
        val managerId = auth.currentUser?.uid ?: return false

        // Dados da conta pré-aprovada
        val preAccount = hashMapOf(
            "name" to name,
            "email" to email,
            "password" to pass, // Em app real, criptografaríamos isso. Para faculdade, ok.
            "managerId" to managerId,
            "role" to "FUNCIONARIO",
            "createdAt" to System.currentTimeMillis()
        )

        return try {
            // Salva na coleção de contas temporárias
            // Usamos o email como ID para facilitar a busca no login
            db.collection("temp_accounts").document(email).set(preAccount).await()

            // Também cria um "convite aceito" para garantir
            val inviteData = hashMapOf(
                "email" to email,
                "managerId" to managerId,
                "status" to "accepted"
            )
            db.collection("invites").document(email).set(inviteData).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Busca funcionários (tanto os reais quanto os pré-criados)
    suspend fun getMyTeam(): List<AppUser> {
        val managerId = auth.currentUser?.uid ?: return emptyList()
        val team = mutableListOf<AppUser>()

        try {
            // 1. Funcionários Reais (Já cadastrados no Auth)
            val realUsers = db.collection("users")
                .whereEqualTo("establishmentId", managerId)
                .whereEqualTo("role", "FUNCIONARIO")
                .get().await()
            team.addAll(realUsers.toObjects(AppUser::class.java))

            // 2. Funcionários Pré-criados (Ainda não logaram/ativaram)
            val tempUsers = db.collection("temp_accounts")
                .whereEqualTo("managerId", managerId)
                .get().await()

            tempUsers.forEach { doc ->
                // Evita duplicatas se o cara já tiver virado user real
                val email = doc.getString("email") ?: ""
                if (team.none { it.email == email }) {
                    team.add(
                        AppUser(
                            uid = "", // Sem UID ainda
                            name = doc.getString("name") ?: "Pendente",
                            email = email,
                            role = "FUNCIONARIO (Pendente)"
                        )
                    )
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        return team
    }

    suspend fun removeEmployee(employee: AppUser): Boolean {
        return try {
            if (employee.uid.isNotEmpty()) {
                // Caso 1: Usuário Real -> Apenas desvincula (null no establishmentId)
                // O usuário continua existindo no Auth, mas perde acesso aos dados da loja.
                db.collection("users").document(employee.uid)
                    .update("establishmentId", null)
                    .await()
            } else {
                // Caso 2: Conta Pendente (Temp) -> Exclui os documentos
                db.collection("temp_accounts").document(employee.email).delete().await()
                db.collection("invites").document(employee.email).delete().await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Opcional: Editar nome (apenas se for conta temporária, pois usuário real gerencia o próprio perfil)
    suspend fun updatePendingEmployeeName(email: String, newName: String): Boolean {
        return try {
            db.collection("temp_accounts").document(email)
                .update("name", newName)
                .await()
            true
        } catch (e: Exception) { false }
    }
}