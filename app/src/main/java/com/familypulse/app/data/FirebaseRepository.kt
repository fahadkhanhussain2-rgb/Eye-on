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
        } catch (e: Exception) {
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

    suspend fun addTask(familyId: String, task: FamilyTask) {
        db.collection("families")
            .document(familyId)
            .collection("tasks")
            .add(task)
            .await()
    }

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

    fun getCheckIns(familyId: String) =
        db.collection("families")
            .document(familyId)
            .collection("checkins")
            suspend fun createFamilyCode(): String {
    val code = (100000..999999).random().toString()

    db.collection("families")
        .document(code)
        .set(
            mapOf(
                "createdBy" to auth.currentUser?.uid,
                "createdAt" to System.currentTimeMillis()
            )
        )
        .await()

    return code
            }
}
