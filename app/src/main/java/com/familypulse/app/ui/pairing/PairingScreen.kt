package com.familypulse.app.ui.pairing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PairingScreen(
    repo: FirebaseRepository,
    onPairingComplete: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Family Pairing",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Enter a 6-digit family code.")

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = code,
            onValueChange = {
                code = it.filter { char -> char.isDigit() }.take(6)
                message = ""
            },
            label = { Text("Family Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val uid = auth.currentUser?.uid

                if (uid == null) {
                    message = "Please log in first."
                    return@Button
                }

                if (code.length != 6) {
                    message = "Please enter a 6-digit code."
                    return@Button
                }

                loading = true

                db.collection("families")
                    .document(code)
                    .get()
                    .addOnSuccessListener { family ->
                        if (!family.exists()) {
                            loading = false
                            message = "Family code not found."
                            return@addOnSuccessListener
                        }

                        db.collection("users")
                            .document(uid)
                            .update("familyId", code)
                            .addOnSuccessListener {
                                loading = false
                                message = "Family joined successfully!"
                            }
                            .addOnFailureListener {
                                loading = false
                                message = it.message ?: "Could not join family."
                            }
                    }
                    .addOnFailureListener {
                        loading = false
                        message = it.message ?: "Connection failed."
                    }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Joining..." else "Join Family")
        }

        Spacer(modifier = Modifier.height(12.dp))
Button(
    onClick = {
        loading = true
        message = ""

        scope.launch {
            try {
                val newCode = repo.createFamilyCode()

                db.collection("users")
                    .document(auth.currentUser?.uid ?: "")
                    .update("familyId", newCode)

                code = newCode
                message = "Family created! Your code is: $newCode"
            } catch (e: Exception) {
                message = e.message ?: "Could not create family."
            } finally {
                loading = false
            }
        }
    },
    enabled = !loading,
    modifier = Modifier.fillMaxWidth()
) {
    Text("Create Family Code")
}

Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}
