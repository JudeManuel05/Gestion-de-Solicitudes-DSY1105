package cl.duoc.gestionsolicitudes.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.gestionsolicitudes.data.local.SolicitudEntity
import cl.duoc.gestionsolicitudes.utils.QrScanner
import cl.duoc.gestionsolicitudes.viewmodel.QrViewModel

private enum class PantallaQr { SIN_PERMISO, ESCANEANDO, RESULTADO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: QrViewModel,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    buscarSolicitud: (String) -> SolicitudEntity?,
    onAbrirSolicitud: (Long) -> Unit,
    onVolver: () -> Unit
) {
    // LiveData del ViewModel convertido en estado de Compose
    val qrResult by viewModel.qrResult.observeAsState()
    val context = LocalContext.current

    val pantalla = when {
        !hasCameraPermission -> PantallaQr.SIN_PERMISO
        qrResult == null -> PantallaQr.ESCANEANDO
        else -> PantallaQr.RESULTADO
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear código QR") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        // Animación: transición suave entre los 3 escenarios de la pantalla
        AnimatedContent(
            targetState = pantalla,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "pantalla_qr",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { actual ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (actual) {
                    // 1) No hay permiso: se explica y se pide
                    PantallaQr.SIN_PERMISO -> {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Permiso de cámara requerido",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Para leer el código QR de una solicitud necesitamos acceso a la cámara",
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onRequestPermission) { Text("Conceder permiso de cámara") }
                    }

                    // 2) Cámara activa buscando un QR
                    PantallaQr.ESCANEANDO -> {
                        Text("Escanea el QR de una solicitud", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            QrScanner(
                                onQrCodeScanned = { contenido ->
                                    viewModel.onQrDetected(contenido)
                                    Toast.makeText(context, "QR detectado", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            MarcoGuia()
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Enfoca el código QR dentro del marco", style = MaterialTheme.typography.bodyMedium)
                    }

                    // 3) Resultado: se busca la solicitud con ese código
                    PantallaQr.RESULTADO -> {
                        val codigo = qrResult?.content.orEmpty()
                        val solicitud = remember(codigo) { buscarSolicitud(codigo) }
                        ResultadoQr(
                            codigo = codigo,
                            solicitud = solicitud,
                            onAbrir = { solicitud?.let { onAbrirSolicitud(it.id) } },
                            onEscanearOtro = viewModel::clearResult
                        )
                    }
                }
            }
        }
    }
}

/** Marco que "respira" para guiar al usuario dónde poner el QR. */
@Composable
private fun MarcoGuia() {
    val transicion = rememberInfiniteTransition(label = "marco")
    val alpha by transicion.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alpha_marco"
    )
    Surface(
        modifier = Modifier.size(250.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    ) {}
}

@Composable
private fun ResultadoQr(
    codigo: String,
    solicitud: SolicitudEntity?,
    onAbrir: () -> Unit,
    onEscanearOtro: () -> Unit
) {
    val encontrada = solicitud != null
    Icon(
        if (encontrada) Icons.Filled.CheckCircle else Icons.Filled.SearchOff,
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = if (encontrada) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    )
    Spacer(Modifier.height(12.dp))
    Text("QR detectado", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Contenido: $codigo", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (solicitud != null) {
                Text(solicitud.titulo, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                EstadoChip(solicitud.estado)
            } else {
                Text(
                    "No existe una solicitud registrada con este código",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    if (encontrada) {
        Button(onClick = onAbrir, modifier = Modifier.fillMaxWidth()) { Text("Abrir solicitud") }
        Spacer(Modifier.height(8.dp))
    }
    OutlinedButton(onClick = onEscanearOtro, modifier = Modifier.fillMaxWidth()) {
        Text("Escanear otro código QR")
    }
}