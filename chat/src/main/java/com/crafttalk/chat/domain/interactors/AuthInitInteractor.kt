package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IVisitorRepository
import javax.inject.Inject

class AuthInitInteractor
@Inject constructor(
    private val visitorRepository: IVisitorRepository
) {

    fun logIn(visitor: Visitor, success: () -> Unit, fail: (ex: Throwable) -> Unit, useSync: Boolean = false) {
        try {
            visitorRepository.logIn(visitor, success, fail, null, useSync)
        }
        catch (ex: Throwable) {
            fail(ex)
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