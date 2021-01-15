package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.presentation.ChatEventListener

interface IAuthRepository {
    fun logIn(
        visitor: Visitor,
        successAuth: () -> Unit,
        failAuth: (ex: Throwable) -> Unit,
        chatEventListener: ChatEventListener? = null
    )
    fun logOut(visitor: Visitor)
}