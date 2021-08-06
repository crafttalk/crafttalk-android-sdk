package com.crafttalk.chat.data.api.rest

import com.crafttalk.chat.data.ApiParams
import com.crafttalk.chat.domain.entity.file.NetworkBodyStructureUploadFile
import com.crafttalk.chat.utils.ChatParams
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface FileApi {

    @Headers("Content-Type: application/json")
    @POST("webchat/{clientId}/upload-file")
    fun uploadFile(
        @Path("clientId") clientId: String = ChatParams.urlUploadNameSpace!!,
        @Query("auth_token") visitorToken: String,
        @Body networkBody: NetworkBodyStructureUploadFile
    ): Call<String>

    @Multipart
    @POST("webchat/{clientId}/upload-file")
    fun uploadFile(
        @Path("clientId") clientId: String = ChatParams.urlUploadNameSpace!!,
        @Query("auth_token") visitorToken: String,
        @Part(ApiParams.FILE_NAME) fileName: RequestBody,
        @Part(ApiParams.UUID) uuid: RequestBody,
        @Part fileB64: MultipartBody.Part
    ): Call<String>

}