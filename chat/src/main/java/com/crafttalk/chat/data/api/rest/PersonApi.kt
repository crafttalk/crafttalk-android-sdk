package com.crafttalk.chat.data.api.rest

import com.crafttalk.chat.domain.entity.notification.ResultPersonPreview
import com.crafttalk.chat.utils.ChatParams
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PersonApi {

    @GET("webchat/{clientId}/get-operator/{personId}")
    fun getPersonPreview(
        @Path("clientId") clientId: String = ChatParams.urlUploadNameSpace!!,
        @Path("personId") personId : String,
        @Query("auth_token") visitorToken: String
    ) : Call<ResultPersonPreview>

}