package com.crafttalk.chat.data

import com.crafttalk.chat.utils.ChatAttr

object ApiParams {
    val CLIENT_ID = ChatAttr.getInstance().urlUploadNameSpace
    val HOST = ChatAttr.getInstance().urlUploadHost
    const val FILE_NAME = "fileName"
    const val UUID = "uuid"
    const val FILE_FIELD_NAME = "fileB64"
}