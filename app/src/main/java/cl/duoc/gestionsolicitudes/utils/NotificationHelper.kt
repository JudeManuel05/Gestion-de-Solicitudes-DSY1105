package cl.duoc.gestionsolicitudes.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.gestionsolicitudes.MainActivity
import cl.duoc.gestionsolicitudes.R

/** Recurso nativo: notificaciones locales del sistema. */
object NotificationHelper {

    private const val CANAL_ID = "canal_solicitudes"

    // Desde Android 8 toda notificación necesita un canal
    fun crearCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_ID,
            "Solicitudes",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Avisos sobre el registro de solicitudes" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    // Desde Android 13 el usuario debe autorizar las notificaciones
    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // El permiso se verifica con tienePermiso()
    fun mostrar(context: Context, titulo: String, mensaje: String) {
        if (!tienePermiso(context)) return

        // Al tocar la notificación se abre la aplicación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notificacion)
    }
}