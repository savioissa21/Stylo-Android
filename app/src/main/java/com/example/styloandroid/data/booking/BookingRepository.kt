package com.example.styloandroid.data.booking

import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Review
import com.example.styloandroid.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

    /**
     * Busca a equipe completa: O Gestor (Dono) + Funcionários vinculados
     */
    suspend fun getTeamForEstablishment(managerId: String): List<AppUser> {
        return try {
            val team = mutableListOf<AppUser>()

            // 1. Busca o próprio Gestor (Ele também atende?)
            val managerDoc = db.collection("users").document(managerId).get().await()
            managerDoc.toObject(AppUser::class.java)?.let { team.add(it) }

            // 2. Busca Funcionários vinculados a este estabelecimento
            val employeesSnapshot = db.collection("users")
                .whereEqualTo("establishmentId", managerId)
                .whereEqualTo("role", "FUNCIONARIO")
                .get()
                .await()
            
            team.addAll(employeesSnapshot.toObjects(AppUser::class.java))
            
            team
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createAppointment(appointment: Appointment): Boolean { // <- RECEBE O AGENDAMENTO
        val user = auth.currentUser ?: return false
        return try {
            val ref = db.collection("appointments").document()
            // Adiciona apenas ID e ClientID, mas o resto (Data, ProviderId) já veio pronto!
            val finalAppointment = appointment.copy(id = ref.id, clientId = user.uid)
            ref.set(finalAppointment).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * VERIFICAÇÃO DE CHOQUE DE HORÁRIO
     * Agora verifica se o *EmployeeId* específico está ocupado naquele horário.
     */
    // Em BookingRepository.kt

    suspend fun isTimeSlotTaken(employeeId: String, newStartTime: Long, durationMin: Int): Boolean {
        return try {
            // Define o fim do novo agendamento
            val newEndTime = newStartTime + (durationMin * 60 * 1000)

            // 1. CORREÇÃO: Buscamos TODOS os agendamentos (incluindo cancelados)
            // Removemos o .whereNotEqualTo para o Firebase não pedir índice
            val snapshot = db.collection("appointments")
                .whereEqualTo("employeeId", employeeId)
                .get()
                .await()

            val appointments = snapshot.toObjects(Appointment::class.java)

            // 2. FILTRAGEM NA MEMÓRIA: O Kotlin faz o trabalho sujo
            appointments.any { existing ->
                // Se estiver cancelado, IGNORA (finge que não existe)
                if (existing.status == "canceled") return@any false

                val existingStart = existing.date
                val existingEnd = existingStart + (existing.durationMin * 60 * 1000)

                // Lógica de intersecção de horários (Matemática pura)
                (newStartTime < existingEnd && newEndTime > existingStart)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // DICA DE OURO: Retorne FALSE no catch.
            // Se der erro de internet, melhor deixar o cliente tentar agendar
            // do que bloquear dizendo "Horário Indisponível".
            false
        }
    }

    // --- Métodos de Listagem (Mantidos/Atualizados) ---

    suspend fun getProviderAppointments(): List<Appointment> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val role = userDoc.getString("role")

            val query = if (role == "FUNCIONARIO") {
                // Busca agendamentos deste funcionário
                db.collection("appointments").whereEqualTo("employeeId", uid)
            } else {
                // Gestor vê tudo do estabelecimento
                db.collection("appointments").whereEqualTo("providerId", uid)
            }

            // CORREÇÃO: Removemos .orderBy("date") do Firestore para evitar erro de índice
            val snapshot = query.get().await()

            // MÁGICA: Ordenamos a lista aqui no Kotlin (do mais antigo para o mais novo)
            snapshot.toObjects(Appointment::class.java).sortedBy { it.date }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getClientAppointments(): List<Appointment> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("clientId", uid)
                // CORREÇÃO: Removemos .orderBy do Firestore
                .get()
                .await()

            // MÁGICA: Ordenamos aqui (do mais recente para o mais antigo)
            snapshot.toObjects(Appointment::class.java).sortedByDescending { it.date }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: String): Boolean {
        return try {
            db.collection("appointments").document(appointmentId).update("status", newStatus).await()
            true
        } catch (e: Exception) { false }
    }

    // --- NOVO: Enviar Avaliação ---
    suspend fun submitReview(review: Review): Boolean {
        return try {
            // 1. Salva a Review na coleção global 'reviews'
            val ref = db.collection("reviews").document()
            val finalReview = review.copy(id = ref.id)
            ref.set(finalReview).await()

            // 2. Marca o agendamento como avaliado (hasReview = true)
            // Isso impede que o usuário avalie o mesmo serviço 2 vezes
            db.collection("appointments").document(review.appointmentId)
                .update("hasReview", true)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getCurrentUserName(): String? = auth.currentUser?.displayName
}