package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.auth.Visitor

interface IVisitorRepository {
    fun getVisitorFromClient(): Visitor?
    fun getVisitorFromSharedPreferences(): Visitor?
    fun setVisitorFromClient(visitor: Visitor?)
    fun saveVisitor(visitor: Visitor)
    fun deleteVisitor()
}