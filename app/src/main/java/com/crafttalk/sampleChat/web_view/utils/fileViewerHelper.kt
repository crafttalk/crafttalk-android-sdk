package com.crafttalk.sampleChat.web_view.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.crafttalk.sampleChat.R
import com.crafttalk.sampleChat.web_view.contracts.PickFileModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun pickFiles(
    pickFile: ActivityResultLauncher<PickFileModel>?,
    pickFileModel: PickFileModel,
    noPermission: (permissions: String, actionsAfterObtainingPermission: () -> Unit) -> Unit,
    context: Context
) {
    fun pickFile() {
        pickFile?.launch(pickFileModel)
    }
    val permission = if (Build.VERSION.SDK_INT >= TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    checkPermission(
        arrayOf(permission),
        context,
        { noPermission(permission) { pickFile() } }
    ) { pickFile() }
}

fun pickImageFromCamera(
    takePicture: ActivityResultLauncher<Uri>?,
    noPermission: (permissions: String, actionsAfterObtainingPermission: () -> Unit) -> Unit,
    context: Context
) {
    fun pickImage() {
        val fileUri = FileProvider.getUriForFile(
            context,
            context.getString(R.string.chat_file_provider_authorities),
            createImageFile(context, ".jpg")
        )
        takePicture?.launch(fileUri)
    }
    val permission = Manifest.permission.CAMERA
    checkPermission(
        arrayOf(permission),
        context,
        { noPermission(permission) { pickImage() } }
    ) { pickImage() }
}

@SuppressLint("SimpleDateFormat")
private fun createImageFile(context: Context, format: String): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    return File.createTempFile("MEDIA_${timeStamp}_", format, context.filesDir)
}