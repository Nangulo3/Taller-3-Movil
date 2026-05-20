package com.example.taller3_movil.screens

import android.annotation.SuppressLint
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taller3_movil.model.TrackingViewModel
import com.example.taller3_movil.model.haversineDistance
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@SuppressLint("MissingPermission")
@Composable
fun TrackingMapScreen(
    navController: NavController,
    userId: String,
    trackingViewModel: TrackingViewModel = viewModel()
) {
    val context = LocalContext.current

    // ── 1. Iniciar escucha de Firebase para el usuario seguido ────────────────
    LaunchedEffect(userId) {
        trackingViewModel.startTracking(userId)
    }

    val trackedUser by trackingViewModel.trackedUser.collectAsState()

    // ── 2. Posición propia del tracker ────────────────────────────────────────
    var myLocation by remember { mutableStateOf(LatLng(4.627293, -74.063228)) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)
            .setMinUpdateDistanceMeters(10f)
            .build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    myLocation = LatLng(loc.latitude, loc.longitude)
                }
            }
        }
    }

    // ── 3. DisposableEffect: arrancar/detener GPS con el ciclo de vida ────────
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

    // ── 4. Posición del usuario seguido (reactiva desde Firebase) ─────────────
    val trackedPosition = trackedUser?.let { LatLng(it.latitud, it.longitud) }

    // ── 5. Distancia reactiva: se recalcula si cambia myLocation o trackedUser ─
    val distancia = trackedUser?.let { user ->
        haversineDistance(myLocation.latitude, myLocation.longitude, user.latitud, user.longitud)
    }

    // ── 6. Marcador dinámico — actualiza posición sin recargar el mapa ────────
    val trackedMarkerState = rememberMarkerState(position = LatLng(0.0, 0.0))
    LaunchedEffect(trackedPosition) {
        trackedPosition?.let { trackedMarkerState.position = it }
    }

    // ── 7. Cámara: se mueve a la posición del seguido al cargar ───────────────
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.627293, -74.063228), 15f)
    }
    var cameraInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(trackedPosition) {
        if (trackedPosition != null && !cameraInitialized) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(trackedPosition, 15f)
            cameraInitialized = true
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = true, compassEnabled = true)
        ) {
            if (trackedPosition != null) {
                Marker(
                    state = trackedMarkerState,
                    title = trackedUser?.nombre ?: "Usuario",
                    snippet = "Ubicación en tiempo real"
                )
            }
        }

        // ── Tarjeta superpuesta con distancia (Modifier.align sobre el Box) ──
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.65f)
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = trackedUser?.nombre ?: "Cargando...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (distancia != null) "Distancia: $distancia km" else "Calculando distancia...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ElevatedButton(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
        }
    }
}
