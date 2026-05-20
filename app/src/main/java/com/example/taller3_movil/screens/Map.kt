package com.example.taller3_movil.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.taller3_movil.Screens
import com.example.taller3_movil.model.MapViewModel
import com.example.taller3_movil.model.PuntoInteres
import com.example.taller3_movil.model.loadPuntosInteres
import com.example.taller3_movil.showUserAvailableNotification
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
fun GoogleMapsHome(
    navController: NavController,
    locations: List<PuntoInteres>,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val bogota = LatLng(4.627293, -74.063228)
    // ── disponible sincronizado con Firebase (persiste entre sesiones) ───────
    val disponible by mapViewModel.disponible.collectAsState()

    // ── 1. Launcher para solicitar POST_NOTIFICATIONS (Android 13+) ───────────
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* La notificación simplemente no se muestra si el usuario deniega */ }

    // ── 2. Al entrar: inicializar usuario en Firebase y cargar disponible ─────
    LaunchedEffect(Unit) {
        mapViewModel.initUserInDatabase()
        mapViewModel.loadDisponible()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // ── 3. Colectar eventos del SharedFlow y disparar notificación local ───────
    LaunchedEffect(mapViewModel) {
        mapViewModel.notificationEvents.collect { nombre ->
            showUserAvailableNotification(context, nombre)
        }
    }

    // ── 4. Estado de la ubicación actual ──────────────────────────────────────
    var ubicacionActual by remember { mutableStateOf(bogota) }

    // ── 5. Cliente y configuración del GPS ───────────────────────────────────
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)
            .setMinUpdateDistanceMeters(10f)
            .build()
    }

    // ── 6. Callback: actualiza estado local Y Firebase ────────────────────────
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    ubicacionActual = LatLng(loc.latitude, loc.longitude)
                    mapViewModel.updateLocation(loc.latitude, loc.longitude)
                }
            }
        }
    }

    // ── 7. DisposableEffect: arrancar y detener GPS con el ciclo de vida ──────
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

    // ── 8. Cámara y ajustes del mapa ─────────────────────────────────────────
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 15f)
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true
        )
    }

    // ── 9. Marcador dinámico de posición propia ───────────────────────────────
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
            locations.forEach { location ->
                Marker(
                    state = rememberMarkerState(position = LatLng(location.lat, location.long)),
                    title = location.nombre,
                    snippet = "Marker in ${location.nombre}"
                )
            }

            Marker(
                state = myLocationState,
                title = "Mi ubicación",
                snippet = "Posición actual",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
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
                onClick = { mapViewModel.setDisponible(!disponible) },
                label = { Text(if (disponible) "Disponible" else "No disponible") }
            )

            ElevatedButton(
                onClick = { navController.navigate(Screens.Users.name) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Usuarios")
            }

            ElevatedButton(
                onClick = {
                    mapViewModel.setDisponible(false)
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
