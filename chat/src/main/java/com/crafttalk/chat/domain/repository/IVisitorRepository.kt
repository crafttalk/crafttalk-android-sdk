package com.crafttalk.chat.domain.repository

import androidx.datastore.preferences.core.Preferences
import com.crafttalk.chat.domain.entity.auth.Visitor
import kotlinx.coroutines.flow.Flow

interface IVisitorRepository {
    fun getVisitorFromClient(): Visitor?
    fun setVisitorFromClient(visitor: Visitor?)
    val  visitorFlow: Flow<Visitor?>
    fun getVisitorFromDataStore(): Visitor?
    fun saveVisitor(visitor: Visitor)
    fun deleteVisitor()
}