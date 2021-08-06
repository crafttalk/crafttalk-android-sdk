package com.crafttalk.chat.data.api.rest

import com.crafttalk.chat.domain.entity.notification.NetworkCheckSubscription
import com.crafttalk.chat.domain.entity.notification.NetworkResultCheckSubscription
import com.crafttalk.chat.domain.entity.notification.NetworkSubscription
import com.crafttalk.chat.domain.entity.notification.NetworkUnsubscription
import com.crafttalk.chat.utils.ChatParams
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @POST("webchat/{namespace}/set-user-subscription")
    fun subscribe(
        @Body body: NetworkSubscription,
        @Path("namespace") clientId : String = ChatParams.urlSocketNameSpace!!
    ) : Call<Unit>

    @POST("webchat/{namespace}/delete-user-subscription")
    fun unsubscribe(
        @Body body: NetworkUnsubscription,
        @Path("namespace") clientId : String = ChatParams.urlSocketNameSpace!!
    ) : Call<Unit>

    @POST("webchat/{namespace}/check-user-subscription")
    fun checkSubscription(
        @Body body: NetworkCheckSubscription,
        @Path("namespace") clientId : String = ChatParams.urlSocketNameSpace!!
    ) : Call<NetworkResultCheckSubscription>

}