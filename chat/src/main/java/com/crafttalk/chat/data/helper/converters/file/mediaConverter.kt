package com.crafttalk.chat.data.helper.converters.file

import android.content.Context
import android.graphics.Bitmap
import java.io.*

fun convertToFile(bitmap: Bitmap, context: Context, fileName: String): File {
    val file = File(context.cacheDir, fileName)
    file.createNewFile()

    //Convert bitmap to byte array
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos)
    val bitMapData = bos.toByteArray()

    //write the bytes in file
    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(file)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
    try {
        fos?.write(bitMapData)
        fos?.flush()
        fos?.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file
}

fun String?.convertForFileAccess(token: String): String? {
    return this?.replace("undefined", token)
}