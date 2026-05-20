package com.example.taller3_movil.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    // ── Evento de notificación: emite el nombre del usuario que pasó a disponible ─
    private val _notificationEvents = MutableSharedFlow<String>()
    val notificationEvents: SharedFlow<String> = _notificationEvents.asSharedFlow()

    // Estado previo de disponibilidad por uid para detectar transición false → true
    private val knownDisponible = mutableMapOf<String, Boolean>()

    private lateinit var usersRef: DatabaseReference
    private lateinit var usersChildListener: ChildEventListener

    init {
        startUsersListener()
    }

    // ── ChildEventListener: detecta cambios individuales en cada nodo de usuario ─
    private fun startUsersListener() {
        usersRef = db.child("users")

        usersChildListener = object : ChildEventListener {

            // Pobla el estado inicial sin notificar (evita falsos positivos al entrar)
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val uid = snapshot.key ?: return
                val disponible = snapshot.child("disponible").getValue(Boolean::class.java) ?: false
                knownDisponible[uid] = disponible
            }

            // Solo notifica cuando disponible cambia de false → true
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val uid = snapshot.key ?: return
                if (uid == auth.currentUser?.uid) return // Ignorar cambios propios

                val disponible = snapshot.child("disponible").getValue(Boolean::class.java) ?: false
                val wasDisponible = knownDisponible[uid] ?: false
                knownDisponible[uid] = disponible

                if (disponible && !wasDisponible) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Usuario"
                    viewModelScope.launch {
                        _notificationEvents.emit(nombre)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                knownDisponible.remove(snapshot.key)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        usersRef.addChildEventListener(usersChildListener)
    }

    // ── Limpieza del listener al destruir el ViewModel ────────────────────────
    override fun onCleared() {
        usersRef.removeEventListener(usersChildListener)
        super.onCleared()
    }

    // ── Funciones de escritura en Firebase ───────────────────────────────────
    fun initUserInDatabase() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val userRef = db.child("users").child(uid)
        userRef.child("nombre").setValue(user.displayName ?: "")
        userRef.child("photoUrl").setValue(user.photoUrl?.toString() ?: "")
        userRef.child("disponible").get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                userRef.child("disponible").setValue(false)
                userRef.child("latitud").setValue(0.0)
                userRef.child("longitud").setValue(0.0)
            }
        }
    }

    fun setDisponible(disponible: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.child("users").child(uid).child("disponible").setValue(disponible)
    }

    fun updateLocation(lat: Double, lng: Double) {
        val uid = auth.currentUser?.uid ?: return
        db.child("users").child(uid).child("latitud").setValue(lat)
        db.child("users").child(uid).child("longitud").setValue(lng)
    }
}
