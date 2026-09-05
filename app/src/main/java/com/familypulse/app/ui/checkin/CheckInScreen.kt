package com.familypulse.app.ui.checkin

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.models.ManualCheckIn
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    repo: FirebaseRepository,
    familyId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    var note by remember { mutableStateOf("") }
    var includeLocation by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                scope.launch {
                    sendCheckIn(
                        repo,
                        familyId,
                        auth,
                        note,
                        locationClient,
                        true,
                        { isSending = it },
                        { message = it }
                    )
                }
            } else {
                message = "Location permission was not granted."
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Check-In") },
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
                "Let your family know you're safe.",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Status note") },
                placeholder = { Text("I'm home and doing well.") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Include my location",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Optional. Location is shared with your family."
                    )
                }

                Switch(
                    checked = includeLocation,
                    onCheckedChange = { includeLocation = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            message?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {

                    if (includeLocation) {

                        val fineGranted =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                        val coarseGranted =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                        if (fineGranted || coarseGranted) {

                            scope.launch {
                                sendCheckIn(
                                    repo,
                                    familyId,
                                    auth,
                                    note,
                                    locationClient,
                                    true,
                                    { isSending = it },
                                    { message = it }
                                )
                            }

                        } else {

                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }

                    } else {

                        scope.launch {
                            sendCheckIn(
                                repo,
                                familyId,
                                auth,
                                note,
                                locationClient,
                                false,
                                { isSending = it },
                                { message = it }
                            )
                        }
                    }
                },
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isSending) "Sending..." else "Send Check-In"
                )
            }
        }
    }
}

private suspend fun sendCheckIn(
    repo: FirebaseRepository,
    familyId: String,
    auth: FirebaseAuth,
    note: String,
    locationClient: com.google.android.gms.location.FusedLocationProviderClient,
    includeLocation: Boolean,
    onSendingChanged: (Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    try {
        onSendingChanged(true)

        val uid = auth.currentUser?.uid

        if (uid == null) {
            onMessage("Please log in again.")
            return
        }

        val userDocument =
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

        val userName =
            userDocument.getString("name") ?: "Family member"

        var latitude: Double? = null
        var longitude: Double? = null

        if (includeLocation) {
            try {
                val location = locationClient.lastLocation.await()
                latitude = location?.latitude
                longitude = location?.longitude
            } catch (_: Exception) {
                // Check-in can still be sent without location.
            }
        }

        val checkIn = ManualCheckIn(
            userId = uid,
            userName = userName,
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            note = note.trim()
        )

        repo.sendCheckIn(
            familyId,
            checkIn
        )

        onMessage("Check-in sent successfully.")

    } catch (e: Exception) {
        onMessage(
            e.message ?: "Could not send check-in."
        )
    } finally {
        onSendingChanged(false)
    }
}
