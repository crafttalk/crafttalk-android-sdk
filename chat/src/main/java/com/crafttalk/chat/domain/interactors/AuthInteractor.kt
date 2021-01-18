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
    private val visitorInteractor: VisitorInteractor,
    private val notificationInteractor: NotificationInteractor
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
        successAuthUi: (() -> Unit)? = null,
        failAuthUi: (() -> Unit)? = null,
        successAuthUx: () -> Unit = {},
        failAuthUx: () -> Unit = {},
        firstLogInWithForm: () -> Unit = {},
        chatEventListener: ChatEventListener? = null
    ) {
        val currentVisitor = dataPreparation(visitor)

        val successAuthUxWrapper = {
            successAuthUx()
            notificationInteractor.subscribeNotification()
        }

        val failAuthUxWrapper = {
            failAuthUx()
            notificationInteractor.unsubscribeNotification()
        }

        when (ChatParams.authType) {
            AuthType.AUTH_WITH_FORM -> {
                if (currentVisitor == null) {
                    firstLogInWithForm()
                } else {
                    authRepository.logIn(
                        currentVisitor,
                        successAuthUi,
                        failAuthUi,
                        successAuthUxWrapper,
                        failAuthUxWrapper,
                        chatEventListener
                    )
                }
            }
            AuthType.AUTH_WITHOUT_FORM -> {
                authRepository.logIn(
                    currentVisitor!!,
                    successAuthUi,
                    failAuthUi,
                    successAuthUxWrapper,
                    failAuthUxWrapper,
                    chatEventListener
                )
            }
        }
    }

    fun logOut(visitor: Visitor?) {
        notificationInteractor.unsubscribeNotification()
        when (ChatParams.authType) {
            AuthType.AUTH_WITH_FORM -> {
                visitor?.let {  visitorInteractor.deleteVisitor(it) }
            }
            AuthType.AUTH_WITHOUT_FORM -> {
                visitorInteractor.setVisitor(null)
            }
        }
    }

}