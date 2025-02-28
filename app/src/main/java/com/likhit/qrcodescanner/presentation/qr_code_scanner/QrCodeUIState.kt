package com.likhit.qrcodescanner.presentation.qr_code_scanner

import androidx.camera.core.CameraSelector
import androidx.compose.ui.geometry.Rect

data class QrCodeUIState(
    val loading: Boolean = false,
    val detectedQrCode: String = "",
    val targetRect: Rect = Rect.Zero,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val flashEnabled: Boolean = false
)
