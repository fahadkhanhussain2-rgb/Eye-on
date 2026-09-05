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
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    repo: FirebaseRepository,
    familyId: String,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf<List<FamilyTask>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun loadTasks() {
        if (familyId.isBlank()) return

        repo.getTasks(familyId)
            .get()
            .addOnSuccessListener { result ->
                tasks = result.documents.mapNotNull { document ->
                    document.toObject(FamilyTask::class.java)?.copy(
                        id = document.id
                    )
                }
            }
            .addOnFailureListener {
                error = it.message ?: "Could not load tasks."
            }
    }

    LaunchedEffect(familyId) {
        loadTasks()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Family Tasks",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (familyId.isBlank()) {
            Text(
                text = "Family is not paired yet.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                error = ""
            },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                error = ""
            },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (familyId.isBlank()) {
                    error = "Family is not paired yet."
                    return@Button
                }

                if (title.isBlank()) {
                    error = "Please enter a task title."
                    return@Button
                }

                loading = true
                error = ""

                scope.launch {
                    try {
                        repo.addTask(
                            familyId = familyId,
                            task = FamilyTask(
                                title = title.trim(),
                                description = description.trim()
                            )
                        )

                        title = ""
                        description = ""

                        loadTasks()
                    } catch (e: Exception) {
                        error = e.message ?: "Could not add task."
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading && familyId.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Adding..." else "Add Task")
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (tasks.isEmpty()) {
            Text("No tasks added yet.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(tasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (task.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(task.description)
                            }
                        }
                    }
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
