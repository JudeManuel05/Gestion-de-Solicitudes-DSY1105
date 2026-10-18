package cl.duoc.gestionsolicitudes.data.repository

import cl.duoc.gestionsolicitudes.model.Usuario

/** Cuentas ficticias de prueba (no se usan datos ni credenciales reales). */
class UsuarioRepository {

    private val usuarios = listOf(
        Usuario(nombre = "Cliente Demo", correo = "cliente@demo.cl", clave = "demo123"),
        Usuario(nombre = "Coordinador Demo", correo = "admin@demo.cl", clave = "demo123")
    )

    fun autenticar(correo: String, clave: String): Usuario? =
        usuarios.find { it.correo.equals(correo.trim(), ignoreCase = true) && it.clave == clave }
}
