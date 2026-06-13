package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinutesScreen(viewModel: BalClubViewModel) {
    val minutes by viewModel.minutes.collectAsStateWithLifecycle()
    val role by viewModel.currentUserRole.collectAsStateWithLifecycle()
    var isAddingMinute by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    if (isAddingMinute) {
        AddMinuteScreen(
            onBack = { isAddingMinute = false },
            onSave = { minute ->
                viewModel.addMinute(minute)
                isAddingMinute = false
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Digital Minutes", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                if (role == Role.Sachiv || role == Role.Admin) {
                    FloatingActionButton(
                        onClick = { isAddingMinute = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Filled.Add, "Add Minute")
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (minutes.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Article, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No minutes recorded yet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                val sortedMinutes = minutes.sortedByDescending { it.date }
                items(sortedMinutes, key = { it.id }) { minute ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(minute.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        minute.template.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                dateFormat.format(Date(minute.date)),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                minute.summary.ifBlank { "No summary provided." },
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMinuteScreen(onBack: () -> Unit, onSave: (Minute) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(MinuteTemplate.Standard) }
    var chairperson by remember { mutableStateOf("") }
    var attendees by remember { mutableStateOf("") }
    var agenda by remember { mutableStateOf("") }
    var discussion by remember { mutableStateOf("") }
    var decisions by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }

    var expandedTemplate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Minute") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedTemplate,
                    onExpandedChange = { expandedTemplate = !expandedTemplate }
                ) {
                    OutlinedTextField(
                        value = selectedTemplate.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Meeting Template") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTemplate) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTemplate,
                        onDismissRequest = { expandedTemplate = false }
                    ) {
                        MinuteTemplate.values().forEach { template ->
                            DropdownMenuItem(
                                text = { Text(template.name) },
                                onClick = {
                                    selectedTemplate = template
                                    expandedTemplate = false
                                }
                            )
                        }
                    }
                }
            }
            item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Meeting Title/Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
            item { OutlinedTextField(value = chairperson, onValueChange = { chairperson = it }, label = { Text("Chairperson & Secretary") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
            item { OutlinedTextField(value = attendees, onValueChange = { attendees = it }, label = { Text("Attendees") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
            item { OutlinedTextField(value = agenda, onValueChange = { agenda = it }, label = { Text("Agenda") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
            item { OutlinedTextField(value = discussion, onValueChange = { discussion = it }, label = { Text("Discussion Points") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp)) }
            item { OutlinedTextField(value = decisions, onValueChange = { decisions = it }, label = { Text("Assigned Decisions") }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp)) }
            item { OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Conclusion & Summary") }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp)) }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                Button(
                    onClick = {
                        onSave(
                            Minute(
                                template = selectedTemplate,
                                title = title,
                                date = System.currentTimeMillis(),
                                chairperson = chairperson,
                                attendees = attendees,
                                agenda = agenda,
                                discussion = discussion,
                                decisions = decisions,
                                summary = summary
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = title.isNotBlank()
                ) {
                    Text("Save Minute", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
