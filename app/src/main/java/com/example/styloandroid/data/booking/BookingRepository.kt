package com.example.styloandroid.data.booking

import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Busca serviços de UM prestador específico (providerId)
    suspend fun getServicesForProvider(providerId: String): List<Service> {
        return try {
            val snapshot = db.collection("users")
                .document(providerId)
                .collection("services")
                .get()
                .await()
            snapshot.toObjects(Service::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Salva o agendamento na sub-coleção do Prestador E na coleção do Cliente (opcional, mas bom pra histórico)
    suspend fun createAppointment(appointment: Appointment): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            // 1. Gera ID
            val ref = db.collection("appointments").document() // Coleção global ou sub-coleção
            val finalAppointment = appointment.copy(id = ref.id, clientId = user.uid)

            // 2. Salva (Vamos salvar numa coleção raiz 'appointments' para facilitar por enquanto)
            ref.set(finalAppointment).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getCurrentUserName(): String? = auth.currentUser?.displayName
}