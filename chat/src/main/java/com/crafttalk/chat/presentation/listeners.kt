package com.crafttalk.chat.presentation

import com.crafttalk.chat.utils.TypeFailUpload

interface ChatPermissionListener {
    fun requestedPermissions(permissions: Array<String>, messages: Array<String>, action: () -> Unit)
}

interface UploadFileListener {
    fun successUpload()
    fun failUpload(message: String, type: TypeFailUpload)
}

interface ChatInternetConnectionListener {
    fun connect() // has internet
    fun failConnect() // hasn't internet
    fun lossConnection() // hasn't internet
    fun reconnect() // has internet
}

interface StateStartingProgressListener {
    fun start()
    fun stop()
}

interface NavigationListener {
    fun navigate(targetName: String)
}

interface ChatEventListener {
    fun operatorStartWriteMessage()
    fun operatorStopWriteMessage()
}