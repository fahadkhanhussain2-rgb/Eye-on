package com.familypulse.app.ui.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    repo: FirebaseRepository,
    onPairingComplete: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    var familyCodeInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var successText by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Pairing") },
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
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                text = "Link Your Family",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter an existing family code or create a new group.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = familyCodeInput,
                onValueChange = {
                    familyCodeInput = it.uppercase().take(6)
                    errorText = null
                },
                label = { Text("6-Digit Family Code") },
                placeholder = { Text("XXXXXX") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {

                Button(
                    onClick = {
                        if (familyCodeInput.length != 6) {
                            errorText = "Please enter a valid 6-digit code."
                            return@Button
                        }

                        val uid = auth.currentUser?.uid

                        if (uid == null) {
                            errorText = "Please log in first."
                            return@Button
                        }

                        isLoading = true
                        errorText = null

                        db.collection("families")
                            .document(familyCodeInput)
                            .get()
                            .addOnSuccessListener { document ->

                                if (!document.exists()) {
                                    errorText =
                                        "Family code not found. Please verify the code."
                                    isLoading = false
                                    return@addOnSuccessListener
                                }

                                db.collection("users")
                                    .document(uid)
                                    .update("familyId", familyCodeInput)
                                    .addOnSuccessListener {

                                        scope.launch {
                                            val profile = repo.getUserProfile(uid)

                                            if (profile != null) {
                                                successText =
                                                    "Joined family successfully!"
                                                isLoading = false
                                                onPairingComplete(profile)
                                            } else {
                                                errorText =
                                                    "Could not load your profile."
                                               
