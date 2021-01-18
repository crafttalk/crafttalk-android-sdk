package com.crafttalk.chat.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.crafttalk.chat.data.ApiParams
import com.crafttalk.chat.data.ContentTypeValue
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.data.api.rest.FileApi
import com.crafttalk.chat.data.helper.file.FileInfoHelper
import com.crafttalk.chat.data.helper.file.RequestHelper
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.BodyStructureUploadFile
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.repository.IFileRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class FileRepository
@Inject constructor(
    private val fileApi: FileApi,
    private val fileInfoHelper: FileInfoHelper,
    private val fileRequestHelper: RequestHelper
) : IFileRepository {
    private fun uploadFile(uuid: String, token: String, fileName: String, fileRequestBody: String){
        val request = fileApi.uploadFile(
            visitorToken = token,
            body = BodyStructureUploadFile(
                fileName = fileName,
                uuid = uuid,
                fileB64 = fileRequestBody
            )
        )

        request.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                // обработка кодов и проброс оштбок (throw ...)
                Log.d("UPLOAD_TEST", "Success upload - ${response.message()} ${response.body()}; ${response.code()}; ${request.request().url}")
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("UPLOAD_TEST", "Fail upload! - ${t.message};")
            }
        })
    }

    private fun uploadFile(uuid: String, token: String, fileName: String, fileRequestBody: RequestBody) {
        val body: MultipartBody.Part = MultipartBody.Part.createFormData(ApiParams.FILE_FIELD_NAME, fileName, fileRequestBody)
        val fileNameRequestBody = fileName.toRequestBody(
            contentType = ContentTypeValue.TEXT_PLAIN.value.toMediaType()
        )
        val uuidRequestBody = uuid.toRequestBody(
            contentType = ContentTypeValue.TEXT_PLAIN.value.toMediaType()
        )

        val request = fileApi.uploadFile(
            visitorToken = token,
            fileName = fileNameRequestBody,
            uuid = uuidRequestBody,
            fileB64 = body
        )

        request.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("UPLOAD_TEST", "Success upload - ${response.message()} ${response.body()}; ${response.code()}; ${request.request().url}")
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("UPLOAD_TEST", "Fail upload! - ${t.message};")
            }
        })
    }

    override fun uploadFile(visitor: Visitor, file: File, type: TypeUpload) {
        val fileName = fileInfoHelper.getFileName(file.uri) ?: return
        when (type) {
            TypeUpload.JSON -> {
                val fileRequestBody = fileRequestHelper.generateJsonRequestBody(file.uri, file.type) ?: return
                uploadFile(visitor.uuid, visitor.token, fileName, fileRequestBody)
            }
            TypeUpload.MULTIPART -> {
                val fileRequestBody = fileRequestHelper.generateMultipartRequestBody(file.uri) ?: return
                uploadFile(visitor.uuid, visitor.token, fileName, fileRequestBody)
            }
        }
    }

    override fun uploadFile(visitor: Visitor, bitmap: Bitmap, type: TypeUpload) {
        val fileName = "createPhoto${System.currentTimeMillis()}.jpg"
        when (type) {
            TypeUpload.JSON -> {
                val fileRequestBody = fileRequestHelper.generateJsonRequestBody(bitmap) ?: return
                uploadFile(visitor.uuid, visitor.token, fileName, fileRequestBody)
            }
            TypeUpload.MULTIPART -> {
                val fileRequestBody = fileRequestHelper.generateMultipartRequestBody(bitmap, fileName) ?: return
                uploadFile(visitor.uuid, visitor.token, fileName, fileRequestBody)
            }
        }
    }

}