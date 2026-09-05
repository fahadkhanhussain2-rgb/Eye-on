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
import com.familypulse.app.ui.members.FamilyMembersScreen
import com.familypulse.app.ui.pairing.PairingScreen
import com.familypulse.app.ui.profile.ProfileScreen
import com.familypulse.app.ui.settings.SettingsScreen
import com.familypulse.app.ui.tasks.TasksScreen
import com.familypulse.app.ui.theme.FamilyPulseTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FamilyPulseApp()
        }
    }
}

@Composable
fun FamilyPulseApp() {

    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember {
        context.getSharedPreferences(
            "familypulse",
            android.content.Context.MODE_PRIVATE
        )
    }

    var darkTheme by remember {
        mutableStateOf(
            preferences.getBoolean("dark_mode", false)
        )
    }

    FamilyPulseTheme(darkTheme = darkTheme) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FamilyPulseNavigation(
                darkTheme = darkTheme,
                onDarkThemeChanged = {
                    darkTheme = it
                    preferences.edit()
                        .putBoolean("dark_mode", it)
                        .apply()
                }
            )
        }
    }
}

@Composable
private fun FamilyPulseNavigation(
    darkTheme: Boolean,
    onDarkThemeChanged: (Boolean) -> Unit
) {

    val navController = rememberNavController()
    val repo = remember { FirebaseRepository() }
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var familyId by remember {
        mutableStateOf("")
    }

    fun refreshFamilyId() {
        scope.launch {
            val uid = auth.currentUser?.uid

            familyId = if (uid != null) {
                repo.getUserProfile(uid)?.familyId ?: ""
            } else {
                ""
            }
        }
    }

    LaunchedEffect(auth.currentUser?.uid) {
        refreshFamilyId()
    }

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
                    refreshFamilyId()
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
                    refreshFamilyId()
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
                    familyId = ""

                    navController.navigate("login") {
                        popUpTo("dashboard") {
                            inclusive = true
                        }
                    }
                },

                onNavigateToTasks = {
                    refreshFamilyId()
                    navController.navigate("tasks")
                },

                onNavigateToCheckIn = {
                    refreshFamilyId()
                    navController.navigate("checkin")
                },

                onNavigateToPairing = {
                    navController.navigate("pairing")
                },

                onNavigateToMembers = {
                    refreshFamilyId()
                    navController.navigate("members")
                },

                onNavigateToProfile = {
                    navController.navigate("profile")
                },

                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("members") {
            FamilyMembersScreen(
                repo = repo,
                familyId = familyId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("tasks") {
            TasksScreen(
                repo = repo,
                familyId = familyId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("checkin") {
            CheckInScreen(
                repo = repo,
                familyId = familyId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("pairing") {
            PairingScreen(
                repo = repo,
                onPairingComplete = {
                    refreshFamilyId()
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                repo = repo,
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    auth.signOut()
                    familyId = ""

                    navController.navigate("login") {
                        popUpTo("dashboard") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                repo = repo,
                darkTheme = darkTheme,
                onDarkThemeChanged = onDarkThemeChanged,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
