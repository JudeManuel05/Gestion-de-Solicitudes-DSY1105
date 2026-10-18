package cl.duoc.gestionsolicitudes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cl.duoc.gestionsolicitudes.data.repository.QrRepository
import cl.duoc.gestionsolicitudes.model.QrResult

/** Mantiene el resultado del escaneo. La UI lo observa pero no puede modificarlo. */
class QrViewModel : ViewModel() {

    private val repository = QrRepository()

    private val _qrResult = MutableLiveData<QrResult?>()
    val qrResult: LiveData<QrResult?> = _qrResult

    fun onQrDetected(content: String) {
        _qrResult.value = repository.processQrContent(content)
    }

    // Reinicia el estado para permitir un nuevo escaneo
    fun clearResult() {
        _qrResult.value = null
    }
}