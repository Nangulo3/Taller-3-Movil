package com.example.taller3_movil.model

import android.content.Context
import org.json.JSONObject
import kotlin.math.*

data class PuntoInteres(
    val lat: Double = 0.0,
    val long: Double = 0.0,
    val nombre: String = ""
)

data class UsuarioDisponible(
    val uid: String = "",
    val nombre: String = "",
    val photoUrl: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)

private const val RADIUS_OF_EARTH_KM = 6371.0

fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val result = RADIUS_OF_EARTH_KM * c
    return Math.round(result * 100.0) / 100.0
}

fun loadPuntosInteres(context : Context) : MutableList<PuntoInteres> {
    val locations = mutableListOf<PuntoInteres>()
    val json_string = context.assets.open("locations.json").bufferedReader().use {
        it.readText()
    }
    var json = JSONObject(json_string)
    var PuntosJsonArray = json.getJSONArray("locations")
    for (i in 0..PuntosJsonArray.length()-1) {
    val jsonObject = PuntosJsonArray.getJSONObject(i)
        val lat = jsonObject.getString("latitude")
        val latitude = lat.toDouble()
        val long = jsonObject.getString("longitude")
        val longitude = long.toDouble()
        val name = jsonObject.getString("name")
        val location = PuntoInteres(latitude, longitude, name);
        locations.add(location)
}
    return locations
}