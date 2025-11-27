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
                // CORREÇÃO AQUI: Mude de "profissional" para "GESTOR"
                .whereEqualTo("role", "GESTOR")
                .get()
                .await()

            snapshot.toObjects(AppUser::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}