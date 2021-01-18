package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.presentation.ChatEventListener

interface IAuthRepository {
    fun logIn(
        visitor: Visitor,
        successAuthUi: (() -> Unit)?,
        failAuthUi: (() -> Unit)?,
        successAuthUx: () -> Unit,
        failAuthUx: () -> Unit,
        chatEventListener: ChatEventListener?
    )
}