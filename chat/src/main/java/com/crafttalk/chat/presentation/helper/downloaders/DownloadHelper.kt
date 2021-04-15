package com.crafttalk.chat.presentation.helper.downloaders

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.DownloadFileListener
import com.crafttalk.chat.presentation.helper.permission.checkPermission
import java.io.File

fun downloadResource(
    context: Context,
    fileName: String?,
    fileUrl: String?,
    fileType: TypeFile,
    downloadListener: DownloadFileListener,
    noPermission: (permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit) -> Unit
) {
    fun downloadFile(
        context: Context,
        fileName: String?,
        fileUrl: String?,
        fileType: TypeFile,
        downloadListener: DownloadFileListener
    ) {
        try {
            if (fileName == null) throw Exception("Not found file name for file!")
            if (fileUrl == null) throw Exception("Not found file url for file!")
            val dm = ContextCompat.getSystemService(context, DownloadManager::class.java)
            val downloadUri: Uri = Uri.parse(fileUrl)
            val request = DownloadManager.Request(downloadUri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(fileName)
                .setMimeType(fileType.value)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    File.separator.toString() + fileName
                )
            dm!!.enqueue(request)
        } catch (e: Exception) {
            downloadListener.failDownload()
        }
    }
    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    checkPermission(
        permissions,
        context,
        { noPermission(permissions) { downloadFile(context, fileName, fileUrl, fileType, downloadListener) } }
    ) { downloadFile(context, fileName, fileUrl, fileType, downloadListener) }
}

