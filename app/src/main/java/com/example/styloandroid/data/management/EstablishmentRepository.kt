package com.example.styloandroid.data.management

import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EstablishmentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Adiciona um novo serviço
    suspend fun addService(service: Service): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            // Cria um ID único para o serviço automaticamente
            val docRef = db.collection("users").document(uid).collection("services").document()
            val serviceWithId = service.copy(id = docRef.id)
            docRef.set(serviceWithId).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Busca os serviços do utilizador logado
    suspend fun getMyServices(): List<Service> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(uid).collection("services").get().await()
            snapshot.toObjects(Service::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Apaga serviço
    suspend fun deleteService(serviceId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users").document(uid).collection("services").document(serviceId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // NOVO: Busca a equipe para exibir no Dialog de seleção
    suspend fun getMyTeamMembers(): List<AppUser> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            // Busca o próprio Gestor primeiro
            val managerDoc = db.collection("users").document(uid).get().await()
            val manager = managerDoc.toObject(AppUser::class.java)
            
            // Busca Funcionários
            val employeesSnapshot = db.collection("users")
                .whereEqualTo("establishmentId", uid)
                .whereEqualTo("role", "FUNCIONARIO")
                .get()
                .await()
            
            val team = employeesSnapshot.toObjects(AppUser::class.java).toMutableList()
            
            // Adiciona o gestor no topo da lista (ele também pode trabalhar)
            if (manager != null) {
                team.add(0, manager)
            }
            
            team
        } catch (e: Exception) {
            emptyList()
        }
    }
}