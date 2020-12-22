package com.crafttalk.chat.data.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ChatPushService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("TEST_NOTIFICATION", "onMessageReceived - ${remoteMessage.data}; ${remoteMessage.messageType}")
    }

}