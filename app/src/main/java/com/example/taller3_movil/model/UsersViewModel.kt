package com.example.taller3_movil.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsersViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val _users = MutableStateFlow<List<UsuarioDisponible>>(emptyList())
    val users: StateFlow<List<UsuarioDisponible>> = _users.asStateFlow()

    private lateinit var usersRef: DatabaseReference
    private var usersListener: ValueEventListener? = null

    init {
        loadAvailableUsers()
    }

    private fun loadAvailableUsers() {
        val currentUid = auth.currentUser?.uid
        usersRef = db.child("users")
        usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<UsuarioDisponible>()
                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    if (uid == currentUid) continue
                    val disponible = child.child("disponible").getValue(Boolean::class.java) ?: false
                    if (!disponible) continue
                    val nombre = child.child("nombre").getValue(String::class.java) ?: ""
                    val photoUrl = child.child("photoUrl").getValue(String::class.java) ?: ""
                    val latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0
                    val longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0
                    list.add(UsuarioDisponible(uid, nombre, photoUrl, latitud, longitud))
                }
                _users.value = list
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        usersRef.addValueEventListener(usersListener!!)
    }

    override fun onCleared() {
        usersListener?.let { usersRef.removeEventListener(it) }
        super.onCleared()
    }
}
