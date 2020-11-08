package com.crafttalk.chat.data.api.rest

import com.crafttalk.chat.domain.entity.notification.CheckSubscription
import com.crafttalk.chat.domain.entity.notification.ResultCheckSubscription
import com.crafttalk.chat.domain.entity.notification.Subscription
import com.crafttalk.chat.domain.entity.notification.Unsubscription
import com.crafttalk.chat.utils.ChatParams
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @POST("webchat/{namespace}/set-user-subscription")
    fun subscribe(
        @Body body: Subscription,
        @Path("namespace") clientId : String = ChatParams.urlSocketNameSpace!!
    ) : Call<Unit>

    @POST("webchat/{namespace}/delete-user-subscription")
    fun unsubscribe(
        @Body body: Unsubscription,
        @Path("namespace") clientId : String = ChatParams.urlSocketNameSpace!!
    ) : Call<Unit>

    @POST("webchat/{namespace}/check-user-subscription")
    fun checkSubscription(
        @Body body: CheckSubscription,
        @Path("namespace") clientId : String = ChatParams.urlSocketNameSpace!!
    ) : Call<ResultCheckSubscription>

}