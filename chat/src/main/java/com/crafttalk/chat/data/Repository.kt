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

object Repository {

    private lateinit var dao: MessagesDao

    fun setDao(dao: MessagesDao){
        Repository.dao = dao
    }

    fun getVisitor(pref: SharedPreferences): Visitor? {
        return if (checkVisitorInPref(pref)) {
            Log.d("LOGGER", "getVisitor return obj - ${getVisitorFromPref(pref)}")
            getVisitorFromPref(pref)
        }
        else {
            Log.d("LOGGER", "getVisitor return null")
            null
        }
    }

    fun buildVisitor(pref: SharedPreferences, args: Array<out String>): Visitor? {
        val map: HashMap<String, Any> = HashMap()
        map["lastName"] = args[0]
        map["firstName"] = args[1]
        map["phone"] = args[2]
        return buildVisitorSaveToPref(pref, map)
    }

    fun getMessagesList(): LiveData<List<MessageDB>> {
        return dao.getMessages()
    }

    fun addNewMessageFromTheUser(textMessage: String) {
        dao.insertMessage(
            MessageDB(
                id = null,
                messageType = MessageType.VISITOR_MESSAGE.valueType,
                message = textMessage,
                isReply = false,
                timestamp = System.currentTimeMillis(),
                actions = null
            )
        )
    }


    // тут может быть и обновление обьекта и добавление нового
    fun addNewDataAboutMessagesFromTheServer(messageSocket: MessageSocket) {
        if (!messageSocket.isReply) {
            // сообщение от юзера
            val messages = dao.getLastMessages(5)
            messages.forEach {
                if (it.id == null && messageSocket.id != null && !it.isReply && it.message == messageSocket.message && Math.abs(it.timestamp - messageSocket.timestamp) <= 30 * 1000) {
                    dao.updateMessage(it.idKey, messageSocket.id)
                    return
                }
            }
        }
        else {
            // сообщение от сервера
            dao.insertMessage(
                MessageDB(
                    messageSocket.id,
                    messageSocket.message_type,
                    messageSocket.message,
                    messageSocket.actions,
                    messageSocket.isReply,
                    messageSocket.timestamp
                )
            )
        }

    }

}