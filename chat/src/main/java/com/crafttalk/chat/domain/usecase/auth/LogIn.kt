package com.crafttalk.chat.domain.usecase.auth

import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IVisitorRepository

class LogIn constructor(
    private val visitorRepository: IVisitorRepository
) {

    operator fun invoke(visitor: Visitor, successAuth: () -> Unit, failAuth: (ex: Throwable) -> Unit) {
        try {
            visitorRepository.logIn(visitor, successAuth, failAuth)
//            if (usedFormAuth) {
                visitorRepository.saveVisitor(visitor)
//            }
        }
        catch (ex: Throwable) {
            failAuth(ex)
//            if (usedFormAuth) {
                visitorRepository.deleteVisitor(visitor)
//            }
        }
    }

}