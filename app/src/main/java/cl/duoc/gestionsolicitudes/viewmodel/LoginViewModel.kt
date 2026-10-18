package cl.duoc.gestionsolicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.gestionsolicitudes.data.repository.UsuarioRepository
import cl.duoc.gestionsolicitudes.model.LoginUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = UsuarioRepository()

    // Privado y mutable dentro del ViewModel; público y de solo lectura para la View
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, correoError = null, mensajeError = null) }
    }

    fun onClaveChange(valor: String) {
        _uiState.update { it.copy(clave = valor, claveError = null, mensajeError = null) }
    }

    // REGLAS DEL NEGOCIO: la validación vive aquí, no en la pantalla
    fun validarFormulario(): Boolean {
        val estado = _uiState.value
        val correoError = when {
            estado.correo.isBlank() -> "El correo es obligatorio"
            !EMAIL_REGEX.matches(estado.correo.trim()) -> "Formato de correo inválido"
            else -> null
        }
        val claveError = when {
            estado.clave.isBlank() -> "La contraseña es obligatoria"
            estado.clave.length < 6 -> "Debe tener al menos 6 caracteres"
            else -> null
        }
        _uiState.update { it.copy(correoError = correoError, claveError = claveError) }
        return correoError == null && claveError == null
    }

    fun iniciarSesion() {
        if (!validarFormulario()) return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            delay(700) // simula la verificación de la cuenta
            val usuario = repository.autenticar(_uiState.value.correo, _uiState.value.clave)
            _uiState.update {
                if (usuario != null) {
                    it.copy(cargando = false, loginExitoso = true, usuarioNombre = usuario.nombre)
                } else {
                    it.copy(cargando = false, mensajeError = "Correo o contraseña incorrectos")
                }
            }
        }
    }

    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}