package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IAuthRepository
import com.crafttalk.chat.presentation.ChatEventListener
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams
import javax.inject.Inject

class AuthInteractor
@Inject constructor(
    private val authRepository: IAuthRepository,
    private val visitorInteractor: VisitorInteractor
) {

    private fun dataPreparation(visitor: Visitor?): Visitor? {
        when (ChatParams.authType) {
            AuthType.AUTH_WITH_FORM -> {
                visitor?.let {  visitorInteractor.saveVisitor(it) }
            }
            AuthType.AUTH_WITHOUT_FORM -> {
                visitor?.let { visitorInteractor.setVisitor(it) }
            }
        }
        return visitorInteractor.getVisitor()
    }

    fun logIn(
        visitor: Visitor? = null,
        successAuth: () -> Unit,
        failAuth: (ex: Throwable) -> Unit,
        firstLogInWithForm: () -> Unit = {},
        chatEventListener: ChatEventListener? = null
    ) {
        val currentVisitor = dataPreparation(visitor)

        when (ChatParams.authType) {
            AuthType.AUTH_WITH_FORM -> {
                if (currentVisitor == null) {
                    firstLogInWithForm()
                } else {
                    try {
                        authRepository.logIn(currentVisitor, successAuth, failAuth, chatEventListener)
                    } catch (ex: Throwable) {
                        visitorInteractor.deleteVisitor(currentVisitor)
                    }
                }
            }
            AuthType.AUTH_WITHOUT_FORM -> {
                authRepository.logIn(currentVisitor!!, successAuth, failAuth, chatEventListener)
            }
        }
    }

    fun logOut(visitor: Visitor) {
        authRepository.logOut(visitor)
    }

}