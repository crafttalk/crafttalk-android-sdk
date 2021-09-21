package com.crafttalk.chat.domain.interactors

import android.util.Log
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IAuthRepository
import com.crafttalk.chat.presentation.ChatEventListener
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams
import com.crafttalk.chat.utils.ChatStatus
import java.io.File
import javax.inject.Inject

class AuthInteractor
@Inject constructor(
    private val authRepository: IAuthRepository,
    private val visitorInteractor: VisitorInteractor,
    private val conditionInteractor: ConditionInteractor,
    private val personInteractor: PersonInteractor,
    private val notificationInteractor: NotificationInteractor
) {

    private fun dataPreparation(visitor: Visitor?): Visitor? {
        return when (ChatParams.authMode) {
            AuthType.AUTH_WITH_FORM -> visitor?.apply(visitorInteractor::saveVisitor)
            AuthType.AUTH_WITHOUT_FORM -> visitor?.apply(visitorInteractor::setVisitor)
            else -> null
        } ?: visitorInteractor.getVisitor()
    }

    fun logIn(
        visitor: Visitor? = null,
        successAuthUi: () -> Unit = {},
        failAuthUi: () -> Unit = {},
        successAuthUx: suspend () -> Unit = {},
        failAuthUx: suspend () -> Unit = {},
        sync: suspend () -> Unit = {},
        updateCurrentReadMessageTime: (Long) -> Boolean = { false },
        firstLogInWithForm: () -> Unit = {},
        chatEventListener: ChatEventListener? = null
    ) {
        val currentVisitor = dataPreparation(visitor)

        val successAuthUiWrapper = {
            if (conditionInteractor.getStatusChat() == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP) {
                successAuthUi()
            }
        }

        val failAuthUiWrapper = {
            if (conditionInteractor.getStatusChat() == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP) {
                failAuthUi()
            }
        }

        val successAuthUxWrapper = suspend {
            successAuthUx()
            notificationInteractor.subscribeNotification()
        }

        val failAuthUxWrapper = suspend {
            failAuthUx()
            notificationInteractor.unsubscribeNotification()
        }

        val syncWrapper = suspend {
            if (conditionInteractor.getStatusChat() == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP) {
                sync()
            }
        }

        val updateCurrentReadMessageTimeWrapper: (newTimeMark: Long) -> Unit = { newTimeMark ->
            when (conditionInteractor.getStatusChat()) {
                ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP -> updateCurrentReadMessageTime(newTimeMark)
                ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP -> {
                    val currentReadMessageTime = conditionInteractor.getCurrentReadMessageTime()
                    if (newTimeMark > currentReadMessageTime) {
                        conditionInteractor.saveCurrentReadMessageTime(newTimeMark)
                    }
                }
            }
        }

        val updateCountUnreadMessagesWrapper: (countNewMessages: Int, hasUserMessage: Boolean) -> Unit = { countNewUnreadMessages, hasUserMessage ->
            if (conditionInteractor.getStatusChat() ==  ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP) {
                if (hasUserMessage) {
                    conditionInteractor.saveCountUnreadMessages(countNewUnreadMessages)
                } else {
                    val oldCountUnreadMessages = conditionInteractor.getCountUnreadMessages()
                    conditionInteractor.saveCountUnreadMessages(oldCountUnreadMessages + countNewUnreadMessages)
                }
            }
        }

        val getPersonPreviewWrapper: suspend (personId: String) -> String? = { personId ->
            currentVisitor?.token?.let { token ->
                personInteractor.getPersonPreview(personId, token)
            }
        }

        when (ChatParams.authMode) {
            AuthType.AUTH_WITH_FORM -> {
                if (currentVisitor == null) {
                    firstLogInWithForm()
                } else {
                    authRepository.logIn(
                        visitor = currentVisitor,
                        successAuthUi = successAuthUiWrapper,
                        failAuthUi = failAuthUiWrapper,
                        successAuthUx = successAuthUxWrapper,
                        failAuthUx = failAuthUxWrapper,
                        sync = syncWrapper,
                        updateCurrentReadMessageTime = updateCurrentReadMessageTimeWrapper,
                        updateCountUnreadMessages = updateCountUnreadMessagesWrapper,
                        getPersonPreview = getPersonPreviewWrapper,
                        updatePersonName = personInteractor::updatePersonName,
                        chatEventListener = chatEventListener
                    )
                }
            }
            AuthType.AUTH_WITHOUT_FORM -> {
                authRepository.logIn(
                    visitor = currentVisitor!!,
                    successAuthUi = successAuthUiWrapper,
                    failAuthUi = failAuthUiWrapper,
                    successAuthUx = successAuthUxWrapper,
                    failAuthUx = failAuthUxWrapper,
                    sync = syncWrapper,
                    updateCurrentReadMessageTime = updateCurrentReadMessageTimeWrapper,
                    updateCountUnreadMessages = updateCountUnreadMessagesWrapper,
                    getPersonPreview = getPersonPreviewWrapper,
                    updatePersonName = personInteractor::updatePersonName,
                    chatEventListener = chatEventListener
                )
            }
        }
    }

    fun logOut(filesDir: File) {
        try {
            notificationInteractor.unsubscribeNotification()
            authRepository.logOut(filesDir)
        } catch (ex: Exception) {
            Log.e("FAIL logOut", "${ex.message}")
        }
        conditionInteractor.clearDataChatState()
        visitorInteractor.clearDataVisitor()
    }

}