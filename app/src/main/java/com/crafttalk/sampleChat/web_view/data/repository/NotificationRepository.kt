package com.crafttalk.sampleChat.web_view.data.repository

import android.util.Log
import com.crafttalk.sampleChat.web_view.data.api.NotificationApi
import com.crafttalk.sampleChat.web_view.domain.entity.notification.NetworkCheckSubscription
import com.crafttalk.sampleChat.web_view.domain.entity.notification.NetworkResultCheckSubscription
import com.crafttalk.sampleChat.web_view.domain.entity.notification.NetworkSubscription
import com.crafttalk.sampleChat.web_view.domain.entity.notification.NetworkUnsubscription
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationRepository constructor(
    private val notificationApi: NotificationApi
) {

    fun checkSubscription(
        uuid: String,
        urlChatNameSpace: String,
        hasSubscription: Boolean,
        success: () -> Unit
    ) {
        notificationApi.checkSubscription(
            cookie = "webchat-$urlChatNameSpace-uuid=$uuid",
            uuid = uuid,
            clientId = urlChatNameSpace,
            body = NetworkCheckSubscription(uuid)
        ).enqueue(object : Callback<NetworkResultCheckSubscription> {
            override fun onResponse(call: Call<NetworkResultCheckSubscription>, response: Response<NetworkResultCheckSubscription>) {
                if (response.isSuccessful && response.body()?.result == hasSubscription) {
                    success()
                }
            }
            override fun onFailure(call: Call<NetworkResultCheckSubscription>, t: Throwable) {}
        })
    }

    fun subscribe(uuid: String?, urlChatNameSpace: String) {
        uuid ?: return
        getToken { token ->
            checkSubscription(uuid, urlChatNameSpace, false) {
                notificationApi.subscribe(
                    cookie = "webchat-$urlChatNameSpace-uuid=$uuid",
                    uuid = uuid,
                    clientId = urlChatNameSpace,
                    body = NetworkSubscription(token, uuid)
                )
                    .enqueue(object : Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            Log.d("TEST_DATA", "onResponse - ${response.code()};")
                        }
                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Log.d("TEST_DATA", "onFailure - ${t.message};")
                        }
                    })
            }
        }
    }

    fun unSubscribe(uuid: String?, urlChatNameSpace: String) {
        uuid ?: return
        checkSubscription(uuid, urlChatNameSpace, true) {
            notificationApi.unsubscribe(
                cookie = "webchat-$urlChatNameSpace-uuid=$uuid",
                uuid = uuid,
                clientId = urlChatNameSpace,
                body = NetworkUnsubscription(uuid)
            ).enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {}
                override fun onFailure(call: Call<Unit>, t: Throwable) {}
            })
        }
    }

    private fun getToken(success: (token: String) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result
                token?.let(success)
            })
    }
}