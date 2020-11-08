package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IVisitorRepository
import javax.inject.Inject

class VisitorRepository
@Inject constructor(
    private val socketApi: SocketApi
) : IVisitorRepository {

    override fun logIn(visitor: Visitor, successAuth: () -> Unit, failAuth: (ex: Throwable) -> Unit) {
        socketApi.setVisitor(visitor, successAuth, failAuth)
    }

    override fun logOut(visitor: Visitor) {
        TODO("Not yet implemented")
    }

}