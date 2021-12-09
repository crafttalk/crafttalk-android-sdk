package com.crafttalk.chat.data.api.rest

import com.crafttalk.chat.domain.entity.configuration.NetworkResultConfiguration
import com.crafttalk.chat.utils.ChatParams
import retrofit2.Call
import retrofit2.http.*

interface ConfigurationApi {

    @GET("configuration/{clientId}")
    fun getConfiguration(
        @Path("clientId") clientId: String = ChatParams.urlChatNameSpace!!
    ): Call<NetworkResultConfiguration>

}