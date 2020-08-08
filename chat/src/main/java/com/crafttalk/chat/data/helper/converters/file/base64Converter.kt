package com.crafttalk.chat.data.helper.converters.file

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

fun convertToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
}

fun convertToBase64(inputStream: InputStream): String {
    return Base64.encodeToString(inputStream.readBytes(), Base64.DEFAULT)
}