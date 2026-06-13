package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(viewModel: BalClubViewModel) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    var isAddingUser by remember { mutableStateOf(false) }

    if (isAddingUser) {
        AddMemberScreen(
            onBack = { isAddingUser = false },
            onSave = { name, role ->
                viewModel.addUser(name, role)
                isAddingUser = false
            }
        )
    } else {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Members Directory") }) },
            floatingActionButton = {
                if (currentUserRole == Role.Sachiv || currentUserRole == Role.Admin) {
                    FloatingActionButton(onClick = { isAddingUser = true }) {
                        Icon(Icons.Filled.Add, "Add Member")
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(user.name, style = MaterialTheme.typography.titleMedium)
                                Text(user.role.toNepaliDisplay(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            if (currentUserRole == Role.Sachiv || currentUserRole == Role.Admin) {
                                IconButton(onClick = { viewModel.deleteUser(user) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Member", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(onBack: () -> Unit, onSave: (String, Role) -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.Sadasya) }
    var expandedRole by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Member") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
            ExposedDropdownMenuBox(
                expanded = expandedRole,
                onExpandedChange = { expandedRole = !expandedRole }
            ) {
                OutlinedTextField(
                    value = role.toNepaliDisplay(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedRole) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedRole,
                    onDismissRequest = { expandedRole = false }
                ) {
                    Role.values().forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r.toNepaliDisplay()) },
                            onClick = {
                                role = r
                                expandedRole = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(name, role) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save Member")
            }
        }
    }
}
