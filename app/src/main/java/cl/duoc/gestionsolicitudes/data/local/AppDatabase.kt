package cl.duoc.gestionsolicitudes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Punto de entrada a Room: conecta la Entity con su DAO y crea el archivo de la BD. */
@Database(entities = [SolicitudEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun solicitudDao(): SolicitudDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton: se reutiliza una sola instancia de la base de datos
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solicitudes_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
