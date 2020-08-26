package com.crafttalk.chat.di.modules

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.usecase.auth.LogIn
import com.crafttalk.chat.domain.usecase.file.UploadFiles
import com.crafttalk.chat.domain.usecase.internet.SetInternetConnectionListener
import com.crafttalk.chat.domain.usecase.message.*
import com.crafttalk.chat.presentation.ChatView
import com.crafttalk.chat.presentation.ChatViewModel
import com.crafttalk.chat.presentation.ChatViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    @Provides
    @Singleton
    fun provideChatViewModelFactory(
        uploadFiles: UploadFiles,
        getMessages: GetMessages,
        sendMessages: SendMessages,
        syncMessages: SyncMessages,
        selectAction: SelectAction,
        logIn: LogIn,
        setInternetConnectionListener: SetInternetConnectionListener,
        visitor: Visitor?,
        view: ChatView,
        socketApi: SocketApi,
        updateSizeMessages: UpdateSizeMessages
    ): ChatViewModelFactory = ChatViewModelFactory(
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
        updateSizeMessages
    )

    @Provides
    @Singleton
    fun provideChatViewModel(
        parentFragment: Fragment,
        chatViewModelFactory: ChatViewModelFactory
    ): ChatViewModel = ViewModelProvider(parentFragment, chatViewModelFactory).get(ChatViewModel::class.java)

}