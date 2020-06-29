package com.crafttalk.chat.data.remote.loader_service.preparers

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream


class ImagePreparer {

    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
    }

}