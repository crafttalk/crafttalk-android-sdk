package com.crafttalk.sampleChat.web_view

import android.Manifest.permission.READ_MEDIA_IMAGES
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_web_view.*

class JavaScriptInterface(private val context: Context, private val activity:Activity) {
    @JavascriptInterface
    @Throws(IOException::class)
    fun getBase64FromBlobData(base64Data: String) {
        convertBase64StringToFile(base64Data)
    }

    @Throws(IOException::class)
    private fun convertBase64StringToFile(base64PDf: String) {
        val permissionStatus = ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED){
            download(base64PDf)
        } else {
            val code:Int = 1
            ActivityCompat.requestPermissions(activity,
                arrayOf(READ_MEDIA_IMAGES),
                code)
            download(base64PDf)

        }
    }

    private fun download(base64PDf: String){
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(fileMimeType)
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).toString() + "/" + UUID.randomUUID() + "_." + extension
        )
        val regex = "^data:$fileMimeType;base64,"
        try {
            val bytes = Base64.decode(base64PDf.replaceFirst(regex.toRegex(), ""), 0)
            val os = FileOutputStream(file)
            os.write(bytes)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось скачать файл =(", Toast.LENGTH_LONG).show()
        }
        if (file.exists()) {
            Toast.makeText(context, "Файл загружен!", Toast.LENGTH_LONG).show()
            // Что делать дальше решайте сами, можно кинуть натификацию или еще что выдумать...
            sendNotification(file)

        } else {
            Toast.makeText(context, "Не удалось скачать файл =(", Toast.LENGTH_LONG).show()
        }
    }
    private fun getUriForFile(context: Context, file: File): Uri = FileProvider
        .getUriForFile(
            context,
            "com.crafttalk.chat.fileprovider",
            file
        )

    private fun getMimeType(context: Context, uri: Uri): String? = context
        .contentResolver
        .getType(uri)

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(file: File) {
        val notificationId = 1
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val uri: Uri = getUriForFile(context, file)
            setDataAndType(uri, getMimeType(context, uri))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val pendingIntent = PendingIntent.getActivity(context,1, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        if (notificationManager == null) {
            Toast.makeText(context, "Файл скачен и лежит тут - $file", Toast.LENGTH_LONG).show()
        } else {
            val notification = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(CHANNEL_ID,"name", NotificationManager.IMPORTANCE_LOW)
                notificationManager.createNotificationChannel(notificationChannel)
                Notification.Builder(context, CHANNEL_ID)
                    .setContentTitle("Файл скачен")
                    .setContentIntent(pendingIntent)
                    .setChannelId(CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setAutoCancel(true)
                    .build()
            } else {
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("Файл скачен")
//                    .setContentIntent(pendingIntent)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setAutoCancel(true)
                    .build()
            }
            notificationManager.notify(notificationId, notification)
        }
    }

    companion object {
        private var fileMimeType: String? = null
        private const val CHANNEL_ID = "TEST_CHANNEL"

        fun getBase64StringFromBlobUrl(blobUrl: String, mimeType: String): String {
            if (blobUrl.startsWith("blob")) {
                fileMimeType = mimeType
                return """
                    javascript: fetch("$blobUrl", {
                        method: 'GET',
                        headers: {
                            'Content-Type': '$mimeType;charset=utf-8'
                        },
                    })
                    .then(async response => {
                        const blob = await response.blob();
                        console.log(blob);
                        var reader = new FileReader();
                        reader.readAsDataURL(blob);
                        reader.onloadend = function() {
                            base64data = reader.result;
                            Android.getBase64FromBlobData(base64data);
                        }
                    })
                    .catch(error => {
                        console.log(error)
                    });
                """.trimIndent()
            }
            return "javascript: console.log('It is not a Blob URL');"
        }
    }
}