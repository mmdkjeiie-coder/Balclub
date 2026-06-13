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
import com.example.data.Role
import com.example.data.toNepaliDisplay
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BalClubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageInviteCodesScreen(viewModel: BalClubViewModel, onBack: () -> Unit) {
    var isAddingCode by remember { mutableStateOf(false) }
    val codes by viewModel.inviteCodes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchInviteCodes()
    }

    if (isAddingCode) {
        AddInviteCodeScreen(
            onBack = { isAddingCode = false },
            onSave = { code, role ->
                viewModel.addInviteCode(code, role)
                isAddingCode = false
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Manage Invite Codes") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { isAddingCode = true }) {
                    Icon(Icons.Filled.Add, "Add Code")
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
                item {
                    Text("These codes are saved in Firebase Firestore (mocked for preview).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(codes) { codePair ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Code: ${codePair.first}", style = MaterialTheme.typography.titleMedium)
                                Text("Role: ${codePair.second.toNepaliDisplay()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteInviteCode(codePair.first) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
fun AddInviteCodeScreen(onBack: () -> Unit, onSave: (String, Role) -> Unit) {
    var code by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.Sadasya) }
    var expandedRole by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Invite Code") },
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
            OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Invite Code") }, modifier = Modifier.fillMaxWidth())
            ExposedDropdownMenuBox(
                expanded = expandedRole,
                onExpandedChange = { expandedRole = !expandedRole }
            ) {
                OutlinedTextField(
                    value = role.toNepaliDisplay(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assigned Role") },
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
                onClick = { onSave(code, role) },
                modifier = Modifier.fillMaxWidth(),
                enabled = code.isNotBlank()
            ) {
                Text("Save Code to Firestore")
            }
        }
    }
}
