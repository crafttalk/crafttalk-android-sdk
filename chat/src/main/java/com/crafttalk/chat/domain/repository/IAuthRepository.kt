package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.presentation.ChatEventListener
import java.io.File

interface IAuthRepository {
    fun logIn(
        visitor: Visitor,
        successAuthUi: (() -> Unit)?,
        failAuthUi: (() -> Unit)?,
        successAuthUx: () -> Unit,
        failAuthUx: () -> Unit,
        getPersonPreview: (personId: String) -> String?,
        chatEventListener: ChatEventListener?
    )
    fun logOut(uuid: String, filesDir: File)
}