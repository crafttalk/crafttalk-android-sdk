package com.crafttalk.chat.data.api.socket

import android.util.Log
import com.crafttalk.chat.data.helper.converters.text.convertFromHtmlToNormalString
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.message.Message as MessageSocket
import com.crafttalk.chat.data.local.db.entity.Message as MessageDB
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.tags.Tag
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatEventListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams.authType
import com.crafttalk.chat.utils.ChatParams.urlSocketHost
import com.crafttalk.chat.utils.ChatParams.urlSocketNameSpace
import com.crafttalk.chat.utils.ChatStatus
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET_EVENT
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

    private var socket: Socket? = null
    private lateinit var visitor: Visitor
    private lateinit var successAuthFun: () -> Unit
    private lateinit var failAuthFun: (ex: Throwable) -> Unit
    private var isOnline = false

    private var chatInternetConnectionListener: ChatInternetConnectionListener? = null
    private var chatMessageListener: ChatMessageListener? = null
    private var chatEventListener: ChatEventListener? = null

    var chatStatus = ChatStatus.NOT_ON_CHAT_SCREEN
    private val bufferMessages = mutableListOf<MessageSocket>()

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private fun initSocket() {
        if (socket == null) {
            socket = try {
                val manager = Manager(URI(urlSocketHost))
                manager.socket(urlSocketNameSpace).apply {
                    Log.d(TAG_SOCKET_EVENT, "setAllListeners")
                    isOnline = true
                    setAllListeners(this)
                }
            } catch (e: URISyntaxException) {
                Log.e(TAG_SOCKET, "fail init socket")
                isOnline = false
                chatInternetConnectionListener?.failConnect()
                null
            }
        }
    }

    fun setInternetConnectionListener(listener: ChatInternetConnectionListener) {
        this.chatInternetConnectionListener = listener
    }

    fun setMessageListener(listener: ChatMessageListener) {
        this.chatMessageListener = listener
    }

    fun cleanBufferMessages() {
        bufferMessages.clear()
    }

    fun setVisitor(visitor: Visitor, successAuth: () -> Unit, failAuth: (ex: Throwable) -> Unit, chatEventListener: ChatEventListener?) {
        this.successAuthFun = successAuth
        this.failAuthFun = failAuth
        this.chatEventListener = chatEventListener
        this.visitor = visitor
        initSocket()
        connectUser(socket!!)
    }

    private fun setAllListeners(socket: Socket) {
        socket.on("connect") {
            isOnline = true
            Log.d(TAG_SOCKET_EVENT, "connecting: ${socket.connected()}")
            try {
                authenticationUser(socket)
            } catch (ex: Throwable) { // add normal three exception
                failAuthFun(ex)
            }
        }

        socket.on("reconnect") {
            isOnline = true
            Log.d(TAG_SOCKET_EVENT, "reconnect")
            chatInternetConnectionListener?.reconnect()
        }

        socket.on("hide") {
            isOnline = true
            Log.d(TAG_SOCKET, "hide")
//            failAuthFun() // add tree exception
        }

        socket.on("authorized") {
            isOnline = true
            Log.d(TAG_SOCKET_EVENT, "authorized")
            successAuthFun()
        }

        socket.on("message") {
            isOnline = true
            Log.d("TEST_NOTIFICATION", "GET_MESSAGE")
            viewModelScope.launch {
                Log.d(TAG_SOCKET, "message, size = ${it.size}; it = $it")
                val messageJson = it[0] as JSONObject
                Log.d("SOCKET_API", "json message___ methon message - $messageJson")
                val messageSocket = gson.fromJson(messageJson.toString(), MessageSocket::class.java)
                when (messageSocket.messageType) {
                    MessageType.OPERATOR_IS_TYPING.valueType -> chatEventListener?.operatorStartWriteMessage()
                    MessageType.OPERATOR_STOPPED_TYPING.valueType -> chatEventListener?.operatorStopWriteMessage()
                    MessageType.VISITOR_MESSAGE.valueType -> {
                        chatEventListener?.operatorStopWriteMessage()
                    }
                }
                if (!messageJson.toString().contains(""""message":"\/start"""")) {
                    when {
                        (chatStatus == ChatStatus.NOT_ON_CHAT_SCREEN) && (messageSocket.messageType == MessageType.VISITOR_MESSAGE.valueType) -> {
                            bufferMessages.add(messageSocket)
                            chatMessageListener?.getNewMessages(bufferMessages.size)
                        }
                    }
                    insert(messageSocket)
                }
            }
        }

        socket.on("history-messages-loaded") {
            isOnline = true
            viewModelScope.launch {
                Log.d(TAG_SOCKET, "history-messages-loaded, ${it.size}")
                val listMessages = gson.fromJson(it[0].toString(), Array<MessageSocket>::class.java)

//                listMessages.forEach {
//                    Log.d(TAG_SOCKET, "history: $it")
//                }

                if (listMessages.isEmpty()) {
                    greet() // переделать, не ориентируясь на пустой лист сообщений
                } else {
                    marge(listMessages)
                }
            }
        }
        
        socket.on(Socket.EVENT_CONNECT) {
            isOnline = true
            chatInternetConnectionListener?.connect()
        }
        
        
        socket.on(Socket.EVENT_CONNECT_ERROR) {
            isOnline = false
            chatInternetConnectionListener?.lossConnection()
            Log.d(TAG_SOCKET_EVENT, "EVENT_CONNECT_ERROR")
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            isOnline = false
            chatInternetConnectionListener?.lossConnection()
            Log.d(TAG_SOCKET_EVENT, "EVENT_DISCONNECT")
        }
        socket.on(Socket.EVENT_CONNECT_TIMEOUT) {
            isOnline = false
            Log.d(TAG_SOCKET_EVENT, "EVENT_CONNECT_TIMEOUT")
        }
        socket.on(Socket.EVENT_ERROR) {
            isOnline = false
            Log.d(TAG_SOCKET_EVENT, "EVENT_ERROR")
        }
        socket.on(Socket.EVENT_RECONNECTING) {
            isOnline = false
            Log.d(TAG_SOCKET_EVENT, "EVENT_RECONNECTING")
        }
        socket.on(Socket.EVENT_RECONNECT_ATTEMPT) {
            isOnline = false
            Log.d(TAG_SOCKET_EVENT, "EVENT_RECONNECT_ATTEMPT")
        }
        socket.on(Socket.EVENT_RECONNECT_ERROR) {
            isOnline = false
            Log.d(TAG_SOCKET_EVENT, "EVENT_RECONNECT_ERROR")
        }
        socket.on(Socket.EVENT_RECONNECT_FAILED) {
            isOnline = false
            Log.d(TAG_SOCKET_EVENT, "EVENT_RECONNECT_FAILED")
        }
    }


    fun destroy() {
        isOnline = false
        chatInternetConnectionListener?.disconnect()
        socket?.disconnect()
        socket?.off()
        socket = null
        Log.d("TAG_SOCKET_EVENT", "destroy socket")
    }



    private fun connectUser(socket: Socket) {
        Log.d(TAG_SOCKET_EVENT, "Connect User isConnect - ${isOnline};")
        if (isOnline) {
            if (!socket.connected()) {
                socket.connect()
            } else {
                authenticationUser(socket)
            }
        }
    }

    private fun authenticationUser(socket: Socket) {
        Log.d(TAG_SOCKET_EVENT, "authenticationUser - ${visitor.getJsonObject()};\n ${visitor}")
        socket.emit(
            "me",
            visitor.getJsonObject(),
            authType!!.name in listOf(AuthType.AUTH_WITHOUT_FORM_WITH_HASH.name)
        )
    }

    private fun greet() {
        socket!!.emit("visitor-message", "/start", 1, null, 0, null, null, null)
    }

    fun sendMessage(message: String) {
        if (isOnline) {
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
    }

    fun selectAction(actionId: String) {
        if (isOnline) {
            socket!!.emit("visitor-action", actionId)
        }
    }

    fun sync(timestamp: Long) {
        socket!!.emit("history-messages-requested", timestamp)
    }


    private fun insert(messageSocket: MessageSocket) {
        when(messageSocket.messageType) {
            MessageType.VISITOR_MESSAGE.valueType -> {
                Log.d("REPOSITORY", "insertMessage $messageSocket")
                dao.insertMessage(MessageDB.map(messageSocket))
            }
            MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                Log.d("REPOSITORY", "updateMessage: messageType: ${messageSocket.messageType}")
                messageSocket.parentMessageId?.let {
                    dao.updateMessage(it, messageSocket.messageType)
                }
            }
        }
    }

    private fun marge(arrayMessages: Array<MessageSocket>) {
        val messagesFromDb = dao.getMessagesList()
        arrayMessages.sortWith(compareBy(MessageSocket::timestamp))
        arrayMessages.forEach { messageFromHistory ->
            val list = arrayListOf<Tag>()
            val message = messageFromHistory.message?.convertFromHtmlToNormalString(list)

            val messageCheckObj = MessageDB(
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