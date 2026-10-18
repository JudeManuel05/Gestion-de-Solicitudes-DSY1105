package cl.duoc.gestionsolicitudes.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.gestionsolicitudes.ui.screen.ListaSolicitudesScreen
import cl.duoc.gestionsolicitudes.ui.screen.LoginScreen
import cl.duoc.gestionsolicitudes.ui.screen.MenuScreen
import cl.duoc.gestionsolicitudes.ui.screen.QrScannerScreen
import cl.duoc.gestionsolicitudes.ui.screen.SolicitudFormScreen
import cl.duoc.gestionsolicitudes.viewmodel.LoginViewModel
import cl.duoc.gestionsolicitudes.viewmodel.QrViewModel
import cl.duoc.gestionsolicitudes.viewmodel.SolicitudViewModel

/** Nombres de las rutas en un solo lugar. */
object Rutas {
    const val LOGIN = "login"
    const val MENU = "menu"
    const val LISTA = "lista"
    const val QR = "qr"
    const val FORMULARIO = "formulario/{id}"
    fun formulario(id: Long = 0L) = "formulario/$id" // 0 = nueva solicitud
}

private const val DURACION = 350

/**
 * Secuencia: Login -> Menú -> ListaSolicitudesScreen -> SolicitudFormScreen
 * Los ViewModel se crean aquí una sola vez y se comparten entre pantallas.
 */
@Composable
fun AppNavigation(
    hasCameraPermission: Boolean,
    onRequestCameraPermission: () -> Unit
) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val solicitudViewModel: SolicitudViewModel = viewModel()
    val qrViewModel: QrViewModel = viewModel()
    val loginState by loginViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        // Animaciones de transición entre pantallas
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION)) + fadeIn(tween(DURACION))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION)) + fadeOut(tween(DURACION))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION)) + fadeIn(tween(DURACION))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION)) + fadeOut(tween(DURACION))
        }
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = {
                    // Se quita el login del historial: "atrás" no vuelve a él
                    navController.navigate(Rutas.MENU) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.MENU) {
            MenuScreen(
                nombreUsuario = loginState.usuarioNombre,
                onVerSolicitudes = { navController.navigate(Rutas.LISTA) },
                onEscanearQr = { navController.navigate(Rutas.QR) },
                onCerrarSesion = {
                    loginViewModel.cerrarSesion()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.LISTA) {
            ListaSolicitudesScreen(
                viewModel = solicitudViewModel,
                onNuevaSolicitud = { navController.navigate(Rutas.formulario()) },
                onEditarSolicitud = { id -> navController.navigate(Rutas.formulario(id)) },
                onEscanearQr = { navController.navigate(Rutas.QR) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.FORMULARIO,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            SolicitudFormScreen(
                id = id,
                viewModel = solicitudViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestCameraPermission = onRequestCameraPermission,
                onGuardado = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.QR) {
            QrScannerScreen(
                viewModel = qrViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = onRequestCameraPermission,
                buscarSolicitud = solicitudViewModel::buscarPorCodigo,
                onAbrirSolicitud = { id ->
                    qrViewModel.clearResult()
                    navController.navigate(Rutas.formulario(id)) {
                        popUpTo(Rutas.QR) { inclusive = true }
                    }
                },
                onVolver = {
                    qrViewModel.clearResult()
                    navController.popBackStack()
                }
            )
        }
    }
}
