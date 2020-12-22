package com.crafttalk.chat.initialization

import android.content.Context
import com.crafttalk.chat.di.DaggerSdkComponent
import com.crafttalk.chat.di.SdkComponent
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.interactors.AuthInitInteractor
import com.crafttalk.chat.domain.interactors.CustomizingChatBehaviorInteractor
import com.crafttalk.chat.domain.interactors.NotificationInteractor
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams

object Chat {

    private lateinit var customizingChatBehaviorInteractor: CustomizingChatBehaviorInteractor
    private lateinit var authInteractor: AuthInitInteractor
    private lateinit var notificationInteractor: NotificationInteractor
    var sdkComponent: SdkComponent? = null

    fun setOnChatMessageListener(listener: ChatMessageListener) {
        customizingChatBehaviorInteractor.setMessageListener(listener)
    }

    fun setOnInternetConnectionListener(listener: ChatInternetConnectionListener) {
        customizingChatBehaviorInteractor.setInternetConnectionListener(listener)
    }

    internal fun initDI(context: Context) {
        if (sdkComponent == null) {
            sdkComponent = DaggerSdkComponent.builder()
                .context(context)
                .build()
        }
        customizingChatBehaviorInteractor = CustomizingChatBehaviorInteractor(sdkComponent!!.getChatBehaviorRepository())
        authInteractor = AuthInitInteractor(sdkComponent!!.getVisitorRepository())
        notificationInteractor = NotificationInteractor(sdkComponent!!.getNotificationRepository())
    }

    private fun initParams(
        authType: AuthType,
        urlSocketHost: String,
        urlSocketNameSpace: String
    ) {
        ChatParams.authType = authType
        ChatParams.urlSocketHost = urlSocketHost
        ChatParams.urlSocketNameSpace = urlSocketNameSpace
    }

    fun init(
        visitor: Visitor,
        context: Context,
        authType: AuthType,
        urlSocketHost: String,
        urlSocketNameSpace: String
    ) {
        initParams(authType, urlSocketHost, urlSocketNameSpace)
        initDI(context)
        authInteractor.logIn(
            visitor,
            {
                customizingChatBehaviorInteractor.leaveChatScreen()
                notificationInteractor.subscribeNotification(visitor.uuid)
            },
            {}
        )
    }

    fun wakeUp(visitor: Visitor) {
        authInteractor.logIn(
            visitor,
            {
                customizingChatBehaviorInteractor.leaveChatScreen()
                notificationInteractor.subscribeNotification(visitor.uuid)
            },
            {}
        )
    }

    fun destroy() {
//        authInteractor.logOut()
        customizingChatBehaviorInteractor.destroyHostChat()
    }

}