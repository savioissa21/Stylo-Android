package com.example.styloandroid.data.repository

import com.example.styloandroid.data.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Busca todos os usuários que são GESTORES (Prestadores de serviço)
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

    // --- SISTEMA DE FAVORITOS ---

    // 1. Alternar Favorito (Adicionar ou Remover)
    suspend fun toggleFavorite(providerId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val favRef = db.collection("users").document(uid)
            .collection("favorites").document(providerId)

        return try {
            val doc = favRef.get().await()
            if (doc.exists()) {
                // Se já existe, remove (desfavoritar)
                favRef.delete().await()
                false // Retorna false indicando que NÃO é mais favorito
            } else {
                // Se não existe, cria (favoritar)
                val data = hashMapOf("timestamp" to System.currentTimeMillis())
                favRef.set(data).await()
                true // Retorna true indicando que AGORA é favorito
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
            // Retorna apenas os IDs dos documentos (que são os IDs dos prestadores)
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }
}