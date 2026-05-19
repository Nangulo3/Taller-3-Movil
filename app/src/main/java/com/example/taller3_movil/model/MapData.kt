package com.example.taller3_movil.model

import android.content.Context
import org.json.JSONObject

data class PuntoInteres(
    val lat: Double = 0.0,
    val long: Double = 0.0,
    val nombre: String = ""
)

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