package cl.duoc.gestionsolicitudes

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cl.duoc.gestionsolicitudes.navigation.AppNavigation
import cl.duoc.gestionsolicitudes.ui.theme.GestionSolicitudesTheme
import cl.duoc.gestionsolicitudes.utils.CameraPermissionHelper
import cl.duoc.gestionsolicitudes.utils.NotificationHelper

/**
 * Punto de entrada. Coordina permisos (cámara y notificaciones)
 * y carga la navegación de Compose.
 */
class MainActivity : ComponentActivity() {

    // Estado observable: si cambia, Compose vuelve a dibujar las pantallas que lo usan
    private var hasCameraPermission by mutableStateOf(false)

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        val mensaje = if (isGranted) "Permiso de cámara concedido"
        else "Se necesita la cámara para fotos de evidencia y lectura de QR"
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Si se rechaza, la app funciona igual pero sin avisos */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verificar permiso inicial de cámara
        hasCameraPermission = CameraPermissionHelper.hasCameraPermission(this)

        // Preparar notificaciones y pedir permiso (Android 13+)
        NotificationHelper.crearCanal(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.tienePermiso(this)
        ) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            GestionSolicitudesTheme {
                AppNavigation(
                    hasCameraPermission = hasCameraPermission,
                    onRequestCameraPermission = {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        }
    }

    // Por si el usuario cambió el permiso desde Ajustes con la app en segundo plano
    override fun onResume() {
        super.onResume()
        hasCameraPermission = CameraPermissionHelper.hasCameraPermission(this)
    }
}