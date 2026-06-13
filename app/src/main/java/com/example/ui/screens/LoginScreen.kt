package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.BalClubViewModel
import com.example.data.LoginState
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: BalClubViewModel, onLoginSuccess: () -> Unit) {
    var code by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bal Club Login") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to Bal Club", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Enter Invite Code") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.loginWithCode(code) },
                modifier = Modifier.fillMaxWidth(),
                enabled = code.isNotBlank() && loginState !is LoginState.Loading
            ) {
                Text(if (loginState is LoginState.Loading) "Authenticating..." else "Login")
            }
            if (loginState is LoginState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text((loginState as LoginState.Error).message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
