package com.likhit.qrcodescanner.presentation.qr_code_scanner

import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class QrCodeViewModel @Inject constructor() : ViewModel() {
    private val _qrCodeUIState = MutableStateFlow(QrCodeUIState())
    val qrcodeUIState = _qrCodeUIState.asStateFlow()

    fun onQrCodeDetected(result: String) {
        Log.d("QRScanner", result)
        _qrCodeUIState.update {
            it.copy(
                detectedQrCode = result
            )
        }
    }

    fun onTargetPositioned(rect: Rect) {
        _qrCodeUIState.update {
            it.copy(
                targetRect = rect
            )
        }
    }

    fun onToggleFlash(){
        _qrCodeUIState.update {
            it.copy(
                flashEnabled = !it.flashEnabled
            )
        }
    }
}