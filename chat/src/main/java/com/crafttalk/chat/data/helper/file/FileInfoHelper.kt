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
        return context.contentResolver.query(uri, null, null, null, null)?.let { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    cursor.getString(columnIndex).apply {
                        cursor.close()
                    }
                } else {
                    // Handle the case where the column does not exist
                    null
                }
            } else {
                // Handle the case where no rows were returned
                null
            }
        }
    }

    fun getFileName(uri: Uri, file: File? = getFile(uri)): String? = file?.name

    fun getFileType(uri: Uri): String? = context.contentResolver.getType(uri)

}