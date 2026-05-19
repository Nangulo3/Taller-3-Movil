package com.example.taller3_movil.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false
)

data class RegisterUiState(
    val nombre: String = "",
    val apellido: String = "",
    val identificacion: String = "",
    val email: String = "",
    val password: String = "",
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel : ViewModel() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // --- Login ¿

    fun onLoginEmailChange(value: String) =
        _loginState.update { it.copy(email = value, emailError = null) }

    fun onLoginPasswordChange(value: String) =
        _loginState.update { it.copy(password = value, passwordError = null) }

    // --- Register

    fun onRegisterNombreChange(value: String) =
        _registerState.update { it.copy(nombre = value, nombreError = null) }

    fun onRegisterApellidoChange(value: String) =
        _registerState.update { it.copy(apellido = value, apellidoError = null) }

    fun onRegisterIdentificacionChange(value: String) =
        _registerState.update { it.copy(identificacion = value) }

    fun onRegisterEmailChange(value: String) =
        _registerState.update { it.copy(email = value, emailError = null) }

    fun onRegisterPasswordChange(value: String) =
        _registerState.update { it.copy(password = value, passwordError = null) }

    // --- Validation ---

    private fun validateEmail(email: String): String? =
        if (!emailRegex.matches(email)) "Formato de correo inválido" else null

    private fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "La contraseña no puede estar vacía"
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    // --- Firebase operations ---

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _loginState.value
        val emailErr = validateEmail(state.email)
        val passErr = validatePassword(state.password)

        if (emailErr != null || passErr != null) {
            _loginState.update { it.copy(emailError = emailErr, passwordError = passErr) }
            return
        }

        _loginState.update { it.copy(isLoading = true) }
        auth.signInWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                _loginState.update { it.copy(isLoading = false) }
                if (task.isSuccessful) onSuccess()
                else onError(task.exception?.message ?: "Error al iniciar sesión")
            }
    }

    fun register(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _registerState.value
        val emailErr = validateEmail(state.email)
        val passErr = validatePassword(state.password)
        val nombreErr = if (state.nombre.isBlank()) "El nombre es requerido" else null
        val apellidoErr = if (state.apellido.isBlank()) "El apellido es requerido" else null

        if (emailErr != null || passErr != null || nombreErr != null || apellidoErr != null) {
            _registerState.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passErr,
                    nombreError = nombreErr,
                    apellidoError = apellidoErr
                )
            }
            return
        }

        _registerState.update { it.copy(isLoading = true) }
        auth.createUserWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                _registerState.update { it.copy(isLoading = false) }
                if (task.isSuccessful) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName("${state.nombre} ${state.apellido}")
                        .setPhotoUri(Uri.parse("https://ui-avatars.com/api/?name=${state.nombre}+${state.apellido}"))
                        .build()
                    task.result.user
                        ?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { onSuccess() }
                        ?: onSuccess()
                } else {
                    onError(task.exception?.message ?: "Error al registrar usuario")
                }
            }
    }
}
