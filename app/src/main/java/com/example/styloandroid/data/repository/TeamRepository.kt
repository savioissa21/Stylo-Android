package com.example.styloandroid.data.repository

import com.example.styloandroid.data.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeamRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Criar conta do funcionario
    suspend fun createEmployeeAccount(name: String, email: String, pass: String): Boolean {
        val managerId = auth.currentUser?.uid ?: return false

        val preAccount = hashMapOf(
            "name" to name,
            "email" to email,
            "password" to pass,
            "managerId" to managerId,
            "role" to "FUNCIONARIO",
            "createdAt" to System.currentTimeMillis()
        )

        return try {
            db.collection("temp_accounts").document(email).set(preAccount).await()

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

    suspend fun getMyTeam(): List<AppUser> {
        val managerId = auth.currentUser?.uid ?: return emptyList()
        val team = mutableListOf<AppUser>()

        try {
            val realUsers = db.collection("users")
                .whereEqualTo("establishmentId", managerId)
                .whereEqualTo("role", "FUNCIONARIO")
                .get().await()
            team.addAll(realUsers.toObjects(AppUser::class.java))

            val tempUsers = db.collection("temp_accounts")
                .whereEqualTo("managerId", managerId)
                .get().await()

            tempUsers.forEach { doc ->
                val email = doc.getString("email") ?: ""
                if (team.none { it.email == email }) {
                    team.add(
                        AppUser(
                            uid = "",
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
                db.collection("users").document(employee.uid)
                    .update("establishmentId", null)
                    .await()
            } else {
                db.collection("temp_accounts").document(employee.email).delete().await()
                db.collection("invites").document(employee.email).delete().await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updatePendingEmployeeName(email: String, newName: String): Boolean {
        return try {
            db.collection("temp_accounts").document(email)
                .update("name", newName)
                .await()
            true
        } catch (e: Exception) { false }
    }
}