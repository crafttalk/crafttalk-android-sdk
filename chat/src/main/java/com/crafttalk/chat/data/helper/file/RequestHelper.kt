package com.crafttalk.chat.data.helper.file

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.crafttalk.chat.data.ContentTypeValue
import com.crafttalk.chat.data.helper.converters.file.convertToBase64
import com.crafttalk.chat.data.helper.converters.file.convertToFile
import com.crafttalk.chat.domain.entity.file.TypeFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RequestHelper constructor(
    private val context: Context
) {

    fun generateMultipartRequestBody(uri: Uri): RequestBody? {
        return context.contentResolver.openInputStream(uri)?.readBytes()?.toRequestBody(
            contentType = MultipartBody.FORM
        )
    }

    fun generateMultipartRequestBody(file: File): RequestBody? {
        return file.readBytes().toRequestBody(
            contentType = MultipartBody.FORM
        )
    }

    fun generateMultipartRequestBody(bitmap: Bitmap, mediaName: String): RequestBody? {
        return convertToFile(bitmap, context, mediaName).readBytes().toRequestBody(
            contentType = ContentTypeValue.MEDIA.value.toMediaType()
        )
    }

    fun generateJsonRequestBody(uri: Uri, type: TypeFile): String? {
        return when(type) {
            TypeFile.FILE -> {
                context.contentResolver.openInputStream(uri)?.let {
                    convertToBase64(it)
                }
            }
            TypeFile.IMAGE -> {
                generateJsonRequestBody(
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                )
            }
            else -> null
        }
    }

    fun generateJsonRequestBody(bitmap: Bitmap): String? = convertToBase64(bitmap)

}