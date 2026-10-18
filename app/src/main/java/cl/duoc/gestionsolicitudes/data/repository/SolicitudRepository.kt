package cl.duoc.gestionsolicitudes.data.repository

import cl.duoc.gestionsolicitudes.data.local.SolicitudDao
import cl.duoc.gestionsolicitudes.data.local.SolicitudEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow

/**
 * Repository: única puerta de acceso a los datos de solicitudes.
 * El ViewModel no conoce Room; solo habla con esta clase.
 */
class SolicitudRepository(private val dao: SolicitudDao) {

    val solicitudes: Flow<List<SolicitudEntity>> = dao.obtenerTodas()

    suspend fun obtenerPorId(id: Long): SolicitudEntity? = dao.obtenerPorId(id)

    suspend fun insertar(solicitud: SolicitudEntity): Long = dao.insertar(solicitud)

    suspend fun actualizar(solicitud: SolicitudEntity) = dao.actualizar(solicitud)

    /** Identificador único de seguimiento, por ejemplo SOL-3F9A1C2B. */
    fun generarCodigo(): String =
        "SOL-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
}
