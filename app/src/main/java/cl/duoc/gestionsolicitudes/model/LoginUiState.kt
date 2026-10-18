package cl.duoc.gestionsolicitudes.model

/** Todo lo que la pantalla de login necesita mostrar. null en un error = campo válido. */
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val correoError: String? = null,
    val claveError: String? = null,
    val mensajeError: String? = null,
    val cargando: Boolean = false,
    val loginExitoso: Boolean = false,
    val usuarioNombre: String = ""
)