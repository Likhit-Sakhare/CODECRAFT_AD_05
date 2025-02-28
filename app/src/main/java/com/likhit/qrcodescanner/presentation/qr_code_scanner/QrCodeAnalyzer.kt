package com.likhit.qrcodescanner.presentation.qr_code_scanner

import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.math.max

@ExperimentalGetImage
class QrCodeAnalyzer(
    private val targetRect: Rect,
    private val previewView: PreviewView,
    private val onQrCodeScanned: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val previewHeight = previewView.height.toFloat()
    private val previewWidth = previewView.width.toFloat()
    private var scaleFactor = 0f

    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val scanner = BarcodeScanning.getClient(scannerOptions)


    override fun analyze(image: ImageProxy) {
        if (image.image != null) {
            val frameHeight = image.height
            val frameWidth = image.width
            scaleFactor = max(
                previewHeight / frameHeight,
                previewWidth / frameWidth
            )
            val scaledFrameHeight = frameHeight * scaleFactor
            val scaledFrameWidth = frameWidth * scaleFactor
            val marginY = (scaledFrameHeight - previewHeight) / 2
            val marginX = (scaledFrameWidth - previewWidth) / 2

            val inputImage = InputImage.fromMediaImage(
                image.image!!,
                image.imageInfo.rotationDegrees
            )
            scanner.process(inputImage)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.let { barcodes ->
                            barcodes.forEach { barcode ->
                                barcode.boundingBox?.transform { scaleBound ->
                                    scaleBound.offset(-marginX, -marginY)
                                    if (targetRect.toRectF().contains(scaleBound)) {
                                        barcode.rawValue?.let(onQrCodeScanned)
                                    }
                                }
                            }
                        }
                    } else {
                        task.exception?.let { exception ->
                            Log.e("QRAnalyzer", exception.message ?: "Unknown error")
                        }
                    }
                    image.close()
                }
        } else {
            image.close()
        }
    }

    private fun transformX(x: Float) = x * scaleFactor
    private fun transformY(y: Float) = y * scaleFactor

    private fun Rect.transform(
        block: (RectF) -> Unit,
    ) {
        val original = this
        block(
            toRectF().apply {
                left = transformX(original.left.toFloat())
                top = transformY(original.top.toFloat())
                right = transformX(original.right.toFloat())
                bottom = transformY(original.bottom.toFloat())
            }
        )
    }
}