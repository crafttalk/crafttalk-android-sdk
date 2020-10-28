package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.rest.NotificationApi
import com.crafttalk.chat.domain.entity.notification.CheckSubscription
import com.crafttalk.chat.domain.entity.notification.Subscription
import com.crafttalk.chat.domain.entity.notification.Unsubscription
import com.crafttalk.chat.domain.repository.INotificationRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import javax.inject.Inject

class NotificationRepository
@Inject constructor(
    private val notificationApi: NotificationApi
) : INotificationRepository {

    override suspend fun subscribe(uuid: String) {
        getToken { token ->
            if (notificationApi.checkSubscription(CheckSubscription(uuid)).result) {
                notificationApi.subscribe(Subscription(token, uuid))
            }
        }
    }

    override suspend fun unSubscribe(uuid: String) {
        if (!notificationApi.checkSubscription(CheckSubscription(uuid)).result) {
            notificationApi.unsubscribe(Unsubscription(uuid))
        }
    }

    override suspend fun getToken(success: (token: String) -> Unit) {
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