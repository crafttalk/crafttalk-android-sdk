package com.crafttalk.chat.data.api.rest

import com.crafttalk.chat.domain.entity.notification.NetworkCheckSubscription
import com.crafttalk.chat.domain.entity.notification.NetworkResultCheckSubscription
import com.crafttalk.chat.domain.entity.notification.NetworkSubscription
import com.crafttalk.chat.domain.entity.notification.NetworkUnsubscription
import com.crafttalk.chat.utils.ChatParams
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @POST("webchat/{namespace}/set-user-subscription")
    fun subscribe(
        @Header("Cookie") cookie: String = "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}",
        @Header("uuid") uuid: String = ChatParams.visitorUuid,
        @Path("namespace") clientId : String = ChatParams.urlChatNameSpace!!,
        @Body body: NetworkSubscription
    ) : Call<Unit>

    @POST("webchat/{namespace}/delete-user-subscription")
    fun unsubscribe(
        @Header("Cookie") cookie: String = "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}",
        @Header("uuid") uuid: String = ChatParams.visitorUuid,
        @Path("namespace") clientId : String = ChatParams.urlChatNameSpace!!,
        @Body body: NetworkUnsubscription
    ) : Call<Unit>

    @POST("webchat/{namespace}/check-user-subscription")
    fun checkSubscription(
        @Header("Cookie") cookie: String = "webchat-${ChatParams.urlChatNameSpace}-uuid=${ChatParams.visitorUuid}",
        @Header("uuid") uuid: String = ChatParams.visitorUuid,
        @Path("namespace") clientId : String = ChatParams.urlChatNameSpace!!,
        @Body body: NetworkCheckSubscription
    ) : Call<NetworkResultCheckSubscription>
}