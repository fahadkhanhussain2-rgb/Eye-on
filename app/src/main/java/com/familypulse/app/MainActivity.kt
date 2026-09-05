 package com.familypulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.ui.auth.LoginScreen
import com.familypulse.app.ui.auth.RegisterScreen
import com.familypulse.app.ui.dashboard.DashboardScreen
import com.familypulse.app.ui.pairing.PairingScreen
import com.familypulse.app.ui.tasks.TasksScreen
import com.familypulse.app.ui.checkin.CheckInScreen
import com.familypulse.app.ui.theme.FamilyPulseTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FamilyPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FamilyPulseApp()
                }
            }
        }
    }
}

@Composable
fun FamilyPulseApp() {

    val navController = rememberNavController()
    val repo = remember { FirebaseRepository() }
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
},
onNavigateToPairing = {
    navController.navigate("pairing")
}
            )
        }

        composable("pairing") {

            PairingScreen(
                repo = repo,
                onPairingComplete = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("tasks") {

            val currentUser = auth.currentUser

            TasksScreen(
                repo = repo,
                familyId = currentUser?.uid ?: "",
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("checkin") {

            val currentUser = auth.currentUser

            CheckInScreen(
                repo = repo,
                familyId = currentUser?.uid ?: "",
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
