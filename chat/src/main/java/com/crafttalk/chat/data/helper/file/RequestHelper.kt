package com.crafttalk.chat.data.helper.file

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.crafttalk.chat.data.ContentTypeValue
import com.crafttalk.chat.data.helper.converters.file.convertToBase64
import com.crafttalk.chat.data.helper.converters.file.convertToFile
import com.crafttalk.chat.domain.entity.file.TypeFile
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class RequestHelper
@Inject constructor(
    private val context: Context
) {

    fun getMimeType(fileName: String): MediaType {
        val ext = fileName.substringAfterLast('.').lowercase()
        val mime = when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "application/octet-stream"
        }
        return mime.toMediaType()
    }

    fun generateMultipartRequestBody(uri: Uri, filename: String): RequestBody? {
        val mimeType: MediaType =  getMimeType(filename)
        return context.contentResolver.openInputStream(uri)?.readBytes()?.let { bytes ->
            RequestBody.create(
                mimeType,
                bytes
            )
        }
    }

    fun generateMultipartRequestBody(file: File, filename: String): RequestBody {
        val mimeType: MediaType =  getMimeType(filename)
        return file.readBytes().let { bytes ->
            RequestBody.create(
                mimeType,
                bytes
            )
        }
    }

    fun generateMultipartRequestBody(bitmap: Bitmap, mediaName: String): RequestBody {
        return convertToFile(bitmap, context, mediaName).readBytes().let { bytes ->
            RequestBody.create(
                ContentTypeValue.MEDIA.value.toMediaTypeOrNull(),
                bytes
            )
        }
    }

    fun generateJsonRequestBody(uri: Uri, type: TypeFile): String? {
        return when(type) {
            TypeFile.FILE -> context.contentResolver.openInputStream(uri)?.run(::convertToBase64)
            TypeFile.IMAGE -> {
                generateJsonRequestBody(
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                )
            }
            else -> null
        }
    }

    fun generateJsonRequestBody(bitmap: Bitmap): String = convertToBase64(bitmap)

}