package com.crafttalk.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.interactor.NotificationInteractor
import com.crafttalk.chat.domain.usecase.auth.LogIn
import com.crafttalk.chat.domain.usecase.file.UploadFiles
import com.crafttalk.chat.domain.usecase.internet.SetInternetConnectionListener
import com.crafttalk.chat.domain.usecase.message.*

@Suppress("UNCHECKED_CAST")
class ChatViewModelFactory constructor(
    private val uploadFiles: UploadFiles,
    private val getMessages: GetMessages,
    private val sendMessages: SendMessages,
    private val syncMessages: SyncMessages,
    private val selectAction: SelectAction,
    private val logIn: LogIn,
    private val setInternetConnectionListener: SetInternetConnectionListener,
    private val visitor: Visitor?,
    private val view: ChatView,
    private val socketApi: SocketApi,
    private val updateSizeMessages: UpdateSizeMessages,
    private val notificationInteractor: NotificationInteractor
): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatViewModel(
            uploadFiles,
            getMessages,
            sendMessages,
            syncMessages,
            selectAction,
            logIn,
            setInternetConnectionListener,
            visitor,
            view,
            socketApi,
            updateSizeMessages,
            notificationInteractor
        ) as T
    }
}