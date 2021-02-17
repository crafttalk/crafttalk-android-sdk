package com.crafttalk.chat.initialization

import android.content.Context
import com.crafttalk.chat.di.DaggerSdkComponent
import com.crafttalk.chat.di.SdkComponent
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.interactors.*
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams

object Chat {

    private lateinit var customizingChatBehaviorInteractor: CustomizingChatBehaviorInteractor
    private lateinit var visitorInteractor: VisitorInteractor
    private lateinit var authInteractor: AuthInteractor
    private lateinit var notificationInteractor: NotificationInteractor
    private lateinit var personInteractor: PersonInteractor
    private var sdkComponent: SdkComponent? = null

    internal fun getSdkComponent(): SdkComponent = sdkComponent ?: throw IllegalStateException("You must call the init method before going to the chat.")

    fun setOnChatMessageListener(listener: ChatMessageListener) {
        customizingChatBehaviorInteractor.setMessageListener(listener)
    }

    private fun initDI(context: Context) {
        if (sdkComponent == null) {
            sdkComponent = DaggerSdkComponent.builder()
                .context(context)
                .build()
        }
        customizingChatBehaviorInteractor = CustomizingChatBehaviorInteractor(sdkComponent!!.getChatBehaviorRepository())
        visitorInteractor = VisitorInteractor(sdkComponent!!.getVisitorRepository())
        notificationInteractor = NotificationInteractor(sdkComponent!!.getNotificationRepository(), visitorInteractor)
        personInteractor = PersonInteractor(sdkComponent!!.getPersonRepository())
        authInteractor = AuthInteractor(sdkComponent!!.getAuthRepository(), visitorInteractor, personInteractor, notificationInteractor)
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
        context: Context,
        authType: AuthType,
        urlSocketHost: String,
        urlSocketNameSpace: String
    ) {
        initParams(authType, urlSocketHost, urlSocketNameSpace)
        initDI(context)
    }

    fun wakeUp(visitor: Visitor?) {
        customizingChatBehaviorInteractor.openApp()
        authInteractor.logIn(
            visitor = visitor
        )
    }

    fun destroy() {
        customizingChatBehaviorInteractor.closeApp()
        customizingChatBehaviorInteractor.destroyHostChat()
    }

//    fun logOut(visitor: Visitor?) {
//        authInteractor.logOut(visitor)
//    }

}