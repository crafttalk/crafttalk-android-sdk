package com.crafttalk.chat.data.helper.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.crafttalk.chat.data.ContentTypeValue
import com.crafttalk.chat.data.helper.converters.file.convertToBase64
import com.crafttalk.chat.data.helper.converters.file.convertToFile
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.utils.ConstantsUtils.TAG_FILE_UPLOAD
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
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

    /**
     * Открывает поток на чтение файла.
     *
     * Для виртуальных документов (файлы Google Docs, ещё не скачанные файлы облачных хранилищ)
     * openInputStream бросает FileNotFoundException с текстом "File is virtual": содержимого
     * в исходном формате у таких файлов нет. Зато провайдер сообщает, в какие форматы файл
     * можно выгрузить, поэтому пробуем прочитать его в одном из них.
     */
    @Throws(FileNotFoundException::class)
    private fun openFileStream(uri: Uri): InputStream = try {
        context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException("Can't open input stream, uri - $uri")
    } catch (ex: FileNotFoundException) {
        Log.w(TAG_FILE_UPLOAD, "Can't open file, try to read it as virtual, uri - $uri", ex)
        openVirtualFileStream(uri) ?: throw ex
    }

    private fun openVirtualFileStream(uri: Uri): InputStream? {
        val streamTypes = context.contentResolver.getStreamTypes(uri, ANY_MIME_TYPE) ?: return null
        streamTypes.forEach { streamType ->
            try {
                val descriptor = context.contentResolver
                    .openTypedAssetFileDescriptor(uri, streamType, null) ?: return@forEach
                Log.d(TAG_FILE_UPLOAD, "Virtual file is read as $streamType, uri - $uri")
                return descriptor.createInputStream()
            } catch (ex: Exception) {
                Log.w(TAG_FILE_UPLOAD, "Can't read virtual file as $streamType, uri - $uri", ex)
            }
        }
        return null
    }

    fun generateMultipartRequestBody(uri: Uri, filename: String): RequestBody {
        val mimeType: MediaType =  getMimeType(filename)
        return openFileStream(uri).use { it.readBytes() }.let { bytes ->
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
            TypeFile.FILE -> openFileStream(uri).use(::convertToBase64)
            TypeFile.IMAGE -> generateJsonRequestBody(decodeBitmap(uri))
            else -> null
        }
    }

    fun generateJsonRequestBody(bitmap: Bitmap): String = convertToBase64(bitmap)

    private fun decodeBitmap(uri: Uri): Bitmap =
        openFileStream(uri).use(BitmapFactory::decodeStream)
            ?: throw IOException("Can't decode image, uri - $uri")

    companion object {
        private const val ANY_MIME_TYPE = "*/*"
    }

}