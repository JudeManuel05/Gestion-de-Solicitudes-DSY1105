package cl.duoc.gestionsolicitudes.model

/**
 * Estado del formulario de solicitud (lo que se está escribiendo + errores).
 * No es la tabla de la BD: la tabla es SolicitudEntity.
 */
data class SolicitudUiState(
    val id: Long = 0,                 // 0 = solicitud nueva
    val codigo: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",
    val evidenciaUri: String? = null,
    val estado: String = Catalogo.ESTADO_INICIAL,
    val fechaCreacion: Long? = null,

    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val servicioError: String? = null,

    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
) {
    val esEdicion: Boolean get() = id != 0L

    // El servicio relacionado es obligatorio "cuando corresponda" según la categoría
    val servicioObligatorio: Boolean get() = categoria !in Catalogo.categoriasSinServicio
}