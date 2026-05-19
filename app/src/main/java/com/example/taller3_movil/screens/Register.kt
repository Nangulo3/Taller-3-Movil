package com.example.taller3_movil.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taller3_movil.Screens
import com.example.taller3_movil.model.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by authViewModel.registerState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Registro",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { /* Abrir selector de imagen */ },
            contentAlignment = Alignment.Center
        ) {
            Text("Foto")
        }

        Spacer(modifier = Modifier.height(8.dp))

        val nombreError = uiState.nombreError
        TextField(
            value = uiState.nombre,
            onValueChange = authViewModel::onRegisterNombreChange,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            isError = nombreError != null,
            supportingText = if (nombreError != null) ({ Text(nombreError) }) else null,
            singleLine = true
        )

        val apellidoError = uiState.apellidoError
        TextField(
            value = uiState.apellido,
            onValueChange = authViewModel::onRegisterApellidoChange,
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            isError = apellidoError != null,
            supportingText = if (apellidoError != null) ({ Text(apellidoError) }) else null,
            singleLine = true
        )

        TextField(
            value = uiState.identificacion,
            onValueChange = authViewModel::onRegisterIdentificacionChange,
            label = { Text("Número de identificación") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        val emailError = uiState.emailError
        TextField(
            value = uiState.email,
            onValueChange = authViewModel::onRegisterEmailChange,
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            supportingText = if (emailError != null) ({ Text(emailError) }) else null,
            singleLine = true
        )

        val passwordError = uiState.passwordError
        TextField(
            value = uiState.password,
            onValueChange = authViewModel::onRegisterPasswordChange,
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
                authViewModel.register(
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
                Text("Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
