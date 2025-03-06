@file:OptIn(ExperimentalPermissionsApi::class)

package com.likhit.qrcodescanner.presentation.qr_code_scanner

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.likhit.qrcodescanner.presentation.permission.CameraPermission
import com.likhit.qrcodescanner.presentation.permission.CameraPermissionScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.withContext

@ExperimentalGetImage
@Composable
fun QrCodeScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: QrCodeViewModel = hiltViewModel(),
) {
    val qrCodeUIState by viewModel.qrcodeUIState.collectAsState()

    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
    ) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                cameraPermissionState.launchPermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context)
    }
    val preview = Preview.Builder().build()
    val imageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(
            android.util.Size(
                previewView.width,
                previewView.height
            )
        )
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    val targetRect by remember {
        derivedStateOf {
            qrCodeUIState.targetRect
        }
    }

    LaunchedEffect(qrCodeUIState.lensFacing) {
        imageAnalysis.setAnalyzer(
            Dispatchers.Default.asExecutor(),
            QrCodeAnalyzer(
                targetRect = android.graphics.Rect(
                    targetRect.left.toInt(),
                    targetRect.top.toInt(),
                    targetRect.right.toInt(),
                    targetRect.bottom.toInt()
                ),
                previewView = previewView,
                onQrCodeScanned = { result ->
                    viewModel.onQrCodeDetected(result)
                }
            )
        )
    }

    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(qrCodeUIState.lensFacing)
        .build()
    var camera by remember {
        mutableStateOf<Camera?>(null)
    }
    LaunchedEffect(qrCodeUIState.lensFacing) {
        val cameraProvider = ProcessCameraProvider.getInstance(context)
        camera = withContext(Dispatchers.IO) {
            cameraProvider.get()
        }.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )
        preview.surfaceProvider = previewView.surfaceProvider
    }

    LaunchedEffect(qrCodeUIState.flashEnabled) {
        camera?.cameraControl?.enableTorch(qrCodeUIState.flashEnabled)
    }

    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current

    CameraPermission(
        deniedContent = { status ->
            CameraPermissionScreen(
                requestPermission = cameraPermissionState::launchPermissionRequest,
                shouldShowRationale = status.shouldShowRationale
            )
        },
        grantedContent = {
            Scaffold { paddingValues ->
                QrCodeScreen(
                    modifier = modifier.padding(paddingValues),
                    focusManager = focusManager,
                    clipboardManager = clipboardManager,
                    qrCodeUIState = qrCodeUIState,
                    previewView = previewView,
                    onTargetPositioned = viewModel::onTargetPositioned,
                    onToggleFlash = viewModel::onToggleFlash
                )
            }
        }
    )
}

@Composable
fun QrCodeScreen(
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    clipboardManager: ClipboardManager,
    previewView: PreviewView,
    qrCodeUIState: QrCodeUIState,
    onTargetPositioned: (Rect) -> Unit,
    onToggleFlash: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = {
                previewView
            }
        )
        val widthInPx: Float
        val heightInPx: Float
        val radiusInPx: Float
        with(LocalDensity.current) {
            widthInPx = 250.dp.toPx()
            heightInPx = 250.dp.toPx()
            radiusInPx = 16.dp.toPx()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit){
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(250.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(15.dp)
                    )
                    .onGloballyPositioned {
                        onTargetPositioned(it.boundsInRoot())
                    }
            ) {
                val offset = Offset(
                    x = (size.width - widthInPx) / 2,
                    y = (size.height - heightInPx) / 2
                )
                val cutoutRect = Rect(
                    offset = offset,
                    size = Size(
                        widthInPx,
                        heightInPx
                    )
                )
                drawRoundRect(
                    topLeft = cutoutRect.topLeft,
                    size = cutoutRect.size,
                    cornerRadius = CornerRadius(radiusInPx, radiusInPx),
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear
                )
            }
        }

        IconButton(
            onClick = onToggleFlash,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(15.dp)
                .background(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (qrCodeUIState.flashEnabled) Icons.Default.FlashOn
                else Icons.Default.FlashOff,
                contentDescription = "Flash On/Off",
                tint = Color.Black
            )
        }

        if (qrCodeUIState.detectedQrCode.isNotEmpty()) {
            Log.d("QRCodeScreen", qrCodeUIState.detectedQrCode)
            val isUrl = qrCodeUIState.detectedQrCode.startsWith("http://") ||
                    qrCodeUIState.detectedQrCode.startsWith("https://")
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SelectionContainer {
                    Text(
                        text = qrCodeUIState.detectedQrCode,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(15.dp)
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                            .clickable {
                                if (isUrl) {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            qrCodeUIState.detectedQrCode
                                        )
                                    )
                                    context.startActivity(Intent.createChooser(intent, "Open with"))
                                }
                            },
                        color = if (isUrl) Color.Blue.copy(alpha = 0.5f) else Color.Black
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    if(isUrl){
                        IconButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        qrCodeUIState.detectedQrCode
                                    )
                                )
                                context.startActivity(Intent.createChooser(intent, "Open with"))
                            },
                            modifier = Modifier.size(50.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White.copy(0.6f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Open Link",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }else{
                        IconButton(
                            onClick = {
                                val searchIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        "https://www.google.com/search?q=" +
                                                Uri.encode(qrCodeUIState.detectedQrCode)
                                    )
                                )
                                context.startActivity(searchIntent)
                            },
                            modifier = Modifier.size(50.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White.copy(0.6f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Web search",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    qrCodeUIState.detectedQrCode
                                )
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(
                                sendIntent,
                                if(isUrl) "Sharing link" else "Sharing Text"
                            )
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.size(50.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White.copy(0.6f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(qrCodeUIState.detectedQrCode))
                        },
                        modifier = Modifier.size(50.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}