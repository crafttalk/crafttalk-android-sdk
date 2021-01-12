package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.rest.NotificationApi
import com.crafttalk.chat.domain.entity.notification.CheckSubscription
import com.crafttalk.chat.domain.entity.notification.ResultCheckSubscription
import com.crafttalk.chat.domain.entity.notification.Subscription
import com.crafttalk.chat.domain.entity.notification.Unsubscription
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
        notificationApi.checkSubscription(CheckSubscription(uuid)).enqueue(object : Callback<ResultCheckSubscription> {
            override fun onResponse(call: Call<ResultCheckSubscription>, response: Response<ResultCheckSubscription>) {
                if (response.isSuccessful && response.body()?.result == hasSubscription) {
                    success()
                }
            }
            override fun onFailure(call: Call<ResultCheckSubscription>, t: Throwable) {}
        })
    }

    override fun subscribe(uuid: String) {
        getToken { token ->
            checkSubscription(uuid, false) {
                notificationApi.subscribe(Subscription(token, uuid)).enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {}
                    override fun onFailure(call: Call<Unit>, t: Throwable) {}
                })
            }
        }
    }

    override fun unSubscribe(uuid: String) {
        checkSubscription(uuid, true) {
            notificationApi.unsubscribe(Unsubscription(uuid)).enqueue(object : Callback<Unit> {
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