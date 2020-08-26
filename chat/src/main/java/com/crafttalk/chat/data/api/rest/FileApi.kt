package com.crafttalk.chat.data.api.rest

import com.crafttalk.chat.data.ApiParams
import com.crafttalk.chat.data.ApiParams.CLIENT_ID
import com.crafttalk.chat.domain.entity.file.BodyStructureUploadFile
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface FileApi {

    @Headers("Content-Type: application/json")
    @POST("webchat/{clientId}/upload-file")
    suspend fun uploadFile(
        @Body body: BodyStructureUploadFile,
        @Path("clientId") clientId: String = CLIENT_ID
    ): Call<String>

    @Multipart
    @POST("webchat/{clientId}/upload-file")
    suspend fun uploadFile(
        @Part(ApiParams.FILE_NAME) fileName: RequestBody,
        @Part(ApiParams.UUID) uuid: RequestBody,
        @Part fileB64: MultipartBody.Part,
        @Path("clientId") clientId: String = CLIENT_ID
    )

}