package com.crafttalk.chat.data.api.rest

import com.crafttalk.chat.domain.entity.configuration.NetworkResultConfiguration
import com.crafttalk.chat.utils.ChatParams
import retrofit2.Call
import retrofit2.http.*

interface ConfigurationApi {

    @GET("configuration/{clientId}")
    fun getConfiguration(
        @Header("Cookie") cookie: String = "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}",
        @Path("clientId") clientId: String = ChatParams.urlChatNameSpace!!
    ): Call<NetworkResultConfiguration>
}