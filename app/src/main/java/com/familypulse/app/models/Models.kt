package com.familypulse.app.models

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Parent",
    val familyId: String = "",
    val hasConsented: Boolean = false
)

data class FamilyTask(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val assignedTo: String = "",
    val isCompleted: Boolean = false,
    val dueDate: Long = 0L
)

data class ScreenTimeGoal(
    val limitMinutes: Int = 120,
    val dailySchedule: Map<String, String> = emptyMap()
)

data class ManualCheckIn(
    val userId: String = "",
    val userName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val note: String = ""
)
