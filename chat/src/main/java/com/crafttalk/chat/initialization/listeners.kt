package com.crafttalk.chat.initialization

import com.crafttalk.chat.utils.R_PERMISSIONS

interface ChatMessageListener {
    fun getNewMessages(countMessages: Int)
}

interface ChatPermissionListener {
    fun requestedPermissions(permissions: Array<R_PERMISSIONS>, message: Array<String>)
}

interface ChatInternetConnectionListener {
    fun connect() // has internet
    fun failConnect() // hasn't internet
    fun disconnect()
    fun lossConnection() // hasn't internet
    fun reconnect() // has internet
}