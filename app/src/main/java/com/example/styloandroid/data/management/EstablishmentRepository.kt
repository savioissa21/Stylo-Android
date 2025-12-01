package com.example.styloandroid.data.management

import android.net.Uri
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage // Import novo
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EstablishmentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance() // Instância do Storage

    // --- LÓGICA DE UPLOAD DE IMAGEM ---
    suspend fun uploadProfileImage(imageUri: Uri): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            // Cria uma referência: profile_images/USER_ID.jpg
            // Usamos o UID como nome do arquivo para substituir a antiga automaticamente se o usuário trocar
            val ref = storage.reference.child("profile_images/$uid.jpg")

            // Faz o upload
            ref.putFile(imageUri).await()

            // Pega a URL pública para download
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

    // ... MANTENHA AS OUTRAS FUNÇÕES (addService, getMyServices, etc) EXATAMENTE COMO ESTAVAM ...
    // Estou apenas repetindo getMyProfile para garantir que você veja onde ele se encaixa
    suspend fun getMyProfile(): AppUser? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(AppUser::class.java)
        } catch (e: Exception) { null }
    }

    // ... MANTENHA updateEstablishmentSettings, addService, deleteService, etc ...
    suspend fun updateEstablishmentSettings(openTime: String, closeTime: String, workDays: List<Int>): Boolean {
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

    suspend fun getMyServices(): List<Service> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(uid).collection("services").get().await()
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
            db.collection("users").document(uid).collection("services").document(serviceId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

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
        } catch (e: Exception) { emptyList() }
    }

    suspend fun updateService(service: Service): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            // .set() com o mesmo ID sobrescreve (atualiza) os dados
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
}