package com.familypulse.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.models.UserProfile
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repo: FirebaseRepository,
    darkTheme: Boolean,
    onDarkThemeChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {

    val scope = rememberCoroutineScope()

    var profile by remember {
        mutableStateOf<UserProfile?>(null)
    }

    var consent by remember {
        mutableStateOf(false)
    }

    var saving by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    fun loadProfile() {
        scope.launch {
            val uid = repo.getCurrentUser()?.uid

            if (uid != null) {
                profile = repo.getUserProfile(uid)
                consent = profile?.hasConsented == true
            }
        }
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            "Dark Mode",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            "Use a darker interface."
                        )
                    }

                    Switch(
                        checked = darkTheme,
                        onCheckedChange = onDarkThemeChanged
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    "Privacy & Consent",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "FamilyPulse syncs your account, family membership, " +
                            "shared tasks and voluntary check-ins. " +
                            "Location is optional and is only included " +
                            "when you choose it."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            "Consent",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            if (consent)
                                "Consent enabled"
                            else
                                "Consent disabled"
                        )
                    }

                    Switch(
                        checked = consent,
                        enabled = !saving && profile != null,
                        onCheckedChange = { newValue ->

                            val current = profile
                                ?: return@Switch

                            saving = true
                            message = ""

                            scope.launch {
                                try {

                                    val updated =
                                        current.copy(
                                            hasConsented = newValue
                                        )

                                    repo.saveUserProfile(updated)

                                    profile = updated
                                    consent = newValue

                                    message =
                                        "Privacy preference saved."

                                } catch (e: Exception) {

                                    message =
                                        e.message
                                            ?: "Could not save preference."

                                } finally {
                                    saving = false
                                }
                            }
                        }
                    )
                }
            }
        }

        if (message.isNotBlank()) {

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    "About FamilyPulse",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Simple family coordination with shared tasks, " +
                            "family members and voluntary check-ins."
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Version 1.0")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
