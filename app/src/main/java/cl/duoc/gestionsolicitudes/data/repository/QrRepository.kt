package cl.duoc.gestionsolicitudes.data.repository

import cl.duoc.gestionsolicitudes.model.QrResult

/** Procesa el texto leído por la cámara y lo convierte en un QrResult. */
class QrRepository {
    fun processQrContent(content: String): QrResult = QrResult(content.trim())
}
