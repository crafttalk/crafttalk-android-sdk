package com.crafttalk.sampleChat.web_view

import android.content.Context
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*

class JavaScriptInterface(private val context: Context) {

    @JavascriptInterface
    @Throws(IOException::class)
    fun getBase64FromBlobData(base64Data: String) {
        convertBase64StringToFile(base64Data)
    }

    @Throws(IOException::class)
    private fun convertBase64StringToFile(base64PDf: String) {
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(fileMimeType)
        val path = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).toString() + "/" + UUID.randomUUID() + "_." + extension
        )
        val regex = "^data:$fileMimeType;base64,"
        val bytes = Base64.decode(base64PDf.replaceFirst(regex.toRegex(), ""), 0)
        try {
            val os = FileOutputStream(path)
            os.write(bytes)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось скачать файл =(", Toast.LENGTH_LONG).show()
        }
        if (path.exists()) {
            // Что делать дальше решайте сами, можно кинуть натификацию или еще что выдумать...
            Toast.makeText(context, "Файл скачен и лежит тут - $path", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Не удалось скачать файл =(", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private var fileMimeType: String? = null

        fun getBase64StringFromBlobUrl(blobUrl: String, mimeType: String): String {
            if (blobUrl.startsWith("blob")) {
                fileMimeType = mimeType
                return "javascript: var xhr = new XMLHttpRequest();" +
                        "xhr.open('GET', '" + blobUrl + "', true);" +
                        "xhr.setRequestHeader('Content-type','" + mimeType + ";charset=UTF-8');" +
                        "xhr.responseType = 'blob';" +
                        "xhr.onload = function(e) {" +
                        "    if (this.status == 200) {" +
                        "        var blobFile = this.response;" +
                        "        var reader = new FileReader();" +
                        "        reader.readAsDataURL(blobFile);" +
                        "        reader.onloadend = function() {" +
                        "            base64data = reader.result;" +
                        "            Android.getBase64FromBlobData(base64data);" +
                        "        }" +
                        "    }" +
                        "};" +
                        "xhr.send();"
            }
            return "javascript: console.log('It is not a Blob URL');"
        }
    }
}