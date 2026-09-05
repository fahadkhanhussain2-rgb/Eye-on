package com.familypulse.app.ui.members

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.models.UserProfile
import kotlinx.coroutines.launch

@Composable
fun FamilyMembersScreen(
    repo: FirebaseRepository,
    familyId: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var members by remember {
        mutableStateOf<List<UserProfile>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    fun loadMembers() {

        if (familyId.isBlank()) {
            loading = false
            return
        }

        loading = true
        error = ""

        scope.launch {
            try {

                members = repo.getFamilyMembers(familyId)

            } catch (e: Exception) {

                error = e.message
                    ?: "Could not load family members."

            } finally {

                loading = false
            }
        }
    }

    LaunchedEffect(familyId) {
        loadMembers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Family Members",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (familyId.isNotBlank()) {

            Text(
                text = "Family Code: $familyId",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (familyId.isBlank()) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        "No family connected yet.",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        "Create or join a family to see its members."
                    )
                }
            }

        } else if (loading) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator()
            }

        } else if (error.isNotBlank()) {

            Column {

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { loadMembers() }
                ) {
                    Text("Try Again")
                }
            }

        } else if (members.isEmpty()) {

            Text("No family members found.")

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                items(
                    items = members,
                    key = { it.uid }
                ) { member ->

                    MemberCard(member)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun MemberCard(
    member: UserProfile
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = member.name
                            .firstOrNull()
                            ?.uppercase()
                            ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = member.name.ifBlank {
                        "Family Member"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = member.role,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
