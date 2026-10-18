package cl.duoc.gestionsolicitudes.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/** Genera (con ZXing) la imagen QR del código de una solicitud. */
object QrGenerator {

    fun generar(texto: String, tamano: Int = 512): Bitmap {
        val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamano, tamano)
        val bitmap = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.RGB_565)
        for (x in 0 until tamano) {
            for (y in 0 until tamano) {
                bitmap.setPixel(x, y, if (matriz[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}