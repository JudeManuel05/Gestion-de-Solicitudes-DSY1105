package cl.duoc.gestionsolicitudes.model

/** Valores fijos del caso (categorías sugeridas y estado inicial). */
object Catalogo {

    const val ESTADO_INICIAL = "Recibida"

    val categorias = listOf(
        "Desarrollo web",
        "Mantenimiento web",
        "Incidente técnico",
        "Hosting",
        "Soporte de correo",
        "Marketing digital",
        "Solicitud de cambio",
        "Consulta",
        "Acceso o configuración",
        "Otro requerimiento"
    )

    // En estas categorías el servicio relacionado NO es obligatorio
    val categoriasSinServicio = setOf("Consulta", "Otro requerimiento")
}
