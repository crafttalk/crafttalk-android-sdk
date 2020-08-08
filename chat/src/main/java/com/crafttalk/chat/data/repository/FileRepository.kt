package com.crafttalk.chat.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.crafttalk.chat.data.ApiParams
import com.crafttalk.chat.data.ContentTypeValue
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.data.api.FileApi
import com.crafttalk.chat.data.helper.file.FileInfoHelper
import com.crafttalk.chat.data.helper.file.RequestHelper
import com.crafttalk.chat.domain.entity.file.BodyStructureUploadFile
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.repository.IFileRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response

class FileRepository constructor(
    private val fileApi: FileApi,
    private val uuid: String, // Uuid.generateUUID(false)
    private val fileInfoHelper: FileInfoHelper,
    private val fileRequestHelper: RequestHelper
) : IFileRepository {
    private suspend fun uploadFile(fileName: String, fileRequestBody: String){
        val res = fileApi.uploadFile(
            BodyStructureUploadFile(
                fileName = fileName,
                uuid = uuid,
                fileB64 = fileRequestBody
            )
        )
        res.enqueue(object:retrofit2.Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("onActivityResult", "Error - ${t.message};")
                throw t
            }
            override fun onResponse(call: Call<String>, response: Response<String>) {
                // обработка кодов и проброс оштбок (throw ...)
                Log.d("onActivityResult", "onResponse - ${response.message()} ${response.body()}; ${response.code()}")
            }
        })
    }

    private suspend fun uploadFile(fileName: String, fileRequestBody: RequestBody) {
        val body: MultipartBody.Part = MultipartBody.Part.createFormData(ApiParams.FILE_FIELD_NAME, fileName, fileRequestBody)
        val fileNameRequestBody = fileName.toRequestBody(
            contentType = ContentTypeValue.TEXT_PLAIN.value.toMediaType()
        )
        val uuidRequestBody = uuid.toRequestBody(
            contentType = ContentTypeValue.TEXT_PLAIN.value.toMediaType()
        )

        fileApi.uploadFile(
            fileNameRequestBody,
            uuidRequestBody,
            body
        )

    }

    override suspend fun uploadFile(file: File, type: TypeUpload) {
        val fileName = fileInfoHelper.getFileName(file.uri) ?: return
        when (type) {
            TypeUpload.JSON -> {
                val fileRequestBody = fileRequestHelper.generateJsonRequestBody(file.uri, file.type) ?: return
                uploadFile(fileName, fileRequestBody)
            }
            TypeUpload.MULTIPART -> {
                val fileRequestBody = fileRequestHelper.generateMultipartRequestBody(file.uri) ?: return
                uploadFile(fileName, fileRequestBody)
            }
        }
    }

    override suspend fun uploadFile(bitmap: Bitmap, type: TypeUpload) {
        val fileName = "createPhoto${System.currentTimeMillis()}.jpg"
        when (type) {
            TypeUpload.JSON -> {
                val fileRequestBody = fileRequestHelper.generateJsonRequestBody(bitmap) ?: return
                uploadFile(fileName, fileRequestBody)
            }
            TypeUpload.MULTIPART -> {
                val fileRequestBody = fileRequestHelper.generateMultipartRequestBody(bitmap, fileName) ?: return
                uploadFile(fileName, fileRequestBody)
            }
        }

    }

}