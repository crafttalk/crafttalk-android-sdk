package com.crafttalk.chat.data.helper.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import javax.inject.Inject

class FileInfoHelper
@Inject constructor(
    private val context: Context
) {

    fun getFile(uri: Uri): File? {
        return try {
            File(uri.path!!)
        } catch (ex: Exception) {
            null
        }
    }

    fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null, null)?.let { cursor ->
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).apply {
                cursor.close()
            }
        }
    }

    fun getFileName(uri: Uri, file: File? = getFile(uri)): String? = file?.name

    fun getFileType(uri: Uri): String? = context.contentResolver.getType(uri)

}