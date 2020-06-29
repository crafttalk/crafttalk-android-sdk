package com.crafttalk.chat

enum class Events{
    MESSAGE_SEND,
    MESSAGE_SEND_ERROR,
    NO_INTERNET,
    HAS_INTERNET,
    RECONNECT,
    MESSAGE_GET,
    MESSAGE_GET_ERROR,
    ACTION_SELECT,
    ACTION_SELECT_ERROR,


    USER_NOT_FAUND,
    USER_FAUND_WITHOUT_AUTH,

    MESSAGE_GET_SERVER,
    MESSAGE_GET_OPERATOR,
    USER_AUTHORIZAT,
    START_EVENT_SEND,

    SOCKET_DESTROY // нужен для того чтобы при преподключению к чату не произошел вызов обработчика последнего эвента, который может быть аутентификация
}