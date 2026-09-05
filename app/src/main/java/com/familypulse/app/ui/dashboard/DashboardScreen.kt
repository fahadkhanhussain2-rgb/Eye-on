@file:OptIn(ExperimentalMaterial3Api::class)

package com.familypulse.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.ui.theme.FamilyPulseLogo
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun DashboardScreen(
    repo: FirebaseRepository,
    onLogout: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("Family Member") }
    var familyId by remember { mutableStateOf("") }
    var memberCount by remember { mutableIntStateOf(0) }
    var taskCount by remember { mutableIntStateOf(0) }
    var checkInCount by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }

    fun refresh() {
        scope.launch {
            loading = true

            try {
                val uid = repo.getCurrentUser()?.uid

                if (uid != null) {
                    val profile = repo.getUserProfile(uid)

                    if (profile != null) {
                        userName = profile.name.ifBlank {
                            "Family Member"
                        }

                        familyId = profile.familyId

                        if (familyId.isNotBlank()) {
                            memberCount =
                                repo.getFamilyMembers(familyId).size

                            taskCount =
                                repo.getTasks(familyId)
                                    .get()
                                    .await()
                                    .size()

                            checkInCount =
                                repo.getCheckIns(familyId)
                                    .get()
                                    .await()
                                    .size()
                        }
                    }
                }
            } catch (_: Exception) {
            }

            loading = false
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FamilyPulseLogo(size = 52.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "FamilyPulse",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    "Welcome, $userName",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = onNavigateToSettings
            ) {
                Text(
                    "⚙",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Family Dashboard",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp)
                    )
                } else if (familyId.isBlank()) {
                    Text(
                        "You haven't joined a family yet.",
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToPairing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set Up Family")
                    }
                } else {
                    Text(
                        "Family Code: $familyId",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        SummaryItem(
                            value = memberCount,
                            label = "Members"
                        )

                        SummaryItem(
                            value = taskCount,
                            label = "Tasks"
                        )

                        SummaryItem(
                            value = checkInCount,
                            label = "Check-ins"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Family Tools",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        DashboardButton(
            "Family Members",
            "See everyone connected to your family",
            onNavigateToMembers
        )

        DashboardButton(
            "Family Tasks",
            "Create and manage shared tasks",
            onNavigateToTasks
        )

        DashboardButton(
            "Daily Check-In",
            "Share a voluntary status update",
            onNavigateToCheckIn
        )

        DashboardButton(
            "Family Pairing",
            "Create or join a family",
            onNavigateToPairing
        )

        DashboardButton(
            "My Profile",
            "View your account and family information",
            onNavigateToProfile
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Privacy-first family coordination. No hidden monitoring.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SummaryItem(
    value: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value.toString(),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun DashboardButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                "›",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
