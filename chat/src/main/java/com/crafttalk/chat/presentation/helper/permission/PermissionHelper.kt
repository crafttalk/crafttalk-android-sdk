package com.crafttalk.chat.presentation.helper.permission

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

fun requestPermissionWithAction(
    context: Context,
    action: () -> Unit = {},
    permissions: Array<String>,
    noPermission: (permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit) -> Unit
) {
    checkPermission(
        permissions = permissions,
        context = context,
        noPermission = { noPermission(permissions) { action() } },
        onPermissionsGranted = action
    )
}