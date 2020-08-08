package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.auth.Visitor

interface IVisitorRepository {
    fun logIn(visitor: Visitor, successAuth: () -> Unit, failAuth: (ex: Throwable) -> Unit)
    fun logOut()

    fun saveVisitor(visitor: Visitor)
    fun deleteVisitor(visitor: Visitor)
}