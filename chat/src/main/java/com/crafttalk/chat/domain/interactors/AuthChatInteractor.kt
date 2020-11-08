package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.ICacheRepository
import com.crafttalk.chat.domain.repository.IVisitorRepository
import javax.inject.Inject

class AuthChatInteractor
@Inject constructor(
    private val visitorRepository: IVisitorRepository,
    private val casheRepository: ICacheRepository
) {

    fun logIn(visitor: Visitor, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            visitorRepository.logIn(visitor, success, fail)
//            if (usedFormAuth) {
            casheRepository.saveVisitor(visitor)
//            }
        }
        catch (ex: Throwable) {
            fail(ex)
//            if (usedFormAuth) {
            casheRepository.deleteVisitor(visitor)
//            }
        }
    }

    fun logOut(visitor: Visitor, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            visitorRepository.logOut(visitor)
            success()
        }
        catch (ex: Throwable) {
            fail(ex)
        }
    }

}