package com.familypulse.app.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familypulse.app.data.FirebaseRepository
import com.familypulse.app.models.FamilyTask
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    repo: FirebaseRepository,
    familyId: String,
    onBack: () -> Unit
) {
    var tasks by remember { mutableStateOf<List<FamilyTask>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()

    LaunchedEffect(familyId) {
        repo.getTasks(familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = error.message
                    return@addSnapshotListener
                }

                tasks = snapshot?.documents?.mapNotNull {
                    it.toObject(FamilyTask::class.java)?.copy(id = it.id)
                } ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Tasks") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Text(
                    "Add a Family Task",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Please enter a task title."
                            return@Button
                        }

                        val uid = auth.currentUser?.uid ?: return@Button

                        val task = FamilyTask(
                            title = title.trim(),
                            description = description.trim(),
                            assignedTo = uid
                        )

                        repo.addTask(familyId, task)

                        title = ""
                        description = ""
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Task")
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Tasks",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.description)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            if (task.isCompleted) "Completed" else "Pending"
                        )
                    }
                }
            }
        }
    }
}
