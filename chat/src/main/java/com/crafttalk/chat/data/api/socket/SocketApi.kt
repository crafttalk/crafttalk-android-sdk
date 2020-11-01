package com.crafttalk.chat.data.api.socket

import android.util.Log
import com.crafttalk.chat.data.helper.converters.text.convertFromHtmlToNormalString
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.message.Message
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.tags.Tag
import com.crafttalk.chat.initialization.ChatInternetConnectionListener
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET
import com.github.nkzawa.socketio.client.Manager
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException

class SocketApi constructor(
    private val dao: MessagesDao,
    private val gson: Gson
) {

    private lateinit var visitor: Visitor
    private lateinit var successAuthFun: () -> Unit
    private lateinit var failAuthFun: (ex: Throwable) -> Unit
    private var socket: Socket? = null

    private lateinit var chatInternetConnectionListener: ChatInternetConnectionListener
    private lateinit var chatMessageListener: ChatMessageListener


    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private fun initSocket() {
        socket = try {
            val manager = Manager(URI(ChatAttr.getInstance().urlSocketHost))
            manager.socket(ChatAttr.getInstance().urlSocketNameSpace)
        } catch (e: URISyntaxException) {
            Log.e(TAG_SOCKET, "fail init socket")
            chatInternetConnectionListener.failConnect()
            null
        }
    }

    fun setInternetConnectionListener(listener: ChatInternetConnectionListener) {
        this.chatInternetConnectionListener = listener
    }

    fun setMessageListener(listener: ChatMessageListener) {
        this.chatMessageListener = listener
    }

    fun setVisitor(visitor: Visitor, successAuth: () -> Unit, failAuth: (ex: Throwable) -> Unit) {
        this.successAuthFun = successAuth
        this.failAuthFun = failAuth
        this.visitor = visitor
        initSocket()
        connectUser(socket!!)
    }

    private fun setAllListeners(socket: Socket) {
        socket.on("connect") {
            Log.d(TAG_SOCKET, "connecting: ${socket.connected()}")
            try {
                authenticationUser(socket)
            } catch (ex: Throwable) { // add normal three exception
                failAuthFun(ex)
            }
        }

        socket.on("reconnect") {
            Log.d(TAG_SOCKET, "reconnect")
            chatInternetConnectionListener.reconnect()
        }

        socket.on("hide") {
            Log.d(TAG_SOCKET, "hide")
//            failAuthFun() // add tree exception
        }

        socket.on("authorized") {
            Log.d(TAG_SOCKET, "authorized")
            successAuthFun()
        }

        socket.on("message") {
            Log.d("TEST_NOTIFICATION", "GET_MESSAGE")
            viewModelScope.launch {
                Log.d(TAG_SOCKET, "message, size = ${it.size}; it = $it")
                val messageJson = it[0] as JSONObject
                Log.d("SOCKET_API", "json message___ methon message - $messageJson")
                val messageSocket = gson.fromJson(messageJson.toString(), Message::class.java)
                if (!messageJson.toString().contains(""""message":"\/start"""")) {
                    insert(messageSocket)
                }
            }
        }

        socket.on("history-messages-loaded") {
            viewModelScope.launch {
                Log.d(TAG_SOCKET, "history-messages-loaded, ${it.size}")
                val listMessages = gson.fromJson(it[0].toString(), Array<Message>::class.java)

                listMessages.forEach {
                    Log.d(TAG_SOCKET, "history: $it")
                }

                if (listMessages.isEmpty()) {
                    greet() // переделать, не ориентируясь на пустой лист сообщений
                } else {
                    marge(listMessages)
                }
            }
        }

        socket.on(Socket.EVENT_CONNECT) {
            chatInternetConnectionListener.connect()
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) {
            chatInternetConnectionListener.lossConnection()
            Log.d(TAG_SOCKET, "EVENT_CONNECT_ERROR")
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            chatInternetConnectionListener.lossConnection()
            Log.d(TAG_SOCKET, "EVENT_DISCONNECT")
        }
        socket.on(Socket.EVENT_CONNECT_TIMEOUT) {
//            Log.d(TAG_SOCKET, "EVENT_CONNECT_TIMEOUT")
        }
        socket.on(Socket.EVENT_ERROR) {
//            Log.d(TAG_SOCKET, "EVENT_ERROR")
        }
        socket.on(Socket.EVENT_RECONNECTING) {
//            Log.d(TAG_SOCKET, "EVENT_RECONNECTING")
        }
        socket.on(Socket.EVENT_RECONNECT_ATTEMPT) {
//            Log.d(TAG_SOCKET, "EVENT_RECONNECT_ATTEMPT")
        }
        socket.on(Socket.EVENT_RECONNECT_ERROR) {
//            Log.d(TAG_SOCKET, "EVENT_RECONNECT_ERROR")
        }
        socket.on(Socket.EVENT_RECONNECT_FAILED) {
//            Log.d(TAG_SOCKET, "EVENT_RECONNECT_FAILED")
        }
    }


    fun destroy() {
        chatInternetConnectionListener.disconnect()
        socket?.disconnect()
        socket?.off()
        Log.d("TEST_NOTIFICATION", "destroy socket")
    }

    private fun connectUser(socket: Socket) {
        if (!socket.connected()) {
            setAllListeners(socket)
            socket.connect()
        } else {
            authenticationUser(socket)
        }
    }

    private fun authenticationUser(socket: Socket) {
        Log.d(TAG_SOCKET, "authenticationUser - ${visitor.getJsonObject()};\n ${visitor}")
        socket.emit(
            "me",
            visitor.getJsonObject(),
            ChatAttr.getInstance().authType.name in listOf(AuthType.AUTH_WITHOUT_FORM_WITH_HASH.name)
        )
    }

    private fun greet() {
        socket!!.emit("visitor-message", "/start", 1, null, 0, null, null, null)
    }

    fun sendMessage(message: String) {
        socket!!.emit(
            "visitor-message",
            message,
            MessageType.VISITOR_MESSAGE.valueType,
            null,
            0,
            null,
            null,
            null
        )
    }

    fun selectAction(actionId: String) {
        socket!!.emit("visitor-action", actionId)
    }

    fun sync(timestamp: Long) {
        socket!!.emit("history-messages-requested", timestamp)
    }


    fun insert(messageSocket: Message) {
        when(messageSocket.messageType) {
            MessageType.VISITOR_MESSAGE.valueType -> {
                Log.d("REPOSITORY", "insertMessage $messageSocket")
                dao.insertMessage(com.crafttalk.chat.data.local.db.entity.Message.map(messageSocket))
            }
            MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                Log.d("REPOSITORY", "updateMessage: messageType: ${messageSocket.messageType}")
                messageSocket.parentMessageId?.let {
                    dao.updateMessage(it, messageSocket.messageType)
                }
            }
        }
    }

    fun marge(arrayMessages: Array<Message>) {
        val messagesFromDb = dao.getMessagesList()
        arrayMessages.sortWith(compareBy(Message::timestamp))
        arrayMessages.forEach { messageFromHistory ->
            val list = arrayListOf<Tag>()
            val message = messageFromHistory.message?.convertFromHtmlToNormalString(list)

            val messageCheckObj = com.crafttalk.chat.data.local.db.entity.Message(
                id = messageFromHistory.id,
                messageType = messageFromHistory.messageType,
                isReply = messageFromHistory.isReply,
                parentMsgId = messageFromHistory.parentMessageId,
                timestamp = messageFromHistory.timestamp,
                message = message,
                spanStructureList = list,
                actions = messageFromHistory.actions,
                attachmentUrl = messageFromHistory.attachmentUrl,
                attachmentType = messageFromHistory.attachmentType,
                attachmentName = messageFromHistory.attachmentName,
                operatorName = if (messageFromHistory.operatorName == null || !messageFromHistory.isReply) "Вы" else messageFromHistory.operatorName,
                height = 0,
                width = 0
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