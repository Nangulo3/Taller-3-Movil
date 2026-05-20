package com.example.taller3_movil.model

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrackingViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference

    private val _trackedUser = MutableStateFlow<UsuarioDisponible?>(null)
    val trackedUser: StateFlow<UsuarioDisponible?> = _trackedUser.asStateFlow()

    private var trackingRef: DatabaseReference? = null
    private var trackingListener: ValueEventListener? = null

    fun startTracking(userId: String) {
        trackingRef?.let { ref -> trackingListener?.let { ref.removeEventListener(it) } }

        trackingRef = db.child("users").child(userId)
        trackingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                val photoUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: ""
                val latitud = snapshot.child("latitud").getValue(Double::class.java) ?: 0.0
                val longitud = snapshot.child("longitud").getValue(Double::class.java) ?: 0.0
                _trackedUser.value = UsuarioDisponible(userId, nombre, photoUrl, latitud, longitud)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        trackingRef!!.addValueEventListener(trackingListener!!)
    }

    override fun onCleared() {
        trackingRef?.let { ref -> trackingListener?.let { ref.removeEventListener(it) } }
        super.onCleared()
    }
}
