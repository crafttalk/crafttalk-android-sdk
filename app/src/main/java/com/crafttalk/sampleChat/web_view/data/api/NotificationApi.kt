package com.crafttalk.sampleChat.web_view.data.api

import com.crafttalk.sampleChat.web_view.domain.entity.notification.NetworkCheckSubscription
import com.crafttalk.sampleChat.web_view.domain.entity.notification.NetworkResultCheckSubscription
import com.crafttalk.sampleChat.web_view.domain.entity.notification.NetworkSubscription
import com.crafttalk.sampleChat.web_view.domain.entity.notification.NetworkUnsubscription
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @POST("webchat/{namespace}/set-user-subscription")
    fun subscribe(
        @Header("Cookie") cookie: String,
        @Header("ct-webchat-client-id") uuid: String,
        @Path("namespace") clientId: String,
        @Body body: NetworkSubscription
    ) : Call<Unit>

    @POST("webchat/{namespace}/delete-user-subscription")
    fun unsubscribe(
        @Header("Cookie") cookie: String,
        @Header("ct-webchat-client-id") uuid: String,
        @Path("namespace") clientId: String,
        @Body body: NetworkUnsubscription
    ) : Call<Unit>

    @POST("webchat/{namespace}/check-user-subscription")
    fun checkSubscription(
        @Header("Cookie") cookie: String,
        @Header("ct-webchat-client-id") uuid: String,
        @Path("namespace") clientId: String,
        @Body body: NetworkCheckSubscription
    ) : Call<NetworkResultCheckSubscription>
}