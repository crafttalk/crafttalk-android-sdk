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
        @Header("Cookie") cookie: String = "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}",
        @Header("uuid") uuid: String = ChatParams.visitorUuid,
        @Path("clientId") clientId: String = ChatParams.urlChatNameSpace!!,
        @Body networkBody: NetworkBodyStructureUploadFile
    ): Call<String>

    @Multipart
    @POST("webchat/{clientId}/upload-file")
    fun uploadFile(
        @Header("Cookie") cookie: String = "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}",
        @Header("uuid") uuidHearder: String = ChatParams.visitorUuid,
        @Path("clientId") clientId: String = ChatParams.urlChatNameSpace!!,
        @Part(ApiParams.FILE_NAME) fileName: RequestBody,
        @Part(ApiParams.UUID) uuid: RequestBody,
        @Part fileB64: MultipartBody.Part
    ): Call<String>

}