package com.crafttalk.chat.utils

import java.util.*
import java.util.concurrent.TimeUnit

object ChatParams {
    internal var authMode: AuthType? = null
    internal var initialMessageMode: InitialMessageMode? = null
    internal var urlChatHost: String? = null
    internal var urlChatNameSpace: String? = null
    internal var operatorPreviewMode: OperatorPreviewMode? = null
    internal var operatorNameMode: OperatorNameMode? = null
    internal var clickableLinkMode: ClickableLinkMode? = null
    internal var locale: Locale? = null
    internal var phonePatterns: Array<CharSequence>? = null
    internal var fileProviderAuthorities: String? = null
    internal var certificatePinning: String? = null
    internal var fileConnectTimeout: Long? = null
    internal var fileReadTimeout: Long? = null
    internal var fileWriteTimeout: Long? = null
    internal var fileCallTimeout: Long? = null
    internal var timeUnitTimeout: TimeUnit = TimeUnit.SECONDS
}