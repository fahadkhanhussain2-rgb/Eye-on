package com.familypulse.app.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    familyId: String,
    onBack: () -> Unit
) {
    var minutes by remember { mutableStateOf("120") }
    var message by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen Time Goals") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {

            Text(
                "Daily Screen-Time Goal",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Set a suggested daily screen-time goal for your family."
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = minutes,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        minutes = it
                    }
                },
                label = { Text("Minutes per day") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val value = minutes.toIntOrNull()

                    if (value == null || value <= 0) {
                        message = "Please enter a valid number of minutes."
                        return@Button
                    }

                    isSaving = true
                    message = null

                    val goal = mapOf(
                        "limitMinutes" to value,
                        "updatedAt" to System.currentTimeMillis()
                    )

                    db.collection("families")
                        .document(familyId)
                        .collection("settings")
                        .document("screenTimeGoal")
                        .set(goal)
                        .addOnSuccessListener {
                            isSaving = false
                            message = "Screen-time goal saved."
                        }
                        .addOnFailureListener { error ->
                            isSaving = false
                            message = error.message
                                ?: "Could not save the goal."
                        }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isSaving) "Saving..." else "Save Goal"
                )
            }

            message?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
