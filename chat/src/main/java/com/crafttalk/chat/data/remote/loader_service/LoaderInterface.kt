package com.crafttalk.chat.data.remote.loader_service

import retrofit2.Call
import retrofit2.http.*


interface LoaderInterface {
    @Headers("Content-Type: application/json")
    @POST("webchat/{clientId}/upload-file")
    fun uploadFile(
        @Body body: BodyStructureUploadFile,
        @Path("clientId") clientId: String = Uploader.clientId
    ): Call<String>

//    @Multipart
////    @Headers("Content-Type: multipart/form-data")
//    @POST("webchat/webchat_android_internal/upload-file")
//    fun uploadFile(
//        @Part("fileName") fileName: RequestBody,
//        @Part("fileB64") fileB64: RequestBody,
//        @Part("uuid") uuid: RequestBody
//    ): Call<String>


//    @Multipart
//    @Headers("Content-Type: application/json")
////    @POST("webchat/{clientId}/upload-file")
//    @POST("webchat/webchat_android_internal/upload-file")
//    fun uploadFile(
//        @Part("fileName") fileName: RequestBody,
//        @Part fileB64: MultipartBody.Part,
//        @Part("uuid") uuid: RequestBody//,
////        @Path("clientId") clientId: String = Uploader.clientId
//    ): Call<String>

}