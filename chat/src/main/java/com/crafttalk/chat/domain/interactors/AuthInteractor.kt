package com.crafttalk.chat.domain.interactors

import android.util.Log
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IAuthRepository
import com.crafttalk.chat.presentation.ChatEventListener
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams
import java.io.File
import javax.inject.Inject

class AuthInteractor
@Inject constructor(
    private val authRepository: IAuthRepository,
    private val visitorInteractor: VisitorInteractor,
    private val personInteractor: PersonInteractor,
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

        val getPersonPreview: (personId: String) -> String? =  { personId ->
            if (personId.isEmpty()) null
            else {
                currentVisitor?.token?.let { token ->
                    personInteractor.getPersonPreview(personId, token)
                }
            }
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
                        getPersonPreview,
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
                    getPersonPreview,
                    chatEventListener
                )
            }
        }
    }

    fun logOut(filesDir: File) {
        try {
            notificationInteractor.unsubscribeNotification()
            visitorInteractor.getVisitor()?.uuid?.let { uuid ->
                authRepository.logOut(uuid, filesDir)
            }
        } catch (ex: Exception) {
            Log.e("FAIL logOut", "${ex.message}")
        }
        when (ChatParams.authType) {
            AuthType.AUTH_WITH_FORM -> {
                visitorInteractor.deleteVisitor()
            }
            AuthType.AUTH_WITHOUT_FORM -> {
                visitorInteractor.setVisitor(null)
            }
        }
    }

}