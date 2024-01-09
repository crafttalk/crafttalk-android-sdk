package com.crafttalk.chat.data.repository

import android.util.Log
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.local.db.dao.FileDao
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IAuthRepository
import com.crafttalk.chat.presentation.ChatEventListener
import com.crafttalk.chat.utils.ConstantsUtils.TAG_AUTH_REPOSITORY
import java.io.File
import javax.inject.Inject

class AuthRepository
@Inject constructor(
    private val socketApi: SocketApi,
    private val fileDao: FileDao,
    private val messageDao: MessagesDao
) : IAuthRepository {

    override fun logIn(
        visitor: Visitor,
        successAuthUi: () -> Unit,
        failAuthUi: () -> Unit,
        successAuthUx: suspend () -> Unit,
        failAuthUx: suspend () -> Unit,
        sync: suspend () -> Unit,
        updateCurrentReadMessageTime: (newTimeMarks: List<Pair<String, Long>>) -> Unit,
        updateCountUnreadMessages: (countNewMessages: Int, hasUserMessage: Boolean) -> Unit,
        getPersonPreview: suspend (personId: String) -> String?,
        updatePersonName: suspend (personId: String?, currentPersonName: String?) -> Unit,
        chatEventListener: ChatEventListener?
    ) {
        socketApi.setVisitor(
            visitor,
            successAuthUi,
            failAuthUi,
            successAuthUx,
            failAuthUx,
            sync,
            updateCurrentReadMessageTime,
            updateCountUnreadMessages,
            getPersonPreview,
            updatePersonName,
            chatEventListener
        )
        Log.i(TAG_AUTH_REPOSITORY,"login user with id: " + visitor.uuid + " successful")
    }

    override fun logOut(filesDir: File) {
        fileDao.getFilesNames().forEach { fileName ->
            fileDao.deleteFile(fileName)
            File(filesDir, fileName).delete()
        }
        messageDao.deleteAllMessages()
        Log.i(TAG_AUTH_REPOSITORY,"logOut user successful")
    }

}