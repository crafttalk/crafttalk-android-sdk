package com.crafttalk.chat.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.crafttalk.chat.data.ApiParams
import com.crafttalk.chat.data.ContentTypeValue
import com.crafttalk.chat.data.api.rest.FileApi
import com.crafttalk.chat.data.helper.file.FileInfoHelper
import com.crafttalk.chat.data.helper.file.RequestHelper
import com.crafttalk.chat.data.local.db.dao.FileDao
import com.crafttalk.chat.data.local.db.entity.FileEntity
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.NetworkBodyStructureUploadFile
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.domain.repository.IFileRepository
import com.crafttalk.chat.utils.ChatParams
import com.crafttalk.chat.utils.ConstantsUtils.TAG_FILE_UPLOAD
import com.crafttalk.chat.utils.ConstantsUtils.TAG_FILE_UPLOAD_MEDIA
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject
import com.crafttalk.chat.domain.entity.file.File as FileModel

class FileRepository
@Inject constructor(
    private val fileApi: FileApi,
    private val fileDao: FileDao,
    private val fileInfoHelper: FileInfoHelper,
    private val fileRequestHelper: RequestHelper
) : IFileRepository {

    private fun uploadFile(uuid: String, fileName: String, fileRequestBody: String, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val request = fileApi.uploadFile(
            networkBody = NetworkBodyStructureUploadFile(
                fileName = fileName,
                uuid = uuid,
                fileB64 = fileRequestBody
            )
        )

        request.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                handleUploadFile(response.code(), response.message())
                Log.d(TAG_FILE_UPLOAD, "Success upload - ${response.message()} ${response.body()}; ${response.code()};")
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                when (t.message) {
                    TIMEOUT_CONST -> handleUploadFile(TIMEOUT_CODE, "")
                }
                Log.d(TAG_FILE_UPLOAD, "Fail upload! - ${t.message};")
            }
        })
    }

    private fun uploadFile(uuid: String, fileName: String, fileRequestBody: RequestBody, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val body: MultipartBody.Part = MultipartBody.Part.createFormData(ApiParams.FILE_FIELD_NAME, fileName, fileRequestBody)
        val fileNameRequestBody = RequestBody.create(
            ContentTypeValue.TEXT_PLAIN.value.toMediaTypeOrNull(),
            fileName
        )
        val uuidRequestBody = RequestBody.create(
            ContentTypeValue.TEXT_PLAIN.value.toMediaTypeOrNull(),
            uuid
        )

        val request = fileApi.uploadFile(
            fileName = fileNameRequestBody,
            uuid = uuidRequestBody,
            fileB64 = body
        )

        request.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                handleUploadFile(response.code(), response.message())
                Log.d(TAG_FILE_UPLOAD, "Success upload - ${response.message()} ${response.body()}; ${response.code()};")
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                when (t.message) {
                    TIMEOUT_CONST -> handleUploadFile(TIMEOUT_CODE, "")
                }
                Log.d(TAG_FILE_UPLOAD, "Fail upload! - ${t.message};")
            }
        })
    }

    override fun uploadFile(visitor: Visitor, file: FileModel, type: TypeUpload, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val fileName = fileInfoHelper.getFileName(file.uri) ?: return
        fileDao.addFile(FileEntity(visitor.uuid, fileName))
        when (type) {
            TypeUpload.JSON -> {
                val fileRequestBody = fileRequestHelper.generateJsonRequestBody(file.uri, file.type) ?: return
                uploadFile(visitor.uuid, fileName, fileRequestBody, handleUploadFile)
            }
            TypeUpload.MULTIPART -> {
                val fileRequestBody = fileRequestHelper.generateMultipartRequestBody(file.uri) ?: return
                uploadFile(visitor.uuid, fileName, fileRequestBody, handleUploadFile)
            }
        }
    }

    override fun uploadMediaFile(visitor: Visitor, bitmap: Bitmap, type: TypeUpload, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val fileName = "createPhoto${System.currentTimeMillis()}.jpg"
        Log.d(TAG_FILE_UPLOAD_MEDIA, "uploadMediaFile t - ${type}; ")
        when (type) {
            TypeUpload.JSON -> {
                val fileRequestBody = fileRequestHelper.generateJsonRequestBody(bitmap)
                Log.d(TAG_FILE_UPLOAD_MEDIA, "uploadMediaFile fileRequestBody1 - ${fileRequestBody}; ")
                uploadFile(visitor.uuid, fileName, fileRequestBody, handleUploadFile)
            }
            TypeUpload.MULTIPART -> {
                val fileRequestBody = fileRequestHelper.generateMultipartRequestBody(bitmap, fileName)
                Log.d(TAG_FILE_UPLOAD_MEDIA, "uploadMediaFile fileRequestBody2 - ${fileRequestBody}; ")
                uploadFile(visitor.uuid, fileName, fileRequestBody, handleUploadFile)
            }
        }
    }

    override suspend fun downloadDocument(documentUrl: String, documentFile: File, alternativeFile: File, downloadedSuccess: suspend () -> Unit, downloadedFail: () -> Unit) {
        val correctFile = try {
            documentFile.createNewFile()
            documentFile
        } catch (ex: IOException) {
            try {
                alternativeFile.createNewFile()
                alternativeFile
            } catch (ex: IOException) {
                downloadedFail()
                return
            }
        }
        try {
            val url = URL(documentUrl)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("Cookie", "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}")
            urlConnection.setRequestProperty("ct-webchat-client-id", ChatParams.visitorUuid)
            urlConnection.connect()
            val inputStream = urlConnection.inputStream
            val fileOutputStream = FileOutputStream(correctFile)

            val buffer = ByteArray(MEGABYTE)
            var bufferLength: Int
            while (inputStream.read(buffer).also { bufferLength = it } > 0) {
                fileOutputStream.write(buffer, 0, bufferLength)
            }
            fileOutputStream.close()

            downloadedSuccess()
        } catch (ex: FileNotFoundException) {
            correctFile.delete()
            downloadedFail()
        } catch (ex: MalformedURLException) {
            correctFile.delete()
            downloadedFail()
        } catch (ex: IOException) {
            correctFile.delete()
            downloadedFail()
        }
    }

    companion object {
        private const val TIMEOUT_CODE = 408
        private const val TIMEOUT_CONST = "timeout"
        private const val MEGABYTE = 1024 * 1024
    }

}