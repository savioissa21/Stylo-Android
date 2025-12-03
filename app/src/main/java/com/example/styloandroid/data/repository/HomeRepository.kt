package com.example.styloandroid.data.repository

import com.example.styloandroid.data.model.AppUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeRepository {

    private val db = FirebaseFirestore.getInstance()

    // Busca todos os usuários que são GESTORES (Prestadores de serviço)
    // Retorna explicitamente uma List<AppUser>
    suspend fun getProfessionalProviders(): List<AppUser> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("role", "GESTOR")
                .get()
                .await()

            // Converte os documentos para objetos AppUser
            snapshot.toObjects(AppUser::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}