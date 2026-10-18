package cl.duoc.gestionsolicitudes.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "solicitudes" de Room. Cada propiedad es una columna.
 * El código (SOL-XXXXXXXX) es único gracias al índice unique.
 */
@Entity(
    tableName = "solicitudes",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,
    val evidenciaUri: String?,
    val estado: String,
    val fechaCreacion: Long,
    val fechaActualizacion: Long
)
