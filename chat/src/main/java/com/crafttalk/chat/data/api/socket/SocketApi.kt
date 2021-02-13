package com.crafttalk.chat.data.api.socket

import android.util.Log
import com.crafttalk.chat.data.helper.converters.text.convertTextToNormalString
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.domain.entity.auth.Visitor
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
import com.crafttalk.chat.data.local.db.entity.Message as MessageDB
import com.crafttalk.chat.domain.entity.message.Message as MessageSocket

class SocketApi constructor(
    private val dao: MessagesDao,
    private val gson: Gson
) {

    private var socket: Socket? = null
    private lateinit var visitor: Visitor
    private var successAuthUiFun: () -> Unit = {}
    private var failAuthUiFun: () -> Unit = {}
    private var successAuthUxFun: () -> Unit = {}
    private var failAuthUxFun: () -> Unit = {}
    private var getPersonPreview: (personId: String) -> String? = { null }
    private var isOnline = false

    private var chatInternetConnectionListener: ChatInternetConnectionListener? = null
    private var chatMessageListener: ChatMessageListener? = null
    private var chatEventListener: ChatEventListener? = null

    var chatStatus = ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP
    private val bufferMessages = mutableListOf<MessageSocket>()

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    private var isMessageSent = false

    private fun initSocket() {
        if (socket == null) {
            socket = try {
                val manager = Manager(URI(urlSocketHost))
                manager.socket(urlSocketNameSpace).apply {
                    isOnline = true
                    setAllListeners(this)
                }
            } catch (e: URISyntaxException) {
                failAuthUiFun()
                viewModelScope.launch {
                    failAuthUxFun()
                }
                isOnline = false
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

    fun setVisitor(
        visitor: Visitor,
        successAuthUi: (() -> Unit)?,
        failAuthUi: (() -> Unit)?,
        successAuthUx: () -> Unit,
        failAuthUx: () -> Unit,
        getPersonPreview: (personId: String) -> String?,
        chatEventListener: ChatEventListener?
    ) {
        successAuthUi?.let { this.successAuthUiFun = it }
        this.successAuthUxFun = successAuthUx
        failAuthUi?.let { this.failAuthUiFun = it }
        this.failAuthUxFun = failAuthUx
        this.getPersonPreview = getPersonPreview
        chatEventListener?.let { this.chatEventListener = it }
        this.visitor = visitor
        initSocket()
        socket?.let {
            connectUser(it)
        }
    }

    private fun setAllListeners(socket: Socket) {

        socket.on("connect") {
            Log.d(TAG_SOCKET_EVENT, "connect connecting - ${socket.connected()}")
            isOnline = true
            authenticationUser(socket)
        }

        socket.on("reconnect") {
            Log.d(TAG_SOCKET_EVENT, "reconnect")
            isOnline = true
            chatInternetConnectionListener?.reconnect()
        }

        socket.on("hide") {
            Log.d(TAG_SOCKET_EVENT, "hide")
            isOnline = true
            failAuthUiFun()
            viewModelScope.launch {
                failAuthUxFun()
            }
        }

        socket.on("authorized") {
            Log.d(TAG_SOCKET_EVENT, "authorized")
            isOnline = true
            successAuthUiFun()
            viewModelScope.launch {
                successAuthUxFun()
            }
            if (chatStatus == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP) {
                viewModelScope.launch {
                    sync(0)
                }
            }
        }

        socket.on("authorization-required") {
            Log.d(TAG_SOCKET_EVENT, "authorization-required")
            isOnline = true
            if (it[0] as Boolean) {
                socket.emit(
                    "authorize",
                    visitor.getJsonObject(),
                    visitor.phone?.length ?: 0
                )
            }
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
                        (chatStatus == ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP) && (messageSocket.messageType == MessageType.VISITOR_MESSAGE.valueType) -> {
                            bufferMessages.add(messageSocket)
                            chatMessageListener?.getNewMessages(bufferMessages.size)
                        }
                    }
                    updateDataInDatabase(messageSocket)
                }
            }
        }

        socket.on("history-messages-loaded") {
            Log.d(TAG_SOCKET_EVENT, "history-messages-loaded, ${it.size}")
            isOnline = true
            viewModelScope.launch {
                val listMessages = gson.fromJson(it[0].toString(), Array<MessageSocket>::class.java)

//                listMessages.forEach {
//                    Log.d(TAG_SOCKET, "history: $it")
//                }

                if (listMessages.isEmpty() && !isMessageSent) {
                    greet() // переделать, не ориентируясь на пустой лист сообщений
                } else {
                    marge(listMessages)
                }
            }
        }


        socket.on(Socket.EVENT_CONNECT) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_CONNECT")
            isOnline = true
            chatInternetConnectionListener?.connect()
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_DISCONNECT")
            isOnline = false
            chatInternetConnectionListener?.lossConnection()
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_CONNECT_ERROR")
            isOnline = false
            chatInternetConnectionListener?.failConnect()
        }
        socket.on(Socket.EVENT_RECONNECT_ERROR) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_RECONNECT_ERROR")
            isOnline = false
        }
        socket.on(Socket.EVENT_RECONNECT_FAILED) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_RECONNECT_FAILED")
            isOnline = false
        }

        socket.on(Socket.EVENT_CONNECT_TIMEOUT) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_CONNECT_TIMEOUT")
            isOnline = false
            failAuthUiFun()
            viewModelScope.launch {
                failAuthUxFun()
            }
        }

    }


    fun destroy() {
        isOnline = false
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    private fun connectUser(socket: Socket) {
        if (isOnline) {
            if (!socket.connected()) {
                socket.connect()
            } else {
                authenticationUser(socket)
            }
        }
    }

    private fun authenticationUser(socket: Socket) {
        try {
            socket.emit(
                "me",
                visitor.getJsonObject(),
                when {
                    authType == null || authType!!.name == AuthType.AUTH_WITH_FORM.name -> false
                    authType!!.name == AuthType.AUTH_WITHOUT_FORM.name -> true
                    else -> false
                }
            )
        } catch (ex: Throwable) {
            failAuthUiFun()
            viewModelScope.launch {
                failAuthUxFun()
            }
        }
    }

    private fun greet() {
        isMessageSent = true
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
        //dao.getLastTime()
        socket!!.emit("history-messages-requested", timestamp, visitor.token)
    }


    private fun updateDataInDatabase(messageSocket: MessageSocket) {
        when {
            (MessageType.VISITOR_MESSAGE.valueType == messageSocket.messageType) && (!messageSocket.attachmentUrl.isNullOrEmpty() || !messageSocket.message.isNullOrEmpty()) -> {
                dao.insertMessage(MessageDB.map(visitor.uuid, messageSocket, messageSocket.operatorId?.let { getPersonPreview(it) }))
            }
            (MessageType.RECEIVED_BY_MEDIATO.valueType == messageSocket.messageType) || (MessageType.RECEIVED_BY_OPERATOR.valueType == messageSocket.messageType) -> {
                messageSocket.parentMessageId?.let { parentId ->
                    dao.updateMessage(visitor.uuid, parentId, messageSocket.messageType)
                }
            }
        }
    }

    private fun marge(arrayMessages: Array<MessageSocket>) {
        arrayMessages.sortWith(compareBy(MessageSocket::timestamp))
        arrayMessages.forEach { messageFromHistory ->
            val list = arrayListOf<Tag>()
            val message = messageFromHistory.message?.convertTextToNormalString(list)

            when (messageFromHistory.messageType) {
                MessageType.VISITOR_MESSAGE.valueType -> {
                    if (messageFromHistory.isReply) {
                        // operator
                        val messagesFromDb = dao.getMessageById(visitor.uuid, messageFromHistory.id)
                        if (messagesFromDb == null) {
                            updateDataInDatabase(messageFromHistory)
                        }
                    }
                    else {
                        // user
                        val messagesFromDb = dao.getMessageByContent(visitor.uuid, message, messageFromHistory.attachmentUrl)
                        val messageCheckObj = MessageDB(
                            uuid = visitor.uuid,
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
                            operatorPreview = null,
                            height = 0,
                            width = 0
                        )
                        if (messageCheckObj !in messagesFromDb) {
                            updateDataInDatabase(messageFromHistory)
                        }
                    }
                }
                MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                    updateDataInDatabase(messageFromHistory)
                }
            }

        }
    }

}