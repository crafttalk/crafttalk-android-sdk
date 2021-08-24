package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.local.db.dao.FileDao
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IAuthRepository
import com.crafttalk.chat.presentation.ChatEventListener
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
        successAuthUi: (() -> Unit)?,
        failAuthUi: (() -> Unit)?,
        successAuthUx: suspend () -> Unit,
        failAuthUx: suspend () -> Unit,
        sync: suspend () -> Unit,
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
            getPersonPreview,
            updatePersonName,
            chatEventListener
        )
    }

    override fun logOut(uuid: String, filesDir: File) {
        fileDao.getFilesNames(uuid).forEach { fileName ->
            fileDao.deleteFile(uuid, fileName)
            File(filesDir, fileName).delete()
        }
        messageDao.deleteAllMessages(uuid)
    }

}