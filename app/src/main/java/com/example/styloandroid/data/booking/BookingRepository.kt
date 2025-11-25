package com.example.styloandroid.data.booking

import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun getProviderAppointments(): List<Appointment> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("providerId", uid)
                .orderBy("date", Query.Direction.ASCENDING) // Ordena por data/hora
                .get()
                .await()
            snapshot.toObjects(Appointment::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 2. Atualiza o status (ex: "confirmed", "finished", "canceled")
    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: String): Boolean {
        return try {
            db.collection("appointments")
                .document(appointmentId)
                .update("status", newStatus)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getClientAppointments(): List<Appointment> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("clientId", uid)
                .orderBy("date", Query.Direction.DESCENDING) // Do mais recente para o mais antigo
                .get()
                .await()
            snapshot.toObjects(Appointment::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 4. REGRA DE OURO: Verifica se o horário já está ocupado
    suspend fun isTimeSlotTaken(providerId: String, timestamp: Long): Boolean {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("providerId", providerId)
                .whereEqualTo("date", timestamp)
                .whereNotEqualTo("status", "canceled") // Ignora cancelados
                .get()
                .await()
            !snapshot.isEmpty // Retorna true se já tiver agendamento (ocupado)
        } catch (e: Exception) {
            e.printStackTrace()
            true // Na dúvida, bloqueia para evitar conflito
        }
    }

    fun getCurrentUserName(): String? = auth.currentUser?.displayName
}