package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.auth.Visitor

interface IVisitorRepository {
    fun getVisitorFromClient(): Visitor?
    fun setVisitorFromClient(visitor: Visitor?)
    fun getVisitorFromSharedPreferences(): Visitor?
    fun saveVisitor(visitor: Visitor)
    fun deleteVisitor()
}