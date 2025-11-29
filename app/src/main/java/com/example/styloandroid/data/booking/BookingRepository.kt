package com.example.styloandroid.data.booking

import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Review
import com.example.styloandroid.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- SERVIÇOS E EQUIPE ---

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

    suspend fun getTeamForEstablishment(managerId: String): List<AppUser> {
        return try {
            val team = mutableListOf<AppUser>()

            // 1. Busca o Gestor
            val managerDoc = db.collection("users").document(managerId).get().await()
            managerDoc.toObject(AppUser::class.java)?.let { team.add(it) }

            // 2. Busca Funcionários
            val employeesSnapshot = db.collection("users")
                .whereEqualTo("establishmentId", managerId)
                .whereEqualTo("role", "FUNCIONARIO")
                .get()
                .await()
            
            team.addAll(employeesSnapshot.toObjects(AppUser::class.java))
            team
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- AGENDAMENTOS (CRUD) ---

    suspend fun createAppointment(appointment: Appointment): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val ref = db.collection("appointments").document()
            // Adiciona ID gerado e o ID do cliente logado
            val finalAppointment = appointment.copy(id = ref.id, clientId = user.uid, clientName = user.displayName ?: "Cliente")
            ref.set(finalAppointment).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Verifica se o funcionário já está ocupado naquele horário
    suspend fun isTimeSlotTaken(employeeId: String, newStartTime: Long, durationMin: Int): Boolean {
        return try {
            val newEndTime = newStartTime + (durationMin * 60 * 1000)

            // Busca todos os agendamentos desse funcionário
            val snapshot = db.collection("appointments")
                .whereEqualTo("employeeId", employeeId)
                .get()
                .await()

            val appointments = snapshot.toObjects(Appointment::class.java)

            // Verifica colisão de horário em memória
            appointments.any { existing ->
                if (existing.status == "canceled") return@any false

                val existingStart = existing.date
                val existingEnd = existingStart + (existing.durationMin * 60 * 1000)

                // Lógica de intersecção: (StartA < EndB) e (EndA > StartB)
                (newStartTime < existingEnd && newEndTime > existingStart)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false 
        }
    }

    // === A FUNÇÃO QUE FALTAVA ===
    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: String): Boolean {
        return try {
            db.collection("appointments").document(appointmentId).update("status", newStatus).await()
            true
        } catch (e: Exception) { false }
    }
    // ============================

    // --- LISTAGEM PARA DASHBOARDS ---

    suspend fun getProviderAppointments(): List<Appointment> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val role = userDoc.getString("role")

            val query = if (role == "FUNCIONARIO") {
                db.collection("appointments").whereEqualTo("employeeId", uid)
            } else {
                db.collection("appointments").whereEqualTo("providerId", uid)
            }

            val snapshot = query.get().await()
            snapshot.toObjects(Appointment::class.java).sortedBy { it.date }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getClientAppointments(): List<Appointment> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("clientId", uid)
                .get()
                .await()
            snapshot.toObjects(Appointment::class.java).sortedByDescending { it.date }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- AVALIAÇÕES (REVIEWS) ---

    suspend fun submitReview(review: Review): Boolean {
        return try {
            val ref = db.collection("reviews").document()
            val finalReview = review.copy(id = ref.id)
            ref.set(finalReview).await()

            db.collection("appointments").document(review.appointmentId)
                .update("hasReview", true)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getReviewsStats(providerId: String): Pair<Double, Int> {
        return try {
            val snapshot = db.collection("reviews")
                .whereEqualTo("providerId", providerId)
                .get()
                .await()
            
            val reviews = snapshot.toObjects(Review::class.java)
            if (reviews.isEmpty()) return Pair(5.0, 0)

            val avg = reviews.map { it.rating }.average()
            val count = reviews.size
            Pair(avg, count)
        } catch (e: Exception) {
            Pair(5.0, 0)
        }
    }

    fun getCurrentUserName(): String? = auth.currentUser?.displayName
}