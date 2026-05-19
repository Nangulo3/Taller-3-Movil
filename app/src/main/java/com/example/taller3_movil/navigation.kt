package com.example.taller3_movil

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taller3_movil.screens.*

enum class Screens {
    Login,
    Register,
    Permission,
    Map,
    Users
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.Login.name) {
        composable(route = Screens.Login.name) {
            LoginScreen(navController)
        }
        composable(route = Screens.Register.name) {
            RegisterScreen(navController)
        }
        composable(route = Screens.Permission.name) {
            LocationPermissionScreen(navController)
        }
        composable(route = Screens.Map.name) {
            MapScreen(navController)
        }
        composable(route = Screens.Users.name) {
            UsersScreen(navController)
        }
    }
}
