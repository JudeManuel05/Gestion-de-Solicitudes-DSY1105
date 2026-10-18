package cl.duoc.gestionsolicitudes.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Crea el archivo donde la cámara guardará la foto de evidencia. */
object EvidenciaHelper {

    fun crearUriEvidencia(context: Context): Uri {
        val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
        val archivo = File(carpeta, "evidencia_${System.currentTimeMillis()}.jpg")
        // FileProvider entrega una Uri segura (content://) en vez de la ruta real
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }
}