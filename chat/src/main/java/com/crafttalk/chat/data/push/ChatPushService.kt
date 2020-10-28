package com.crafttalk.chat.data.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ChatPushService: FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("TEST_NOTIFICATION", "onMessageReceived - ${p0.data}; ${p0.messageType}")
    }

    override fun onDeletedMessages() {
        Log.d("TEST_NOTIFICATION", "onDeletedMessages")
    }

    override fun onMessageSent(p0: String) {
        Log.d("TEST_NOTIFICATION", "onMessageSent - ${p0}")
    }

    override fun onSendError(p0: String, p1: Exception) {
        Log.d("TEST_NOTIFICATION", "onSendError - ${p0}")
    }

    override fun onNewToken(p0: String) {
        Log.d("TEST_NOTIFICATION", "onNewToken - ${p0}")
    }

}