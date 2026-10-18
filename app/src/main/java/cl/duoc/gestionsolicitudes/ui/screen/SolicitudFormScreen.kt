package cl.duoc.gestionsolicitudes.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.gestionsolicitudes.model.Catalogo
import cl.duoc.gestionsolicitudes.utils.EvidenciaHelper
import cl.duoc.gestionsolicitudes.utils.QrGenerator
import cl.duoc.gestionsolicitudes.utils.formatearFecha
import cl.duoc.gestionsolicitudes.viewmodel.SolicitudViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    id: Long,
    viewModel: SolicitudViewModel,
    hasCameraPermission: Boolean,
    onRequestCameraPermission: () -> Unit,
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Al entrar: formulario vacío (id = 0) o carga de la solicitud a editar.
    // rememberSaveable evita recargar (y perder lo escrito) si se rota la pantalla.
    var cargado by rememberSaveable(id) { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (!cargado) {
            viewModel.cargarSolicitud(id)
            cargado = true
        }
    }

    // Cuando el ViewModel confirma el guardado: aviso + volver a la lista
    LaunchedEffect(estado.guardadoExitosamente) {
        if (estado.guardadoExitosamente) {
            Toast.makeText(context, "Solicitud ${estado.codigo} guardada", Toast.LENGTH_SHORT).show()
            viewModel.onGuardadoConsumido()
            onGuardado()
        }
    }

    // Recurso nativo: la app de cámara guarda la foto en la Uri que le entregamos
    var fotoUriPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }
    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val uri = fotoUriPendiente
        if (exito && uri != null) {
            viewModel.onEvidenciaCapturada(uri.toString())
        } else {
            Toast.makeText(context, "No se capturó la fotografía", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estado.esEdicion) "Editar solicitud" else "Nueva solicitud") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Datos de seguimiento (solo al editar): código, estado, fecha y QR
            AnimatedVisibility(visible = estado.esEdicion && estado.codigo.isNotBlank()) {
                TarjetaSeguimiento(
                    codigo = estado.codigo,
                    estado = estado.estado,
                    fechaCreacion = estado.fechaCreacion
                )
            }

            CampoFormulario(
                valor = estado.titulo,
                onValorChange = viewModel::onTituloChange,
                etiqueta = "Título",
                icono = Icons.Filled.Title,
                error = estado.tituloError
            )
            CampoFormulario(
                valor = estado.descripcion,
                onValorChange = viewModel::onDescripcionChange,
                etiqueta = "Descripción de la necesidad",
                icono = Icons.Filled.Description,
                error = estado.descripcionError,
                singleLine = false,
                minLines = 3
            )
            SelectorCategoria(
                seleccion = estado.categoria,
                error = estado.categoriaError,
                onSeleccion = viewModel::onCategoriaChange
            )
            CampoFormulario(
                valor = estado.servicio,
                onValorChange = viewModel::onServicioChange,
                etiqueta = if (estado.servicioObligatorio) "Servicio o sitio relacionado"
                else "Servicio o sitio relacionado (opcional)",
                icono = Icons.Filled.Build,
                error = estado.servicioError
            )

            // Evidencia fotográfica ficticia
            Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Evidencia fotográfica (ficticia)", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    AnimatedVisibility(
                        visible = estado.evidenciaUri != null,
                        enter = fadeIn() + scaleIn(initialScale = 0.9f),
                        exit = fadeOut() + scaleOut(targetScale = 0.9f)
                    ) {
                        AsyncImage(
                            model = estado.evidenciaUri,
                            contentDescription = "Foto de evidencia",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = {
                            if (hasCameraPermission) {
                                val uri = EvidenciaHelper.crearUriEvidencia(context)
                                fotoUriPendiente = uri
                                camaraLauncher.launch(uri)
                            } else {
                                onRequestCameraPermission()
                            }
                        }) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (estado.evidenciaUri == null) "Tomar foto" else "Cambiar foto")
                        }
                        if (estado.evidenciaUri != null) {
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = viewModel::quitarEvidencia) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Quitar")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.guardar()
                },
                enabled = !estado.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                AnimatedContent(targetState = estado.guardando, label = "boton_guardar") { guardando ->
                    if (guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar solicitud")
                        }
                    }
                }
            }
        }
    }
}

/** Lista desplegable de categorías con validación visual. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCategoria(
    seleccion: String,
    error: String?,
    onSeleccion: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
        OutlinedTextField(
            value = seleccion,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                }
            },
            isError = error != null,
            supportingText = { MensajeError(error) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            Catalogo.categorias.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

/** Código de seguimiento, estado y QR de la solicitud (para leerlo con el escáner). */
@Composable
private fun TarjetaSeguimiento(codigo: String, estado: String, fechaCreacion: Long?) {
    val qr = remember(codigo) { QrGenerator.generar(codigo).asImageBitmap() }
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Código de seguimiento", style = MaterialTheme.typography.labelMedium)
                Text(
                    codigo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                EstadoChip(estado)
                fechaCreacion?.let {
                    Spacer(Modifier.height(6.dp))
                    Text("Creada: ${formatearFecha(it)}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Image(bitmap = qr, contentDescription = "QR de la solicitud", modifier = Modifier.size(110.dp))
        }
    }
}