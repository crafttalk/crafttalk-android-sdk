package com.crafttalk.chat.presentation.model

enum class MessageType(val valueType: Int) {
    USER_TEXT_MESSAGE(1),
    USER_IMAGE_MESSAGE(2),
    USER_GIF_MESSAGE(3),
    USER_FILE_MESSAGE(4),
    OPERATOR_TEXT_MESSAGE(-1),
    OPERATOR_IMAGE_MESSAGE(-2),
    OPERATOR_GIF_MESSAGE(-3),
    OPERATOR_FILE_MESSAGE(-4),
    DEFAULT_MESSAGE(0)
}
