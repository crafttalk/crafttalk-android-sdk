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
import com.crafttalk.chat.utils.ChatParams
import java.io.File

fun downloadResource(
    context: Context,
    fileName: String,
    fileUrl: String,
    fileType: TypeFile,
    downloadListener: DownloadFileListener,
    noPermission: (permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit) -> Unit,
    updateDownloadID: (downloadID: Long?) -> Unit
) {
    fun downloadFile(
        context: Context,
        fileName: String,
        fileUrl: String,
        fileType: TypeFile,
        downloadListener: DownloadFileListener,
        updateDownloadID: (downloadID: Long?) -> Unit
    ) {
        try {
            val dm = ContextCompat.getSystemService(context, DownloadManager::class.java)
            val downloadUri: Uri = Uri.parse(fileUrl)
            val request = DownloadManager.Request(downloadUri)
            request
                .addRequestHeader("Cookie", "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}")
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(fileName)
                .setMimeType(fileType.value)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    File.separator.toString() + fileName
                )
            val downloadID = dm!!.enqueue(request)
            updateDownloadID(downloadID)
        } catch (e: Exception) {
            downloadListener.failDownload()
            updateDownloadID(null)
        }
    }
    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    checkPermission(
        permissions,
        context,
        { noPermission(permissions) { downloadFile(context, fileName, fileUrl, fileType, downloadListener, updateDownloadID) } }
    ) { downloadFile(context, fileName, fileUrl, fileType, downloadListener, updateDownloadID) }
}

