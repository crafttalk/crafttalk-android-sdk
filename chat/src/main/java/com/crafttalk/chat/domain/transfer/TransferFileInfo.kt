package com.crafttalk.chat.domain.transfer

// временное решение, пока не добавят поля size, height, width в NetworkMessage
data class TransferFileInfo(
    val size: Long? = null,
    val height: Int? = null,
    val width: Int? = null
)