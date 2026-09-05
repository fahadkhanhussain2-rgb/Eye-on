package com.familypulse.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.models.UserProfile
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    repo: FirebaseRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var profile by remember {
        mutableStateOf<UserProfile?>(null)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        val uid = repo.getCurrentUser()?.uid

        if (uid != null) {
            try {
                profile = repo.getUserProfile(uid)
            } catch (e: Exception) {
                error = e.message ?: "Could not load profile."
            }
        }

        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "My Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (loading) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else if (error.isNotBlank()) {

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )

        } else {

            val currentProfile = profile

            if (currentProfile != null) {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = currentProfile.name.ifBlank {
                                "Family Member"
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentProfile.email,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Role",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Text(
                            text = currentProfile.role,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Family",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Text(
                            text = if (currentProfile.familyId.isBlank()) {
                                "Not paired"
                            } else {
                                currentProfile.familyId
                            },
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Privacy Consent",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Text(
                            text = if (currentProfile.hasConsented) {
                                "Consent enabled"
                            } else {
                                "Consent not recorded"
                            },
                            color = if (currentProfile.hasConsented) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val uid = repo.getCurrentUser()?.uid

                            if (uid != null) {
                                profile = repo.getUserProfile(uid)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Refresh Profile")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
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
