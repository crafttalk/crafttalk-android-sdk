package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.auth.Visitor

interface IVisitorRepository {
    fun logIn(visitor: Visitor, successAuth: () -> Unit, failAuth: (ex: Throwable) -> Unit)
    fun logOut(visitor: Visitor)

    fun saveVisitor(visitor: Visitor)
    fun deleteVisitor(visitor: Visitor)
}