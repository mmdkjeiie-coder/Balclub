package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.BalClubRepository
import com.example.data.BalClubViewModel
import com.example.ui.BalClubViewModelFactory
import com.example.ui.Screen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.*
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            if (BuildConfig.FIREBASE_PROJECT_ID.isNotEmpty() && BuildConfig.FIREBASE_PROJECT_ID != "mock-project") {
                val options = FirebaseOptions.Builder()
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .setApplicationId(BuildConfig.FIREBASE_APPLICATION_ID)
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val repository = BalClubRepository.getInstance(applicationContext)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: BalClubViewModel = viewModel(
                    factory = BalClubViewModelFactory(repository)
                )

                // Initialize some data if it's empty during development
                LaunchedEffect(Unit) {
                    viewModel.seedData()
                }

                BalClubApp(navController, viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalClubApp(navController: NavHostController, viewModel: BalClubViewModel) {
    val items = listOf(
        Screen.Home,
        Screen.Minutes,
        Screen.Events,
        Screen.Members,
        Screen.More
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    LaunchedEffect(loginState) {
        if (loginState is com.example.data.LoginState.Idle && currentRoute != "login") {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Don't show bottom bar on login
    val showBottomBar = currentRoute != "login"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { 
                LoginScreen(viewModel) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            composable(Screen.Home.route) { HomeScreen(viewModel) }
            composable(Screen.Minutes.route) { MinutesScreen(viewModel) }
            composable(Screen.Events.route) { EventsScreen(viewModel) }
            composable(Screen.Members.route) { MembersScreen(viewModel) }
            composable(Screen.More.route) { 
                MoreScreen(
                    viewModel = viewModel, 
                    onNavigateToInviteCodes = { navController.navigate("invite_codes") },
                    onNavigateToPhotoGallery = { navController.navigate("photo_gallery") },
                    onNavigateToSuggestions = { navController.navigate("suggestions") }
                ) 
            }
            composable("invite_codes") {
                ManageInviteCodesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("suggestions") {
                SuggestionsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("photo_gallery") {
                PhotoGalleryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
