package com.crafttalk.chat.initialization

import android.content.Context
import com.crafttalk.chat.R
import com.crafttalk.chat.di.DaggerSdkComponent
import com.crafttalk.chat.di.SdkComponent
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.interactors.*
import com.crafttalk.chat.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

object Chat {

    private val job = Job()
    private val scopeIO = CoroutineScope(Dispatchers.IO + job)
    private val scopeUI = CoroutineScope(Dispatchers.Main + job)

    private var conditionInteractor: ConditionInteractor? = null
    private var visitorInteractor: VisitorInteractor? = null
    private var authInteractor: AuthInteractor? = null
    private var notificationInteractor: NotificationInteractor? = null
    private var personInteractor: PersonInteractor? = null
    private var sdkComponent: SdkComponent? = null

    internal fun getSdkComponent(): SdkComponent = sdkComponent ?: throw IllegalStateException("You must call the init method before going to the chat.")

    fun setOnChatMessageListener(listener: ChatMessageListener) {
        conditionInteractor?.setMessageListener(listener)
    }

    private fun initDI(context: Context) {
        if (sdkComponent == null) {
            sdkComponent = DaggerSdkComponent.builder()
                .context(context)
                .build()
        }
        conditionInteractor = ConditionInteractor(sdkComponent!!.getConditionRepository())
        visitorInteractor = VisitorInteractor(sdkComponent!!.getVisitorRepository())
        personInteractor = PersonInteractor(sdkComponent!!.getPersonRepository())
        notificationInteractor = NotificationInteractor(sdkComponent!!.getNotificationRepository(), visitorInteractor!!)
        authInteractor = AuthInteractor(sdkComponent!!.getAuthRepository(), visitorInteractor!!, conditionInteractor!!, personInteractor!!, notificationInteractor!!)
    }

    fun init(
        context: Context,
        urlChatHost: String,
        urlChatNameSpace: String,
        authType: AuthType = AuthType.AUTH_WITHOUT_FORM,
        initialMessageMode: InitialMessageMode? = InitialMessageMode.SEND_ON_OPEN,
        operatorPreviewMode: OperatorPreviewMode = OperatorPreviewMode.CACHE,
        operatorNameMode: OperatorNameMode = OperatorNameMode.IMMUTABLE,
        clickableLinkMode: ClickableLinkMode = ClickableLinkMode.ALL,
        localeLanguage: String = context.getString(R.string.com_crafttalk_chat_default_language),
        localeCountry: String = context.getString(R.string.com_crafttalk_chat_default_country),
        phonePatterns: Array<CharSequence> = context.resources.getTextArray(R.array.com_crafttalk_chat_phone_patterns),
        fileProviderAuthorities: String? = null,
        certificatePinning: String? = null,
        fileConnectTimeout: Long? = null,
        fileReadTimeout: Long? = null,
        fileWriteTimeout: Long? = null,
        fileCallTimeout: Long? = null
    ) {
        ChatParams.authMode = authType
        ChatParams.initialMessageMode = initialMessageMode
        ChatParams.urlChatHost = urlChatHost
        ChatParams.urlChatNameSpace = urlChatNameSpace
        ChatParams.operatorPreviewMode = operatorPreviewMode
        ChatParams.operatorNameMode = operatorNameMode
        ChatParams.clickableLinkMode = clickableLinkMode
        ChatParams.locale = Locale(localeLanguage, localeCountry)
        ChatParams.phonePatterns = phonePatterns
        ChatParams.fileProviderAuthorities = fileProviderAuthorities
        ChatParams.certificatePinning = certificatePinning
        ChatParams.fileConnectTimeout = fileConnectTimeout
        ChatParams.fileReadTimeout = fileReadTimeout
        ChatParams.fileWriteTimeout = fileWriteTimeout
        ChatParams.fileCallTimeout = fileCallTimeout
        initDI(context)
    }

    fun createSession() {
        conditionInteractor?.createSessionChat()
    }

    fun destroySession() {
        conditionInteractor?.destroySessionChat()
    }

    fun wakeUp(visitor: Visitor?) {
        conditionInteractor?.openApp()
        authInteractor?.logIn(
            visitor = visitor
        )
    }

    fun drop() {
        conditionInteractor?.closeApp()
        conditionInteractor?.dropChat()
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