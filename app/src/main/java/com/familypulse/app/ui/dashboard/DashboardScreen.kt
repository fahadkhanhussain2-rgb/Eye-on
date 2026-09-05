package com.familypulse.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.ui.theme.FamilyPulseLogo
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    repo: FirebaseRepository,
    onLogout: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToMembers: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("Family Member") }
    var familyId by remember { mutableStateOf("") }
    var memberCount by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = repo.getCurrentUser()?.uid

        if (uid != null) {
            val profile = repo.getUserProfile(uid)

            if (profile != null) {
                userName = profile.name.ifBlank { "Family Member" }
                familyId = profile.familyId

                if (familyId.isNotBlank()) {
                    memberCount = repo.getFamilyMembers(familyId).size
                }
            }
        }

        loading = false
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
                    text = "FamilyPulse",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Welcome, $userName",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            TextButton(
                onClick = onLogout
            ) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Family Status",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (loading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp)
                    )

                } else if (familyId.isBlank()) {

                    Text(
                        text = "Your account is not paired with a family yet.",
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
                        text = "Family Code: $familyId",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$memberCount family member" +
                                if (memberCount == 1) "" else "s"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Family Tools",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        DashboardButton(
            title = "Family Members",
            subtitle = "View everyone in your family",
            onClick = onNavigateToMembers
        )

        DashboardButton(
            title = "Family Tasks",
            subtitle = "Create and manage shared tasks",
            onClick = onNavigateToTasks
        )

        DashboardButton(
            title = "Daily Check-In",
            subtitle = "Share a status update with your family",
            onClick = onNavigateToCheckIn
        )

        DashboardButton(
            title = "Family Pairing",
            subtitle = "Create or join a family",
            onClick = onNavigateToPairing
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "FamilyPulse keeps family coordination simple and privacy-conscious.",
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
            .padding(vertical = 5.dp),
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
