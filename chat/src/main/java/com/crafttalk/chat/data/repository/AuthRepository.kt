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
        successAuth: () -> Unit,
        failAuth: (ex: Throwable) -> Unit,
        chatEventListener: ChatEventListener?
    ) {
        socketApi.setVisitor(visitor, successAuth, failAuth, chatEventListener)
    }

    override fun logOut(visitor: Visitor) {
        TODO("Not yet implemented")
    }

}