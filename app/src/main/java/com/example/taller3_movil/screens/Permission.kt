package com.example.taller3_movil.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taller3_movil.Screens
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionScreen(navController: NavController) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    if (permissionState.status.isGranted) {
        // ── Permiso concedido: capturar GPS one-shot y escribir coordenadas en DB ─
        LaunchedEffect(Unit) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnCompleteListener { task ->
                    val loc = task.result
                    if (loc != null) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            FirebaseDatabase.getInstance().reference
                                .child("users").child(uid)
                                .updateChildren(
                                    mapOf(
                                        "latitud" to loc.latitude,
                                        "longitud" to loc.longitude
                                    )
                                )
                        }
                    }
                    navController.navigate(Screens.Map.name) {
                        popUpTo(Screens.Permission.name) { inclusive = true }
                    }
                }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Permiso de ubicación",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            val mensaje = if (permissionState.status.shouldShowRationale) {
                "Esta aplicación necesita acceso a tu ubicación GPS para mostrarte en el mapa " +
                        "y compartir tu posición en tiempo real con otros usuarios. " +
                        "Por favor, concede el permiso para continuar."
            } else {
                "Para continuar necesitamos acceso a tu ubicación GPS."
            }

            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { permissionState.launchPermissionRequest() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (permissionState.status.shouldShowRationale) "Conceder permiso"
                    else "Solicitar permiso"
                )
            }
        }
    }
}
