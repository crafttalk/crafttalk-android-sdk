package com.crafttalk.chat.utils

import java.util.concurrent.TimeUnit

object ChatParams {
    internal var authType: AuthType? = null
    internal var timeDelayed: Long = 0L
    internal var urlSocketNameSpace: String? = null
    internal var urlSocketHost: String? = null
    internal var urlSyncHistory: String? = null
    internal var urlUploadNameSpace: String? = null
    internal var urlUploadHost: String? = null
    internal var fileProviderAuthorities: String? = null
    internal var certificatePinning: String? = null
    internal var fileConnectTimeout: Long? = null
    internal var fileReadTimeout: Long? = null
    internal var fileWriteTimeout: Long? = null
    internal var fileCallTimeout: Long? = null
    internal var timeUnitTimeout: TimeUnit = TimeUnit.SECONDS
}