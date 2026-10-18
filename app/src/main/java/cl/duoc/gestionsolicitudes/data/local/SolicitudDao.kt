package cl.duoc.gestionsolicitudes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** DAO: reúne las operaciones SQL sobre la tabla solicitudes. */
@Dao
interface SolicitudDao {

    // Flow: Room vuelve a emitir la lista cada vez que la tabla cambia (reactividad)
    @Query("SELECT * FROM solicitudes ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): SolicitudEntity?

    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizar(solicitud: SolicitudEntity)
}
