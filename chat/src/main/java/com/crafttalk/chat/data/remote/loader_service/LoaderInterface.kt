package com.crafttalk.chat.data.remote.loader_service

import com.crafttalk.chat.data.remote.pojo.Message
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.POST
import retrofit2.http.Path


interface LoaderInterface {
    @POST("webchat/{clientId}/upload-file")
    fun listRepos(
        @Field("fileName") fileName: String,
        @Field("fileB64") fileB64: String,
        @Field("uuid") uuid: String,
        @Path("clientId") clientId: String = Uploader.clientId
    ): Call<Message>
}