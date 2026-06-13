package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BalClubViewModel
import com.example.data.Role
import com.example.data.toNepaliDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: BalClubViewModel, 
    onNavigateToInviteCodes: () -> Unit,
    onNavigateToPhotoGallery: () -> Unit,
    onNavigateToSuggestions: () -> Unit
) {
    val role by viewModel.currentUserRole.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Menu", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text("Current Role", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text(role.toNepaliDisplay(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Text("Features", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp, top = 8.dp))

            MoreFeatureItem(
                icon = Icons.Filled.PhotoAlbum, 
                title = "Photo Gallery", 
                subtitle = "View event photos",
                onClick = onNavigateToPhotoGallery
            )
            MoreFeatureItem(
                icon = Icons.Filled.HowToVote, 
                title = "Voting & Suggestions", 
                subtitle = "Submit and vote on ideas",
                onClick = onNavigateToSuggestions
            )
            
            if (role == Role.Admin || role == Role.Sachiv) {
                MoreFeatureItem(
                    icon = Icons.Filled.VpnKey, 
                    title = "Manage Invite Codes", 
                    subtitle = "Generate Firebase Codes",
                    onClick = onNavigateToInviteCodes
                )
            }

            MoreFeatureItem(icon = Icons.Filled.Settings, title = "Settings", subtitle = "App preferences")
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            MoreFeatureItem(icon = Icons.Filled.Info, title = "About App", subtitle = "Bal Club Digital Minutes v1.0")
            
            MoreFeatureItem(
                icon = Icons.Filled.ExitToApp, 
                title = "Logout", 
                subtitle = "Sign out of your account",
                onClick = { viewModel.logout() },
                iconColor = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Made with love by Pratik Nepal Sachiv 2083", 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MoreFeatureItem(
    icon: ImageVector, 
    title: String, 
    subtitle: String, 
    onClick: () -> Unit = {}, 
    iconColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp), tint = iconColor)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
