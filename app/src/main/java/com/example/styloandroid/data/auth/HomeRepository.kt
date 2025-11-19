package com.example.styloandroid.data.home // Ou o pacote que preferir

import android.util.Log
import com.example.styloandroid.data.auth.AppUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await

class HomeRepository {

    private val db = FirebaseFirestore.getInstance()

    // Busca todos os usuários que são "profissional"
    suspend fun getProfessionalProviders(): List<AppUser> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("role", "profissional") // O filtro mágico ✨
                .get()
                .await()

            // Converte os documentos para objetos AppUser
            snapshot.toObjects<AppUser>()
        } catch (e: Exception) {
            Log.e("HomeRepository", "Erro ao buscar profissionais", e)
            emptyList() // Retorna lista vazia se der erro
        }
    }
}