package com.crafttalk.sampleChat.web_view.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

fun checkPermission(
    permissions: Array<String>,
    context: Context,
    noPermission: () -> Unit,
    onPermissionsGranted: () -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }) {
        onPermissionsGranted()
    } else {
        noPermission()
    }
}