package com.example.styloandroid.data.repository

import com.example.styloandroid.data.model.AppUser
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Review
import com.example.styloandroid.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- INFORMAÇÕES DO ESTABELECIMENTO E USUÁRIOS ---

    // Busca informações genéricas (usado para pegar dados do Gestor)
    suspend fun getProviderInfo(providerId: String): AppUser? {
        return try {
            val doc = db.collection("users").document(providerId).get().await()
            doc.toObject(AppUser::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // NOVO: Busca configuração específica de um funcionário (ou gestor) pelo ID
    suspend fun getEmployeeConfig(employeeId: String): AppUser? {
        return try {
            val doc = db.collection("users").document(employeeId).get().await()
            doc.toObject(AppUser::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

    // --- LÓGICA DE AGENDAMENTO ---

    suspend fun getAppointmentsForEmployeeOnDate(employeeId: String, startOfDay: Long, endOfDay: Long): List<Appointment> {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("employeeId", employeeId)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .get()
                .await()

            snapshot.toObjects(Appointment::class.java)
                .filter { it.status != "canceled" } // Ignora cancelados para liberar a vaga
                .sortedBy { it.date }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createAppointment(appointment: Appointment): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val ref = db.collection("appointments").document()
            val finalAppointment = appointment.copy(id = ref.id, clientId = user.uid, clientName = user.displayName ?: "Cliente")
            ref.set(finalAppointment).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: String): Boolean {
        return try {
            db.collection("appointments").document(appointmentId).update("status", newStatus).await()
            true
        } catch (e: Exception) { false }
    }

    // --- DASHBOARDS E LISTAS ---

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

    // --- REVIEWS ---

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

    suspend fun cancelAppointment(appointmentId: String): Boolean {
        return try {
            db.collection("appointments").document(appointmentId)
                .update("status", "canceled")
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}