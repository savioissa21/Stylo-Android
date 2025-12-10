package com.example.styloandroid.data.repository

import com.example.styloandroid.data.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Busca todos os usuários que são Prestadores de serviço
    suspend fun getProfessionalProviders(): List<AppUser> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("role", "GESTOR")
                .get()
                .await()
            snapshot.toObjects(AppUser::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 1. Alternar Favorito Adicionar ou Remover
    suspend fun toggleFavorite(providerId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val favRef = db.collection("users").document(uid)
            .collection("favorites").document(providerId)

        return try {
            val doc = favRef.get().await()
            if (doc.exists()) {
                favRef.delete().await()
                false 
            } else {
                val data = hashMapOf("timestamp" to System.currentTimeMillis())
                favRef.set(data).await()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 2. Buscar Lista de IDs dos Favoritos do Usuário Logado
    suspend fun getUserFavoriteIds(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("favorites")
                .get()
                .await()
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }
}