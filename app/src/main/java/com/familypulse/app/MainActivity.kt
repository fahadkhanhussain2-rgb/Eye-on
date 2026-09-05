package com.familypulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.ui.auth.LoginScreen
import com.familypulse.app.ui.auth.RegisterScreen
import com.familypulse.app.ui.checkin.CheckInScreen
import com.familypulse.app.ui.dashboard.DashboardScreen
import com.familypulse.app.ui.tasks.TasksScreen
import com.familypulse.app.ui.theme.FamilyPulseTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val repo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FamilyPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FamilyPulseApp(repo)
                }
            }
        }
    }
}

@Composable
fun FamilyPulseApp(repo: FirebaseRepository) {

    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    val startDestination =
        if (auth.currentUser != null) "dashboard"
        else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                repo = repo,
                onLogout = {
                    auth.signOut()

                    navController.navigate("login") {
                        popUpTo("dashboard") {
                            inclusive = true
                        }
                    }
                },
                onNavigateToTasks = {
                    navController.navigate("tasks")
                },
                onNavigateToCheckIn = {
                    navController.navigate("checkin")
                }
            )
        }

        composable("tasks") {

            val uid = auth.currentUser?.uid

            if (uid != null) {
                val profile by produceState<com.familypulse.app.models.UserProfile?>(
                    initialValue = null
                ) {
                    value = repo.getUserProfile(uid)
                }

                val familyId = profile?.familyId

                if (!familyId.isNullOrBlank()) {
                    TasksScreen(
                        repo = repo,
                        familyId = familyId,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        composable("checkin") {

            val uid = auth.currentUser?.uid

            if (uid != null) {
                val profile by produceState<com.familypulse.app.models.UserProfile?>(
                    initialValue = null
                ) {
                    value = repo.getUserProfile(uid)
                }

                val familyId = profile?.familyId

                if (!familyId.isNullOrBlank()) {
                    CheckInScreen(
                        repo = repo,
                        familyId = familyId,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
