package cl.duoc.gestionsolicitudes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.gestionsolicitudes.data.local.AppDatabase
import cl.duoc.gestionsolicitudes.data.local.SolicitudEntity
import cl.duoc.gestionsolicitudes.data.repository.SolicitudRepository
import cl.duoc.gestionsolicitudes.model.Catalogo
import cl.duoc.gestionsolicitudes.model.SolicitudUiState
import cl.duoc.gestionsolicitudes.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del módulo de solicitudes. Lo comparten ListaSolicitudesScreen y SolicitudFormScreen.
 * View -> ViewModel -> Repository -> DAO -> Room
 */
class SolicitudViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SolicitudRepository(
        AppDatabase.getDatabase(application).solicitudDao()
    )

    // Lista reactiva: cada cambio en Room llega solo a la pantalla de lista
    val solicitudes: StateFlow<List<SolicitudEntity>> = repository.solicitudes
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Estado del formulario
    private val _uiState = MutableStateFlow(SolicitudUiState())
    val uiState: StateFlow<SolicitudUiState> = _uiState.asStateFlow()

    /** id = 0 prepara un formulario vacío; otro id carga la solicitud desde Room para editarla. */
    fun cargarSolicitud(id: Long) {
        if (id == 0L) {
            _uiState.value = SolicitudUiState()
            return
        }
        viewModelScope.launch {
            repository.obtenerPorId(id)?.let { s ->
                _uiState.value = SolicitudUiState(
                    id = s.id,
                    codigo = s.codigo,
                    titulo = s.titulo,
                    descripcion = s.descripcion,
                    categoria = s.categoria,
                    servicio = s.servicio,
                    evidenciaUri = s.evidenciaUri,
                    estado = s.estado,
                    fechaCreacion = s.fechaCreacion
                )
            }
        }
    }

    // Eventos de la View: actualizan el valor y limpian el error de ese campo
    fun onTituloChange(valor: String) {
        _uiState.update { it.copy(titulo = valor, tituloError = null) }
    }

    fun onDescripcionChange(valor: String) {
        _uiState.update { it.copy(descripcion = valor, descripcionError = null) }
    }

    fun onCategoriaChange(valor: String) {
        _uiState.update { it.copy(categoria = valor, categoriaError = null, servicioError = null) }
    }

    fun onServicioChange(valor: String) {
        _uiState.update { it.copy(servicio = valor, servicioError = null) }
    }

    fun onEvidenciaCapturada(uri: String) {
        _uiState.update { it.copy(evidenciaUri = uri) }
    }

    fun quitarEvidencia() {
        _uiState.update { it.copy(evidenciaUri = null) }
    }

    // REGLAS DEL NEGOCIO: validación centralizada, la pantalla solo muestra los errores
    fun validarFormulario(): Boolean {
        val estado = _uiState.value
        val tituloError = when {
            estado.titulo.isBlank() -> "El título es obligatorio"
            estado.titulo.trim().length < 5 -> "El título debe tener al menos 5 caracteres"
            else -> null
        }
        val descripcionError = when {
            estado.descripcion.isBlank() -> "La descripción es obligatoria"
            estado.descripcion.trim().length < 10 -> "Describe la necesidad (mínimo 10 caracteres)"
            else -> null
        }
        val categoriaError = if (estado.categoria.isBlank()) "Selecciona una categoría" else null
        val servicioError = if (estado.servicioObligatorio && estado.servicio.isBlank()) {
            "Indica el servicio o sitio relacionado"
        } else null

        _uiState.update {
            it.copy(
                tituloError = tituloError,
                descripcionError = descripcionError,
                categoriaError = categoriaError,
                servicioError = servicioError
            )
        }
        return listOf(tituloError, descripcionError, categoriaError, servicioError).all { it == null }
    }

    fun guardar() {
        if (!validarFormulario()) return
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val estado = _uiState.value
            val ahora = System.currentTimeMillis()

            if (!estado.esEdicion) {
                // Nueva solicitud: código único, fecha/hora y estado inicial "Recibida"
                val nueva = SolicitudEntity(
                    codigo = repository.generarCodigo(),
                    titulo = estado.titulo.trim(),
                    descripcion = estado.descripcion.trim(),
                    categoria = estado.categoria,
                    servicio = estado.servicio.trim(),
                    evidenciaUri = estado.evidenciaUri,
                    estado = Catalogo.ESTADO_INICIAL,
                    fechaCreacion = ahora,
                    fechaActualizacion = ahora
                )
                repository.insertar(nueva)
                NotificationHelper.mostrar(
                    getApplication<Application>(),
                    "Solicitud ${nueva.codigo} recibida",
                    nueva.titulo
                )
                _uiState.update { it.copy(codigo = nueva.codigo, guardando = false, guardadoExitosamente = true) }
            } else {
                // Edición: se modifican solo los datos básicos y la fecha de actualización
                val original = repository.obtenerPorId(estado.id)
                if (original == null) {
                    _uiState.update { it.copy(guardando = false) }
                    return@launch
                }
                repository.actualizar(
                    original.copy(
                        titulo = estado.titulo.trim(),
                        descripcion = estado.descripcion.trim(),
                        categoria = estado.categoria,
                        servicio = estado.servicio.trim(),
                        evidenciaUri = estado.evidenciaUri,
                        fechaActualizacion = ahora
                    )
                )
                NotificationHelper.mostrar(
                    getApplication<Application>(),
                    "Solicitud ${original.codigo} actualizada",
                    estado.titulo.trim()
                )
                _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
            }
        }
    }

    /** La pantalla avisa que ya reaccionó al guardado (navegó atrás). */
    fun onGuardadoConsumido() {
        _uiState.update { it.copy(guardadoExitosamente = false) }
    }

    /** Usado por el lector QR para encontrar una solicitud por su código. */
    fun buscarPorCodigo(codigo: String): SolicitudEntity? =
        solicitudes.value.find { it.codigo.equals(codigo.trim(), ignoreCase = true) }
}