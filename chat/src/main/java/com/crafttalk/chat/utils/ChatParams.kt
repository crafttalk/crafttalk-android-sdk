package com.crafttalk.chat.utils

import java.util.*
import java.util.concurrent.TimeUnit

object ChatParams {
    internal var visitorUuid: String = ""
    internal var authMode: AuthType? = null
    internal var enableSearch: Boolean? = null
    internal var pageSize = 20
    internal var countDownloadedMessages = 20
    internal var urlChatScheme: String? = null
    internal var urlChatHost: String? = null
    internal var urlChatNameSpace: String? = null
    internal var operatorPreviewMode: OperatorPreviewMode? = null
    internal var operatorNameMode: OperatorNameMode? = null
    internal var clickableLinkMode: ClickableLinkMode? = null
    internal var firebasePushToken: String? = null
    internal var locale: Locale? = null
    internal var phonePatterns: Array<CharSequence>? = null
    internal var uploadPoolMessagesTimeout: Long = 5000
    internal var fileProviderAuthorities: String? = null
    internal var certificatePinning: String? = null
    internal var fileConnectTimeout: Long? = null
    internal var fileReadTimeout: Long? = null
    internal var fileWriteTimeout: Long? = null
    internal var fileCallTimeout: Long? = null
    internal var timeUnitTimeout: TimeUnit = TimeUnit.SECONDS

    internal var addedFieldsForRegistrationVisitor: HashMap<String, Any>? = null

    internal var glueMessage: String? = null
    internal var sendInitialMessageOnOpen: Boolean? = null
    internal var sendInitialMessageOnStartDialog: Boolean? = null
    internal var showInitialMessage: Boolean? = null

    internal var methodGetPayloadTypeWidget: (widgetId: String) -> Class<out Any>? = { null }
}