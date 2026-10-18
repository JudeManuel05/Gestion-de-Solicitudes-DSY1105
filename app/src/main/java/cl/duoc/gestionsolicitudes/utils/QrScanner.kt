package cl.duoc.gestionsolicitudes.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "QrScanner"

/**
 * Vista previa de la cámara (CameraX) + detección de QR (ML Kit).
 * Cuando detecta un código llama una sola vez a onQrCodeScanned.
 */
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun QrScanner(
    onQrCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Mantiene actualizado el callback sin reiniciar CameraX
    val currentCallback by rememberUpdatedState(onQrCodeScanned)

    // Se crea una única vista previa
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE // mejor en emuladores
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)

    // Inicia la cámara al entrar en pantalla y la libera al salir
    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(options)
        val detectado = AtomicBoolean(false)
        val liberado = AtomicBoolean(false)

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(executor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage == null || detectado.get()) {
                imageProxy.close()
                return@setAnalyzer
            }
            val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(input)
                .addOnSuccessListener { barcodes ->
                    val valor = barcodes.firstOrNull()?.rawValue
                    // compareAndSet garantiza que el callback se dispare una sola vez
                    if (valor != null && detectado.compareAndSet(false, true)) {
                        analysis.clearAnalyzer()
                        currentCallback(valor)
                        Log.d(TAG, "QR detectado: $valor")
                    }
                }
                .addOnFailureListener { e -> Log.e(TAG, "Error al procesar la imagen", e) }
                .addOnCompleteListener { imageProxy.close() }
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            if (liberado.get()) return@addListener
            try {
                val cameraProvider = cameraProviderFuture.get()
                // Cámara trasera por defecto; si no existe (algunos emuladores) usa la frontal
                val selector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo iniciar la cámara", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            liberado.set(true)
            analysis.clearAnalyzer()
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbind(preview, analysis)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al liberar la cámara", e)
            }
            scanner.close()
            executor.shutdown()
            Log.d(TAG, "Recursos de cámara liberados")
        }
    }
}