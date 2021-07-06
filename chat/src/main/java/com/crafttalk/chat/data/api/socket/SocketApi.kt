package com.crafttalk.chat.data.api.socket

import android.content.Context
import android.util.Log
import com.crafttalk.chat.data.helper.converters.text.convertTextToNormalString
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.tags.Tag
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatEventListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.presentation.helper.ui.getSizeMediaFile
import com.crafttalk.chat.presentation.helper.ui.getWeightFile
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams.authMode
import com.crafttalk.chat.utils.ChatParams.initialMessageMode
import com.crafttalk.chat.utils.ChatParams.urlSocketHost
import com.crafttalk.chat.utils.ChatParams.urlSocketNameSpace
import com.crafttalk.chat.utils.ChatParams.urlSyncHistory
import com.crafttalk.chat.utils.ChatStatus
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET_EVENT
import com.crafttalk.chat.utils.InitialMessageMode
import io.socket.client.Manager
import io.socket.client.Socket
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import com.crafttalk.chat.domain.entity.message.Message as MessageSocket

class SocketApi constructor(
    private val dao: MessagesDao,
    private val gson: Gson,
    private val context: Context
) {

    private var socket: Socket? = null
    private lateinit var visitor: Visitor
    private var successAuthUiFun: () -> Unit = {}
    private var failAuthUiFun: () -> Unit = {}
    private var successAuthUxFun: () -> Unit = {}
    private var failAuthUxFun: () -> Unit = {}
    private var getPersonPreview: (personId: String) -> String? = { null }
    private var newMessagesStartTime: Long? = null
    private var isUploadHistory: Boolean = false
    private var isAuthorized: Boolean = false

    private var chatInternetConnectionListener: ChatInternetConnectionListener? = null
    private var chatMessageListener: ChatMessageListener? = null
    private var chatEventListener: ChatEventListener? = null

    var chatStatus = ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP
    private val bufferMessages = mutableListOf<MessageSocket>()

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    private var isSendGreet = false

    fun initSocket() {
        if (socket == null) {
            socket = try {
                val manager = Manager(URI(urlSocketHost))
                manager.socket(urlSocketNameSpace).apply {
                    setAllListeners(this)
                }
            } catch (e: URISyntaxException) {
                failAuthUiFun()
                viewModelScope.launch {
                    failAuthUxFun()
                }
                isAuthorized = false
                null
            }
        }
    }

    fun destroySocket() {
        isSendGreet = false
        socket?.off()
        socket = null
    }

    fun dropChat() {
        socket?.disconnect()
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
        socket?.let {
            connectUser(it)
        }
    }

    private fun setAllListeners(socket: Socket) {

        socket.on("connect") {
            Log.d(TAG_SOCKET_EVENT, "connect connecting - ${socket.connected()}")
            authenticationUser(socket)
        }

        socket.on("reconnect") {
            Log.d(TAG_SOCKET_EVENT, "reconnect")
            chatInternetConnectionListener?.reconnect()
        }

        socket.on("hide") {
            Log.d(TAG_SOCKET_EVENT, "hide")
            isAuthorized = false
            failAuthUiFun()
            viewModelScope.launch {
                failAuthUxFun()
            }
        }

        socket.on("authorized") {
            Log.d(TAG_SOCKET_EVENT, "authorized")
            isAuthorized = true
            successAuthUiFun()
            viewModelScope.launch {
                successAuthUxFun()
            }
            if ((initialMessageMode == InitialMessageMode.SEND_AFTER_AUTHORIZATION) || (initialMessageMode == InitialMessageMode.SEND_ON_OPEN && chatStatus == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP)) {
                greet()
            }
            if (chatStatus == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP) {
                viewModelScope.launch {
                    uploadNewMessages()
                }
            }
        }

        socket.on("authorization-required") {
            Log.d(TAG_SOCKET_EVENT, "authorization-required")
            if (it[0] as Boolean) {
                socket.emit(
                    "authorize",
                    visitor.getJsonObject(),
                    visitor.phone?.length ?: 0
                )
            }
        }

        socket.on("message") {
            Log.d("TEST_NOTIFICATION", "GET_MESSAGE")
            viewModelScope.launch {
                Log.d(TAG_SOCKET, "message, size = ${it.size}; it = $it")
                val messageJson = it[0] as JSONObject
                Log.d(TAG_SOCKET, "json message___ methon message - $messageJson")
                val messageSocket = gson.fromJson(messageJson.toString().replace("&amp;", "&"), MessageSocket::class.java)
                when (messageSocket.messageType) {
                    MessageType.OPERATOR_IS_TYPING.valueType -> chatEventListener?.operatorStartWriteMessage()
                    MessageType.OPERATOR_STOPPED_TYPING.valueType -> chatEventListener?.operatorStopWriteMessage()
                    MessageType.VISITOR_MESSAGE.valueType -> {
                        chatEventListener?.operatorStopWriteMessage()
                    }
                    MessageType.FINISH_DIALOG.valueType -> chatEventListener?.finishDialog()
                }
                if (!messageJson.toString().contains(""""message":"\/start"""") && (messageSocket.id != null || !dao.isNotEmpty(visitor.uuid))) {
                    when {
                        (chatStatus == ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP) && (messageSocket.messageType == MessageType.VISITOR_MESSAGE.valueType) -> {
                            bufferMessages.add(messageSocket)
                            chatMessageListener?.getNewMessages(bufferMessages.size)
                        }
                    }
                    if (messageSocket.id == null) {
                        messageSocket.id = System.currentTimeMillis().toString()
                    }
                    updateDataInDatabase(messageSocket)
                }
            }
        }

        socket.on("history-messages-loaded") {
            Log.d(TAG_SOCKET_EVENT, "history-messages-loaded, ${it.size}")
            viewModelScope.launch {
                val listMessages = gson.fromJson(it[0].toString().replace("&amp;", "&"), Array<MessageSocket>::class.java)

//                listMessages.forEach {
//                    Log.d(TAG_SOCKET, "history: $it")
//                }

                if (listMessages.isNotEmpty()) {
                    marge(listMessages)
                    if (listMessages.isNotEmpty() && newMessagesStartTime != null && listMessages[0].timestamp > newMessagesStartTime!!) {
                        newMessagesStartTime = listMessages[0].timestamp
                        socket.emit("history-messages-requested", newMessagesStartTime, visitor.token, urlSyncHistory)
                    } else {
                        newMessagesStartTime = null
                    }
                    isUploadHistory = false
                }
            }
        }

        socket.on(Socket.EVENT_CONNECT) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_CONNECT")
            chatInternetConnectionListener?.connect()
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_DISCONNECT")
            isAuthorized = false
            chatInternetConnectionListener?.lossConnection()
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_CONNECT_ERROR")
            isAuthorized = false
            chatInternetConnectionListener?.failConnect()
        }
        socket.on(Socket.EVENT_RECONNECT_ERROR) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_RECONNECT_ERROR")
            isAuthorized = false
        }
        socket.on(Socket.EVENT_RECONNECT_FAILED) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_RECONNECT_FAILED")
            isAuthorized = false
        }
        socket.on(Socket.EVENT_CONNECT_TIMEOUT) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_CONNECT_TIMEOUT")
            isAuthorized = false
            failAuthUiFun()
            viewModelScope.launch {
                failAuthUxFun()
            }
        }
    }

    private fun connectUser(socket: Socket) {
        if (!socket.connected()) {
            socket.connect()
        } else {
            authenticationUser(socket)
        }
    }

    private fun authenticationUser(socket: Socket) {
        if (isAuthorized && socket.connected()) {
            successAuthUiFun()
            viewModelScope.launch {
                successAuthUxFun()
            }
            if (initialMessageMode == InitialMessageMode.SEND_ON_OPEN && chatStatus == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP) {
                greet()
            }
            if (chatStatus == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP) {
                viewModelScope.launch {
                    uploadNewMessages()
                }
            }
        } else {
            try {
                socket.emit(
                    "me",
                    visitor.getJsonObject(),
                    authMode == AuthType.AUTH_WITHOUT_FORM
                )
            } catch (ex: Throwable) {
                failAuthUiFun()
                viewModelScope.launch {
                    failAuthUxFun()
                }
            }
        }
    }

    private fun greet() {
        if (socket != null && socket!!.connected() && !isSendGreet) {
            isSendGreet = true
            socket?.emit("visitor-message", "/start", MessageType.VISITOR_MESSAGE.valueType, null, 0, null, null, null)
        }
    }

    fun sendMessage(message: String) {
        socket?.emit("visitor-message", message, MessageType.VISITOR_MESSAGE.valueType, null, 0, null, null, null)
    }

    fun selectAction(actionId: String) {
        socket?.emit("visitor-action", actionId)
    }

    fun giveFeedbackOnOperator(countStars: Int) {
        socket?.emit("visitor-message", "", MessageType.UPDATE_DIALOG_SCORE.valueType, null, countStars, null, null)
    }

    fun sync(timestamp: Long) {
        isUploadHistory = true
        socket!!.emit("history-messages-requested", timestamp, visitor.token, urlSyncHistory)
    }

    private fun uploadNewMessages() {
        viewModelScope.launch {
            dao.getLastMessageTime(visitor.uuid)?.let { time ->
                isUploadHistory = false
                newMessagesStartTime = time
                socket!!.emit("history-messages-requested", 0, visitor.token, urlSyncHistory)
            }
        }
    }

    private fun updateDataInDatabase(messageSocket: MessageSocket) {
        when {
            (MessageType.VISITOR_MESSAGE.valueType == messageSocket.messageType) && (!messageSocket.attachmentUrl.isNullOrEmpty() && messageSocket.attachmentType == "IMAGE") -> {
                messageSocket.attachmentUrl?.let {
                    getSizeMediaFile(context, it) { height, width ->
                        viewModelScope.launch {
                            dao.insertMessage(MessageEntity.map(visitor.uuid, messageSocket, isUploadHistory, messageSocket.operatorId?.let { getPersonPreview(it) }, height, width))
                        }
                    }
                }
            }
            (MessageType.VISITOR_MESSAGE.valueType == messageSocket.messageType) && (!messageSocket.attachmentUrl.isNullOrEmpty() && messageSocket.attachmentType == "FILE") -> {
                messageSocket.attachmentUrl?.let {
                    getWeightFile(it) { size ->
                        viewModelScope.launch {
                            dao.insertMessage(MessageEntity.map(visitor.uuid, messageSocket, isUploadHistory, messageSocket.operatorId?.let { getPersonPreview(it) }, attachmentSize = size))
                        }
                    }
                }
            }
            (MessageType.VISITOR_MESSAGE.valueType == messageSocket.messageType) && (!messageSocket.attachmentUrl.isNullOrEmpty() || !messageSocket.message.isNullOrEmpty()) -> {
                dao.insertMessage(MessageEntity.map(visitor.uuid, messageSocket, isUploadHistory, messageSocket.operatorId?.let { getPersonPreview(it) }))
            }
            (MessageType.RECEIVED_BY_MEDIATO.valueType == messageSocket.messageType) || (MessageType.RECEIVED_BY_OPERATOR.valueType == messageSocket.messageType) -> {
                messageSocket.parentMessageId?.let { parentId ->
                    dao.updateMessage(visitor.uuid, parentId, messageSocket.messageType)
                }
            }
            (MessageType.TRANSFER_TO_OPERATOR.valueType == messageSocket.messageType) -> {
                dao.insertMessage(MessageEntity.map(visitor.uuid, messageSocket, isUploadHistory, messageSocket.operatorId?.let { getPersonPreview(it) }))
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
                        val messagesFromDb = dao.getMessageById(visitor.uuid, messageFromHistory.id!!)
                        if (messagesFromDb == null) {
                            updateDataInDatabase(messageFromHistory)
                        }
                    }
                    else {
                        // user
                        val messagesFromDb = dao.getMessageByContent(visitor.uuid, message, messageFromHistory.attachmentUrl)
                        val messageCheckObj = MessageEntity(
                            uuid = visitor.uuid,
                            id = messageFromHistory.id!!,
                            messageType = messageFromHistory.messageType,
                            isReply = messageFromHistory.isReply,
                            parentMsgId = messageFromHistory.parentMessageId,
                            timestamp = messageFromHistory.timestamp,
                            message = message,
                            spanStructureList = list,
                            actions = messageFromHistory.actions?.let { ActionEntity.map(it) },
                            attachmentUrl = messageFromHistory.attachmentUrl,
                            attachmentType = messageFromHistory.attachmentType,
                            attachmentName = messageFromHistory.attachmentName,
                            attachmentSize = null,
                            operatorId = messageFromHistory.operatorId,
                            operatorName = if (messageFromHistory.operatorName == null || !messageFromHistory.isReply) "Вы" else messageFromHistory.operatorName,
                            operatorPreview = null,
                            height = 0,
                            width = 0,
                            isRead = false
                        )
                        if (messageCheckObj !in messagesFromDb) {
                            updateDataInDatabase(messageFromHistory)
                        }
                    }
                }
                MessageType.TRANSFER_TO_OPERATOR.valueType -> {
                    val messagesFromDb = dao.getMessageById(visitor.uuid, messageFromHistory.id!!)

                    if (messagesFromDb == null) {
                        updateDataInDatabase(messageFromHistory)
                    }
                }
                MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                    updateDataInDatabase(messageFromHistory)
                }
            }

        }
    }

}