package com.crafttalk.chat.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.crafttalk.chat.data.ApiParams
import com.crafttalk.chat.data.ContentTypeValue
import com.crafttalk.chat.domain.entity.file.TypeUpload
import com.crafttalk.chat.data.api.rest.FileApi
import com.crafttalk.chat.data.helper.file.FileInfoHelper
import com.crafttalk.chat.data.helper.file.RequestHelper
import com.crafttalk.chat.data.local.db.dao.FileDao
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.BodyStructureUploadFile
import com.crafttalk.chat.domain.entity.file.File as FileModel
import com.crafttalk.chat.domain.repository.IFileRepository
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import com.crafttalk.chat.data.local.db.entity.FileEntity

class FileRepository
@Inject constructor(
    private val fileApi: FileApi,
    private val fileDao: FileDao,
    private val fileInfoHelper: FileInfoHelper,
    private val fileRequestHelper: RequestHelper
) : IFileRepository {

    private fun uploadFile(uuid: String, token: String, fileName: String, fileRequestBody: String, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
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
                handleUploadFile(response.code(), response.message())
                Log.d("UPLOAD_TEST", "Success upload - ${response.message()} ${response.body()}; ${response.code()}; ${request.request().url()}")
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                when (t.message) {
                    TIMEOUT_CONST -> handleUploadFile(TIMEOUT_CODE, "")
                }
                Log.d("UPLOAD_TEST", "Fail upload! - ${t.message};")
            }
        })
    }

    private fun uploadFile(uuid: String, token: String, fileName: String, fileRequestBody: RequestBody, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val body: MultipartBody.Part = MultipartBody.Part.createFormData(ApiParams.FILE_FIELD_NAME, fileName, fileRequestBody)
        val fileNameRequestBody = RequestBody.create(
            MediaType.get(ContentTypeValue.TEXT_PLAIN.value),
            fileName
        )
        val uuidRequestBody = RequestBody.create(
            MediaType.get(ContentTypeValue.TEXT_PLAIN.value),
            uuid
        )

        val request = fileApi.uploadFile(
            visitorToken = token,
            fileName = fileNameRequestBody,
            uuid = uuidRequestBody,
            fileB64 = body
        )

        request.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                handleUploadFile(response.code(), response.message())
                Log.d("UPLOAD_TEST", "Success upload - ${response.message()} ${response.body()}; ${response.code()}; ${request.request().url()}")
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                when (t.message) {
                    TIMEOUT_CONST -> handleUploadFile(TIMEOUT_CODE, "")
                }
                Log.d("UPLOAD_TEST", "Fail upload! - ${t.message};")
            }
        })
    }

    override fun uploadFile(visitor: Visitor, file: FileModel, type: TypeUpload, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val fileName = fileInfoHelper.getFileName(file.uri) ?: return
        fileDao.addFile(FileEntity(visitor.uuid, fileName))
        when (type) {
            TypeUpload.JSON -> {
                val fileRequestBody = fileRequestHelper.generateJsonRequestBody(file.uri, file.type) ?: return
                uploadFile(visitor.uuid, visitor.token, fileName, fileRequestBody, handleUploadFile)
            }
            TypeUpload.MULTIPART -> {
                val fileRequestBody = fileRequestHelper.generateMultipartRequestBody(file.uri) ?: return
                uploadFile(visitor.uuid, visitor.token, fileName, fileRequestBody, handleUploadFile)
            }
        }
    }

    override fun uploadMediaFile(visitor: Visitor, bitmap: Bitmap, type: TypeUpload, handleUploadFile: (responseCode: Int, responseMessage: String) -> Unit) {
        val fileName = "createPhoto${System.currentTimeMillis()}.jpg"
        when (type) {
            TypeUpload.JSON -> {
                val fileRequestBody = fileRequestHelper.generateJsonRequestBody(bitmap) ?: return
                uploadFile(visitor.uuid, visitor.token, fileName, fileRequestBody, handleUploadFile)
            }
            TypeUpload.MULTIPART -> {
                val fileRequestBody = fileRequestHelper.generateMultipartRequestBody(bitmap, fileName) ?: return
                uploadFile(visitor.uuid, visitor.token, fileName, fileRequestBody, handleUploadFile)
            }
        }
    }

    companion object {
        private const val TIMEOUT_CODE = 408
        private const val TIMEOUT_CONST = "timeout"
    }

}