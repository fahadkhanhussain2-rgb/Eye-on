package com.familypulse.app.data

import com.familypulse.app.models.FamilyTask
import com.familypulse.app.models.ManualCheckIn
import com.familypulse.app.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUser() = auth.currentUser

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            db.collection("users")
                .document(uid)
                .get()
                .await()
                .toObject(UserProfile::class.java)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        db.collection("users")
            .document(profile.uid)
            .set(profile)
            .await()
    }

    fun getTasks(familyId: String) =
        db.collection("families")
            .document(familyId)
            .collection("tasks")

    suspend fun addTask(
        familyId: String,
        task: FamilyTask
    ) {
        db.collection("families")
            .document(familyId)
            .collection("tasks")
            .add(task)
            .await()
    }

    suspend fun updateTask(
        familyId: String,
        taskId: String,
        updates: Map<String, Any>
    ) {
        db.collection("families")
            .document(familyId)
            .collection("tasks")
            .document(taskId)
            .update(updates)
            .await()
    }

    suspend fun deleteTask(
        familyId: String,
        taskId: String
    ) {
        db.collection("families")
            .document(familyId)
            .collection("tasks")
            .document(taskId)
            .delete()
            .await()
    }

    fun getCheckIns(familyId: String) =
        db.collection("families")
            .document(familyId)
            .collection("checkins")

    suspend fun sendCheckIn(
        familyId: String,
        checkIn: ManualCheckIn
    ) {
        db.collection("families")
            .document(familyId)
            .collection("checkins")
            .add(checkIn)
            .await()
    }

    suspend fun getFamilyMembers(
        familyId: String
    ): List<UserProfile> {

        if (familyId.isBlank()) {
            return emptyList()
        }

        return db.collection("users")
            .whereEqualTo("familyId", familyId)
            .get()
            .await()
            .documents
            .mapNotNull {
                it.toObject(UserProfile::class.java)
            }
    }

    suspend fun createFamilyCode(): String {

        repeat(5) {

            val code = (100000..999999)
                .random()
                .toString()

            val ref = db.collection("families")
                .document(code)

            if (!ref.get().await().exists()) {

                ref.set(
                    mapOf(
                        "createdBy" to auth.currentUser?.uid,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()

                return code
            }
        }

        throw IllegalStateException(
            "Could not create a unique family code. Please try again."
        )
    }
}
