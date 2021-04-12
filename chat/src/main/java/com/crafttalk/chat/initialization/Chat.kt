package com.crafttalk.chat.initialization

import android.content.Context
import com.crafttalk.chat.di.DaggerSdkComponent
import com.crafttalk.chat.di.SdkComponent
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.interactors.*
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object Chat {

    private val job = Job()
    private val scopeIO = CoroutineScope(Dispatchers.IO + job)
    private val scopeUI = CoroutineScope(Dispatchers.Main + job)

    private var customizingChatBehaviorInteractor: CustomizingChatBehaviorInteractor? = null
    private var visitorInteractor: VisitorInteractor? = null
    private var authInteractor: AuthInteractor? = null
    private var notificationInteractor: NotificationInteractor? = null
    private var personInteractor: PersonInteractor? = null
    private var sdkComponent: SdkComponent? = null

    internal fun getSdkComponent(): SdkComponent = sdkComponent ?: throw IllegalStateException("You must call the init method before going to the chat.")

    fun setOnChatMessageListener(listener: ChatMessageListener) {
        customizingChatBehaviorInteractor?.setMessageListener(listener)
    }

    private fun initDI(context: Context) {
        if (sdkComponent == null) {
            sdkComponent = DaggerSdkComponent.builder()
                .context(context)
                .build()
        }
        customizingChatBehaviorInteractor = CustomizingChatBehaviorInteractor(sdkComponent!!.getChatBehaviorRepository())
        visitorInteractor = VisitorInteractor(sdkComponent!!.getVisitorRepository())
        personInteractor = PersonInteractor(sdkComponent!!.getPersonRepository())
        notificationInteractor = NotificationInteractor(sdkComponent!!.getNotificationRepository(), visitorInteractor!!)
        authInteractor = AuthInteractor(sdkComponent!!.getAuthRepository(), visitorInteractor!!, personInteractor!!, notificationInteractor!!)
    }

    private fun initParams(
        authType: AuthType,
        urlSocketHost: String,
        urlSocketNameSpace: String,
        urlUploadHost: String?,
        urlUploadNameSpace: String?,
        fileProviderAuthorities: String?,
        certificatePinning: String?
    ) {
        ChatParams.authType = authType
        ChatParams.urlSocketHost = urlSocketHost
        ChatParams.urlSocketNameSpace = urlSocketNameSpace
        ChatParams.urlUploadHost = urlUploadHost
        ChatParams.urlUploadNameSpace = urlUploadNameSpace
        ChatParams.fileProviderAuthorities = fileProviderAuthorities
        ChatParams.certificatePinning = certificatePinning
    }

    fun init(
        context: Context,
        authType: AuthType,
        urlSocketHost: String,
        urlSocketNameSpace: String,
        urlUploadHost: String? = null,
        urlUploadNameSpace: String? = null,
        fileProviderAuthorities: String? = null,
        certificatePinning: String? = null
    ) {
        initParams(authType, urlSocketHost, urlSocketNameSpace, urlUploadHost, urlUploadNameSpace, fileProviderAuthorities, certificatePinning)
        initDI(context)
    }

    fun wakeUp(visitor: Visitor?) {
        customizingChatBehaviorInteractor?.openApp()
        authInteractor?.logIn(
            visitor = visitor
        )
    }

    fun destroy() {
        customizingChatBehaviorInteractor?.closeApp()
        customizingChatBehaviorInteractor?.destroyHostChat()
    }

    fun logOut(context: Context) {
        scopeIO.launch {
            authInteractor?.logOut(context.filesDir)
        }
    }

    fun logOutWithUIActionAfter(context: Context, actionUIAfterLogOut: () -> Unit) {
        scopeIO.launch {
            authInteractor?.logOut(context.filesDir)
            scopeUI.launch {
                actionUIAfterLogOut()
            }
        }
    }

    fun logOutWithIOActionAfter(context: Context, actionIOAfterLogOut: () -> Unit) {
        scopeIO.launch {
            authInteractor?.logOut(context.filesDir)
            actionIOAfterLogOut()
        }
    }

}