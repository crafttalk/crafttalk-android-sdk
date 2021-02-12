package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IAuthRepository
import com.crafttalk.chat.presentation.ChatEventListener
import javax.inject.Inject

class AuthRepository
@Inject constructor(
    private val socketApi: SocketApi
) : IAuthRepository {

    override fun logIn(
        visitor: Visitor,
        successAuthUi: (() -> Unit)?,
        failAuthUi: (() -> Unit)?,
        successAuthUx: () -> Unit,
        failAuthUx: () -> Unit,
        getPersonPreview: (personId: String) -> String?,
        chatEventListener: ChatEventListener?
    ) {
        socketApi.setVisitor(
            visitor,
            successAuthUi,
            failAuthUi,
            successAuthUx,
            failAuthUx,
            getPersonPreview,
            chatEventListener
        )
    }

}