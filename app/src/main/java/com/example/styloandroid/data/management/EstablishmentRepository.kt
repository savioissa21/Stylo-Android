package com.example.styloandroid.data.management

import android.net.Uri
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class EstablishmentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // --- LÓGICA DE UPLOAD DE IMAGEM ---

    suspend fun uploadProfileImage(imageUri: Uri): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateUserPhotoUrl(url: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users").document(uid)
                .update("photoUrl", url)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- PERFIL E CONFIGURAÇÕES ---

    suspend fun getMyProfile(): AppUser? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(AppUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateEstablishmentSettings(
        openTime: String,
        closeTime: String,
        workDays: List<Int>
    ): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val updates = mapOf(
            "openTime" to openTime,
            "closeTime" to closeTime,
            "workDays" to workDays
        )
        return try {
            db.collection("users").document(uid).update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- SERVIÇOS ---

    suspend fun addService(service: Service): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val docRef = db.collection("users").document(uid).collection("services").document()
            val serviceWithId = service.copy(id = docRef.id)
            docRef.set(serviceWithId).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Busca serviços do PRÓPRIO usuário logado (usado pelo Gestor)
    suspend fun getMyServices(): List<Service> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(uid).collection("services").get().await()
            snapshot.toObjects(Service::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // NOVO: Busca serviços de um usuário ESPECÍFICO (usado pelo Funcionário para ver os do Gestor)
    suspend fun getServices(targetUid: String): List<Service> {
        return try {
            val snapshot = db.collection("users")
                .document(targetUid)
                .collection("services")
                .get()
                .await()
            snapshot.toObjects(Service::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateServiceEmployees(serviceId: String, employeeIds: List<String>): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users").document(uid)
                .collection("services").document(serviceId)
                .update("employeeIds", employeeIds)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteService(serviceId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users").document(uid).collection("services").document(serviceId).delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateService(service: Service): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users").document(uid)
                .collection("services").document(service.id)
                .set(service)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- EQUIPE ---

    suspend fun getMyTeamMembers(): List<AppUser> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val managerDoc = db.collection("users").document(uid).get().await()
            val manager = managerDoc.toObject(AppUser::class.java)
            val employeesSnapshot = db.collection("users")
                .whereEqualTo("establishmentId", uid)
                .whereEqualTo("role", "FUNCIONARIO")
                .get().await()
            val team = employeesSnapshot.toObjects(AppUser::class.java).toMutableList()
            if (manager != null) team.add(0, manager)
            team
        } catch (e: Exception) {
            emptyList()
        }
    }
}