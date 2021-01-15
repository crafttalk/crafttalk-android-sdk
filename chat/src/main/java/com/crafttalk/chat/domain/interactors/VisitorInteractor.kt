package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IVisitorRepository
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams
import javax.inject.Inject

class VisitorInteractor
@Inject constructor(
    private val visitorRepository: IVisitorRepository
) {
    fun getVisitor() : Visitor? {
        return when (ChatParams.authType) {
            AuthType.AUTH_WITH_FORM -> {
                visitorRepository.getVisitorFromSharedPreferences()
            }
            AuthType.AUTH_WITHOUT_FORM -> {
                val visitor = visitorRepository.getVisitorFromClient() ?: throw Exception("Visitor must not be null!")
                visitor
            }
            else -> throw Exception("Not found type auth!")
        }
    }
    fun setVisitor(visitor: Visitor) = visitorRepository.setVisitorFromClient(visitor)
    fun saveVisitor(visitor: Visitor) = visitorRepository.saveVisitor(visitor)
    fun deleteVisitor(visitor: Visitor) = visitorRepository.deleteVisitor(visitor)
}