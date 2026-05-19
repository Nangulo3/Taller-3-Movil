package com.example.taller3_movil.screens

import android.annotation.SuppressLint
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taller3_movil.Screens
import com.example.taller3_movil.model.PuntoInteres
import com.example.taller3_movil.model.loadPuntosInteres
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val locations = remember { loadPuntosInteres(context) }
    GoogleMapsHome(navController, locations)
}

@SuppressLint("MissingPermission")
@Composable
fun GoogleMapsHome(navController: NavController, locations: List<PuntoInteres>) {
    val context = LocalContext.current
    val bogota = LatLng(4.627293, -74.063228)
    var disponible by remember { mutableStateOf(false) }

    // ── 1. Estado de la ubicación actual ──────────────────────────────────────
    var ubicacionActual by remember { mutableStateOf(bogota) }

    // ── 2. Cliente y configuración del GPS ───────────────────────────────────
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)
            .setMinUpdateDistanceMeters(10f)
            .build()
    }

    // ── 3. Callback que actualiza el estado ──────────────────────────────────
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    ubicacionActual = LatLng(loc.latitude, loc.longitude)
                }
            }
        }
    }

    // ── 4. DisposableEffect: arrancar y detener updates con el ciclo de vida ─
    DisposableEffect(Unit) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // ── 5. Cámara y ajustes del mapa ─────────────────────────────────────────
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 15f)
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true
        )
    }

    // ── 6. Marcador dinámico — equivalente a rememberUpdatedMarkerState ───────
    // rememberUpdatedMarkerState no existe en maps-compose 4.x; este patrón
    // tiene el mismo comportamiento: el pin se mueve cada vez que cambia ubicacionActual.
    val myLocationState = rememberMarkerState(position = ubicacionActual)
    LaunchedEffect(ubicacionActual) {
        myLocationState.position = ubicacionActual
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings
        ) {
            // Marcadores estáticos del JSON
            locations.forEach { location ->
                Marker(
                    state = rememberMarkerState(position = LatLng(location.lat, location.long)),
                    title = location.nombre,
                    snippet = "Marker in ${location.nombre}"
                )
            }

            // ── Marcador dinámico (posición del usuario) ──────────────────
            Marker(
                state = myLocationState,
                title = "Mi ubicación",
                snippet = "Posición actual"
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 12.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = disponible,
                onClick = { disponible = !disponible },
                label = { Text(if (disponible) "Disponible" else "No disponible") }
            )

            ElevatedButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screens.Login.name) {
                        popUpTo(Screens.Map.name) { inclusive = true }
                    }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
