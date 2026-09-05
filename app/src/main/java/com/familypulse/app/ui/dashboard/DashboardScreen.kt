package com.familypulse.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familypulse.app.data.FirebaseRepository

@Composable
fun DashboardScreen(
    repo: FirebaseRepository,
    onLogout: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToCheckIn: () -> Unit
) {
    val user = repo.getCurrentUser()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "FamilyPulse",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Family Dashboard",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = user?.email ?: "User"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNavigateToTasks,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Family Tasks")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNavigateToCheckIn,
            modifier
