@file:OptIn(ExperimentalPermissionsApi::class)

package com.likhit.qrcodescanner.presentation.permission

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@Composable
fun CameraPermission(
    deniedContent: @Composable (PermissionStatus.Denied) -> Unit,
    grantedContent: @Composable () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    val status = cameraPermissionState.status

    AnimatedContent(
        targetState = status
    ) {
        when(it){
            is PermissionStatus.Denied -> deniedContent(it)
            PermissionStatus.Granted -> grantedContent()
        }
    }
}