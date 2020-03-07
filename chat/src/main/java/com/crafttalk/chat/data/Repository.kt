package com.crafttalk.chat.data

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.crafttalk.chat.data.local.pref.buildVisitorSaveToPref
import com.crafttalk.chat.data.local.pref.checkVisitorInPref
import com.crafttalk.chat.data.local.pref.getVisitorFromPref
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.entity.Message as MessageDB
import com.crafttalk.chat.data.remote.Message as MessageSocket
import com.crafttalk.chat.data.model.MessageType
import com.crafttalk.chat.data.model.Visitor
import com.crafttalk.chat.data.remote.socket_service.SocketAPI

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
        map["lastName"] = args[0]
        map["firstName"] = args[1]
        map["phone"] = args[2]
        return buildVisitorSaveToPref(map)
    }

    fun getMessagesList(): LiveData<List<MessageDB>> {
        return dao.getMessagesLiveData()
    }

    fun getMessageFromServer(messageSocket: MessageSocket) {
        when(messageSocket.message_type) {
            MessageType.VISITOR_MESSAGE.valueType -> {
                dao.insertMessage(
                    MessageDB(
                        messageSocket.id,
                        messageSocket.message_type,
                        messageSocket.parentMessageId,
                        messageSocket.message,
                        messageSocket.actions,
                        messageSocket.isReply,
                        messageSocket.timestamp,
                        messageSocket.operator_name ?: "Вы"
                    )
                )
            }
            MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                Log.d("REPOSITORY", "updateMessage: messageType: ${messageSocket.message_type}")
                dao.updateMessage(messageSocket.parentMessageId, messageSocket.message_type)
            }
        }
    }

    fun syncData() {
        val timeStamp = dao.getLastTime()
        SocketAPI.sync(timeStamp)
    }

    fun margeMessages(arrayMessages: Array<MessageSocket>) {
        Log.d("REPOSITORY", "margeMessages: size = ${arrayMessages.size}")
        val messagesFromDb = dao.getMessagesList()
        arrayMessages.sortWith(compareBy(MessageSocket::timestamp))
        arrayMessages.forEach { messageFromHistory ->
            val messageCheckObj = MessageDB(
                messageFromHistory.id,
                messageFromHistory.message_type,
                messageFromHistory.parentMessageId,
                messageFromHistory.message,
                messageFromHistory.actions,
                messageFromHistory.isReply,
                messageFromHistory.timestamp,
                messageFromHistory.operator_name ?: "Вы"
            )
            Log.d("REPOSITORY", "margeMessages: messageCheckObj = $messageCheckObj")

            if (!messagesFromDb.any { it.id == messageCheckObj.id } && messageCheckObj.messageType in listOf(MessageType.VISITOR_MESSAGE.valueType)) {
                dao.insertMessage(messageCheckObj)
                Log.d("REPOSITORY", "insert message $messageCheckObj")
            }else {
                Log.d("REPOSITORY", "update message id - ${messageCheckObj.parentMsgId}, type - ${messageCheckObj.messageType}")
                messageCheckObj.parentMsgId?.let {parentId ->
                    dao.updateMessage(parentId, messageCheckObj.messageType)
                }
            }
        }
    }

}