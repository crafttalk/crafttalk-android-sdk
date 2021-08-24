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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class RequestHelper
@Inject constructor(
    private val context: Context
) {

    fun generateMultipartRequestBody(uri: Uri): RequestBody? {
        return context.contentResolver.openInputStream(uri)?.readBytes()?.let { bytes ->
            RequestBody.create(
                MultipartBody.FORM,
                bytes
            )
        }
    }

    fun generateMultipartRequestBody(file: File): RequestBody? {
        return file.readBytes().let { bytes ->
            RequestBody.create(
                MultipartBody.FORM,
                bytes
            )
        }
    }

    fun generateMultipartRequestBody(bitmap: Bitmap, mediaName: String): RequestBody? {
        return convertToFile(bitmap, context, mediaName).readBytes().let { bytes ->
            RequestBody.create(
                MediaType.get(ContentTypeValue.MEDIA.value),
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