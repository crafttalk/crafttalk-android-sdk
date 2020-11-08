package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.auth.Visitor

interface ICacheRepository {
    fun saveVisitor(visitor: Visitor)
    fun deleteVisitor(visitor: Visitor)
}