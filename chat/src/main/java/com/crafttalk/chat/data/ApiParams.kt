package com.crafttalk.chat.data

import com.crafttalk.chat.utils.ChatAttr

object ApiParams {
    val UPLOAD_CLIENT_ID = ChatAttr.getInstance().urlUploadNameSpace
    val UPLOAD_HOST = ChatAttr.getInstance().urlUploadHost
    val NOTIFICATION_CLIENT_ID = ChatAttr.getInstance().urlSocketNameSpace
    val NOTIFICATION_HOST = ChatAttr.getInstance().urlSocketHost
    const val FILE_NAME = "fileName"
    const val UUID = "uuid"
    const val FILE_FIELD_NAME = "fileB64"
}