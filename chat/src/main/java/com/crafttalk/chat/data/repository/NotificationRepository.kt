package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.rest.NotificationApi
import com.crafttalk.chat.domain.entity.notification.NetworkCheckSubscription
import com.crafttalk.chat.domain.entity.notification.NetworkResultCheckSubscription
import com.crafttalk.chat.domain.entity.notification.NetworkSubscription
import com.crafttalk.chat.domain.entity.notification.NetworkUnsubscription
import com.crafttalk.chat.domain.repository.INotificationRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class NotificationRepository
@Inject constructor(
    private val notificationApi: NotificationApi
) : INotificationRepository {

    private fun checkSubscription(uuid: String, hasSubscription: Boolean, success: () -> Unit) {
        notificationApi.checkSubscription(NetworkCheckSubscription(uuid)).enqueue(object : Callback<NetworkResultCheckSubscription> {
            override fun onResponse(call: Call<NetworkResultCheckSubscription>, response: Response<NetworkResultCheckSubscription>) {
                if (response.isSuccessful && response.body()?.result == hasSubscription) {
                    success()
                }
            }
            override fun onFailure(call: Call<NetworkResultCheckSubscription>, t: Throwable) {}
        })
    }

    override fun subscribe(uuid: String) {
        getToken { token ->
            checkSubscription(uuid, false) {
                notificationApi.subscribe(NetworkSubscription(token, uuid)).enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {}
                    override fun onFailure(call: Call<Unit>, t: Throwable) {}
                })
            }
        }
    }

    override fun unSubscribe(uuid: String) {
        checkSubscription(uuid, true) {
            notificationApi.unsubscribe(NetworkUnsubscription(uuid)).enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {}
                override fun onFailure(call: Call<Unit>, t: Throwable) {}
            })
        }
    }

    override fun getToken(success: (token: String) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result?.token
                token?.let(success)
            })
    }

}