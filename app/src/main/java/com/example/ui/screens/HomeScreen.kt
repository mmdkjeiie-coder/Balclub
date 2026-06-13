package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.runtime.*
import com.example.data.Role
import com.example.data.toNepaliDisplay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BalClubViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: BalClubViewModel) {
    val notices by viewModel.notices.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val members by viewModel.users.collectAsStateWithLifecycle()
    val minutes by viewModel.minutes.collectAsStateWithLifecycle()
    val role by viewModel.currentUserRole.collectAsStateWithLifecycle()
    var isAddingNotice by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    if (isAddingNotice) {
        AddNoticeScreen(
            onBack = { isAddingNotice = false },
            onSave = { title, content, isPinned ->
                viewModel.addNotice(title, content, isPinned)
                isAddingNotice = false
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Bal Club Dashboard", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Role: ${role.toNepaliDisplay()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                if (role == Role.Sachiv || role == Role.Admin) {
                    FloatingActionButton(
                        onClick = { isAddingNotice = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Filled.Add, "Add Notice")
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
                item {
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Members",
                            value = members.size.toString(),
                            icon = Icons.Filled.Group,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Events",
                            value = events.size.toString(),
                            icon = Icons.Filled.EventNote,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Minutes",
                            value = minutes.size.toString(),
                            icon = Icons.Filled.LibraryBooks,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Notice Board", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }

                if (notices.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("No notices posted yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    val sortedNotices = notices.sortedByDescending { it.date }.sortedByDescending { it.isPinned }
                    items(sortedNotices, key = { it.id }) { notice ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (notice.isPinned) 4.dp else 1.dp),
                            colors = if (notice.isPinned) 
                                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) 
                            else 
                                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(notice.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = if (notice.isPinned) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (notice.isPinned) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.padding(start = 8.dp)
                                            ) {
                                                Text("📌 Pinned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        if (role == Role.Sachiv || role == Role.Admin) {
                                            IconButton(onClick = { viewModel.deleteNotice(notice) }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete Notice", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(notice.content, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    dateFormat.format(Date(notice.date)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (notice.isPinned) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoticeScreen(onBack: () -> Unit, onSave: (String, String, Boolean) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Notice") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = { Text("Notice Title") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = content, 
                onValueChange = { content = it }, 
                label = { Text("Content") }, 
                modifier = Modifier.fillMaxWidth().weight(1f), 
                shape = RoundedCornerShape(12.dp)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pin this notice to top", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(
                onClick = { onSave(title, content, isPinned) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = title.isNotBlank() && content.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Publish Notice", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
