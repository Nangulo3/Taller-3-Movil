package com.example.taller3_movil.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taller3_movil.Screens
import com.example.taller3_movil.model.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by authViewModel.loginState.collectAsState()

    LaunchedEffect(Unit) {
        if (authViewModel.auth.currentUser != null) {
            navController.navigate(Screens.Permission.name) {
                popUpTo(Screens.Login.name) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val emailError = uiState.emailError
            TextField(
                value = uiState.email,
                onValueChange = authViewModel::onLoginEmailChange,
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
                isError = emailError != null,
                supportingText = if (emailError != null) ({ Text(emailError) }) else null,
                singleLine = true
            )

            val passwordError = uiState.passwordError
            TextField(
                value = uiState.password,
                onValueChange = authViewModel::onLoginPasswordChange,
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = passwordError != null,
                supportingText = if (passwordError != null) ({ Text(passwordError) }) else null,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    authViewModel.login(
                        onSuccess = {
                            navController.navigate(Screens.Permission.name) {
                                popUpTo(Screens.Login.name) { inclusive = true }
                            }
                        },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Entrar")
                }
            }

            TextButton(
                onClick = { navController.navigate(Screens.Register.name) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}
