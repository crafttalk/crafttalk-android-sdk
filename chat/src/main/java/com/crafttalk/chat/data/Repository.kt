package com.crafttalk.chat.data

import android.content.SharedPreferences
import android.text.Html
import android.util.Log
import androidx.lifecycle.LiveData
import com.crafttalk.chat.data.local.pref.buildVisitorSaveToPref
import com.crafttalk.chat.data.local.pref.checkVisitorInPref
import com.crafttalk.chat.data.local.pref.getVisitorFromPref
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.entity.Message as MessageDB
import com.crafttalk.chat.data.remote.pojo.Message as MessageSocket
import com.crafttalk.chat.data.model.MessageType
import com.crafttalk.chat.data.model.Visitor
import com.crafttalk.chat.data.remote.socket_service.SocketAPI
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET

object Repository {

    private lateinit var dao: MessagesDao

    fun setDao(dao: MessagesDao){
        Repository.dao = dao
    }

    fun getVisitor(pref: SharedPreferences): Visitor? {
        return if (checkVisitorInPref(pref)) {
            Log.d("REPOSITORY", "getVisitor return obj - ${getVisitorFromPref(pref)}")
            getVisitorFromPref(pref)
        }
        else {
            Log.d("REPOSITORY", "getVisitor return null")
            null
        }
    }

    fun buildVisitor(args: Array<out String>): Visitor? {
        val map: HashMap<String, Any> = HashMap()
        map["firstName"] = args[0]
        map["lastName"] = args[1]
        map["phone"] = args[2]
        return buildVisitorSaveToPref(map)
    }

    fun hasVisitor(pref: SharedPreferences): Boolean {
        return checkVisitorInPref(pref)
    }

    fun getMessagesList(): LiveData<List<MessageDB>> {
        return dao.getMessagesLiveData()
    }

    fun insertNewMessageFromServer(messageSocket: MessageSocket) {
        when(messageSocket.messageType) {
            MessageType.VISITOR_MESSAGE.valueType -> {
                Log.d("REPOSITORY", "insertMessage ${messageSocket}")


                dao.insertMessage(
                    MessageDB(
                        id = messageSocket.id,
                        messageType = messageSocket.messageType,
                        isReply = messageSocket.isReply,
                        parentMsgId = messageSocket.parentMessageId,
                        timestamp = messageSocket.timestamp,
                        message = messageSocket.message,
                        actions = messageSocket.actions,
                        attachmentUrl = messageSocket.attachmentUrl,
                        attachmentType = messageSocket.attachmentType,
                        attachmentName = messageSocket.attachmentName,
                        operatorName = if(messageSocket.operatorName == null || !messageSocket.isReply) "Вы" else messageSocket.operatorName
                    )
                )
            }
            MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                Log.d("REPOSITORY", "updateMessage: messageType: ${messageSocket.messageType}")
                messageSocket.parentMessageId?.let {
                    dao.updateMessage(it, messageSocket.messageType)
                }
            }
        }
    }

    fun syncData() {
        val timeStamp = 0L//dao.getLastTime()
        SocketAPI.sync(timeStamp)
    }

    fun margeMessages(arrayMessages: Array<MessageSocket>) {
        Log.d("REPOSITORY", "margeMessages: size = ${arrayMessages.size}")
        val messagesFromDb = dao.getMessagesList()
        arrayMessages.sortWith(compareBy(MessageSocket::timestamp))
        arrayMessages.forEach { messageFromHistory ->
            val messageCheckObj = MessageDB(
                id = messageFromHistory.id,
                messageType = messageFromHistory.messageType,
                isReply = messageFromHistory.isReply,
                parentMsgId = messageFromHistory.parentMessageId,
                timestamp = messageFromHistory.timestamp,
                message = messageFromHistory.message,
                actions = messageFromHistory.actions,
                attachmentUrl = messageFromHistory.attachmentUrl,
                attachmentType = messageFromHistory.attachmentType,
                attachmentName = messageFromHistory.attachmentName,
                operatorName = if(messageFromHistory.operatorName == null || !messageFromHistory.isReply) "Вы" else messageFromHistory.operatorName
            )
            when (messageCheckObj.messageType) {
                MessageType.VISITOR_MESSAGE.valueType -> {
                    if (messageCheckObj.isReply) {
                        // serv
                        if (!messagesFromDb.any { it.id == messageCheckObj.id }) {
                            dao.insertMessage(messageCheckObj)
                        }
                    }
                    else {
                        // user
                        if (messageCheckObj !in messagesFromDb) {
                            Log.d("REPOSITORY", "insert message $messageCheckObj")
                            dao.insertMessage(messageCheckObj)
                        }
                    }
                }
                MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                    Log.d("REPOSITORY", "update message id - ${messageCheckObj.parentMsgId}, type - ${messageCheckObj.messageType}")
                    messageCheckObj.parentMsgId?.let { parentId ->
                        dao.updateMessage(parentId, messageCheckObj.messageType)
                    }
                }
            }

        }
    }

}