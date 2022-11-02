package com.crafttalk.chat.data.api.socket

import android.content.Context
import android.util.Log
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.message.NetworkMessage
import com.crafttalk.chat.domain.entity.message.NetworkWidget
import com.crafttalk.chat.domain.entity.message.NetworkWidgetDeserializer
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatEventListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.presentation.helper.ui.getSizeMediaFile
import com.crafttalk.chat.presentation.helper.ui.getWeightFile
import com.crafttalk.chat.utils.*
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET_EVENT
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import okhttp3.OkHttpClient

class SocketApi constructor(
    private val okHttpClient: OkHttpClient,
    private val messageDao: MessagesDao,
    private val gson: Gson,
    private val context: Context
) {

    private var socket: Socket? = null
    private lateinit var visitor: Visitor
    private var successAuthUiFun: () -> Unit = {}
    private var failAuthUiFun: () -> Unit = {}
    private var successAuthUxFun: suspend () -> Unit = {}
    private var failAuthUxFun: suspend () -> Unit = {}
    private var syncMessages: suspend () -> Unit = {}
    private var updateCurrentReadMessageTime: (newReadPoint: Long) -> Unit = {}
    private var updateCountUnreadMessages: (countNewMessages: Int, hasUserMessage: Boolean) -> Unit = { _,_ -> }
    private var updateSearchMessagePosition: suspend (insertedMessages: List<MessageEntity>) -> Unit = {}
    private var getPersonPreview: suspend (personId: String) -> String? = { null }
    private var updatePersonName: suspend (personId: String?, currentPersonName: String?) -> Unit = { _,_ -> }
    private var isAuthorized: Boolean = false
    private var isSynchronized: Boolean = false
    private val bufferNewMessages = mutableListOf<MessageEntity>()

    private var chatInternetConnectionListener: ChatInternetConnectionListener? = null
    private var chatMessageListener: ChatMessageListener? = null
    private var chatEventListener: ChatEventListener? = null

    var chatStatus = ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP
    private var countNewMessages = 0

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    private var isSendGreet = false

    fun initSocket() {
        if (socket == null) {
            socket = try {
                val opt: IO.Options = IO.Options().apply {
                    callFactory = okHttpClient
                    webSocketFactory = okHttpClient
                }
                val manager = Manager(
                    URI("${ChatParams.urlChatScheme}://${ChatParams.urlChatHost}"),
                    opt
                )
                manager.socket("/${ChatParams.urlChatNameSpace}", opt).apply {
                    setAllListeners(this)
                }
            } catch (e: URISyntaxException) {
                failAuthUiFun()
                viewModelScope.launch {
                    failAuthUxFun()
                }
                isAuthorized = false
                isSynchronized = false
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

    fun setUpdateSearchMessagePosition(updateSearchMessagePosition: suspend (insertedMessages: List<MessageEntity>) -> Unit) {
        this.updateSearchMessagePosition = updateSearchMessagePosition
    }

    fun resetNewMessagesCounter() {
        countNewMessages = 0
    }

    fun setVisitor(
        visitor: Visitor,
        successAuthUi: () -> Unit,
        failAuthUi: () -> Unit,
        successAuthUx: suspend () -> Unit,
        failAuthUx: suspend () -> Unit,
        sync: suspend () -> Unit,
        updateCurrentReadMessageTime: (newTimeMark: Long) -> Unit,
        updateCountUnreadMessages: (countNewMessages: Int, hasUserMessage: Boolean) -> Unit,
        getPersonPreview: suspend (personId: String) -> String?,
        updatePersonName: suspend (personId: String?, currentPersonName: String?) -> Unit,
        chatEventListener: ChatEventListener?
    ) {
        this.successAuthUiFun = successAuthUi
        this.successAuthUxFun = successAuthUx
        this.failAuthUiFun = failAuthUi
        this.failAuthUxFun = failAuthUx
        this.syncMessages = sync
        this.updateCurrentReadMessageTime = updateCurrentReadMessageTime
        this.updateCountUnreadMessages = updateCountUnreadMessages
        this.getPersonPreview = getPersonPreview
        this.updatePersonName = updatePersonName
        chatEventListener?.let { this.chatEventListener = it }
        this.visitor = visitor
        socket?.run(::connectUser)
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
            isSynchronized = false
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
            if ((ChatParams.initialMessageMode == InitialMessageMode.SEND_AFTER_AUTHORIZATION) || (ChatParams.initialMessageMode == InitialMessageMode.SEND_ON_OPEN && chatStatus == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP)) {
                greet()
            }
            syncChat()
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
            viewModelScope.launch {
                Log.d(TAG_SOCKET, "message, size = ${it.size}; it = $it")
                val messageJson = it[0] as JSONObject
                val currentTimestamp = System.currentTimeMillis()
                Log.d(TAG_SOCKET_EVENT, "json message___ methon message - $messageJson")

                val gson = GsonBuilder()
                    .registerTypeAdapter(NetworkWidget::class.java, NetworkWidgetDeserializer())
                    .create()
                val messageSocket = gson.fromJson(messageJson.toString().replace("&amp;", "&"), NetworkMessage::class.java)
                when (messageSocket.messageType) {
                    MessageType.OPERATOR_IS_TYPING.valueType -> chatEventListener?.operatorStartWriteMessage()
                    MessageType.OPERATOR_STOPPED_TYPING.valueType -> chatEventListener?.operatorStopWriteMessage()
                    MessageType.VISITOR_MESSAGE.valueType -> chatEventListener?.operatorStopWriteMessage()
                    MessageType.FINISH_DIALOG.valueType -> chatEventListener?.finishDialog()
                    MessageType.MERGE_HISTORY.valueType -> chatEventListener?.showUploadHistoryBtn()
                }
                if (
                    (!messageJson.toString().contains(""""message":"\/start"""") && !messageJson.toString().contains(""""message":"/start"""")) &&
                    (messageSocket.id != null || !messageDao.isNotEmpty())
                ) {
                    when {
                        (chatStatus == ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP) && (messageSocket.messageType == MessageType.VISITOR_MESSAGE.valueType) -> {
                            countNewMessages++
                            chatMessageListener?.getNewMessages(countNewMessages)
                        }
                    }
                    if (messageSocket.id == null) {
                        messageSocket.id = System.currentTimeMillis().toString()
                    }
                    updateDataInDatabase(messageSocket, currentTimestamp)
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
            isSynchronized = false
            chatInternetConnectionListener?.lossConnection()
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) {
            Log.d(TAG_SOCKET_EVENT, "EVENT_CONNECT_ERROR")
            isAuthorized = false
            isSynchronized = false
            chatInternetConnectionListener?.failConnect()
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
            if (ChatParams.initialMessageMode == InitialMessageMode.SEND_ON_OPEN && chatStatus == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP) {
                greet()
            }
            syncChat()
        } else {
            try {
                socket.emit(
                    "me",
                    visitor.getJsonObject(),
                    ChatParams.authMode == AuthType.AUTH_WITHOUT_FORM
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

    fun sendMessage(message: String, repliedMessage: NetworkMessage?) {
        if (ChatAttr.getInstance().replyEnable) {
            val repliedMessageJSONObject = repliedMessage?.let {
                JSONObject(gson.toJson(it))
            }
            socket?.emit(
                "visitor-message",
                message,
                MessageType.VISITOR_MESSAGE.valueType,
                null,
                0,
                null,
                null,
                repliedMessageJSONObject,
                null
            )
        } else {
            socket?.emit(
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
        socket?.emit("visitor-action", actionId)
    }

    fun giveFeedbackOnOperator(countStars: Int) {
        socket?.emit("visitor-message", "", MessageType.UPDATE_DIALOG_SCORE.valueType, null, countStars, null, null)
    }

    fun mergeNewMessages() {
        isSynchronized = true
        val maxUserTimestamp = bufferNewMessages.filter { !it.isReply }.maxBy { it.timestamp }?.timestamp
        maxUserTimestamp?.run(updateCurrentReadMessageTime)
        updateCountUnreadMessages(bufferNewMessages.filter { it.timestamp > (maxUserTimestamp ?: 0) }.size, maxUserTimestamp != null)
        viewModelScope.launch {
            updateSearchMessagePosition(bufferNewMessages)
        }
        messageDao.insertMessages(bufferNewMessages.distinctBy { it.id }.filter { !messageDao.hasThisMessage(it.id) })
        bufferNewMessages.clear()
        chatEventListener?.synchronized()
    }

    suspend fun uploadMessages(
        timestamp: Long
    ): List<NetworkMessage>? {
        val channel = Channel<List<NetworkMessage>?>()

        socket?.on("history-messages-loaded") {
            viewModelScope.launch {
                val listMessages = gson.fromJson(it[0].toString().replace("&amp;", "&"), Array<NetworkMessage>::class.java)
                channel.send(listMessages.toList())
            }
        }
        socket?.emit("history-messages-requested", timestamp, visitor.token, ChatParams.urlChatHost) ?: channel.send(null)

        return viewModelScope.async {
            channel.receive()
        }.await()
    }

    fun closeHistoryListener() {
        socket?.off("history-messages-loaded")
    }

    private fun syncChat() {
        viewModelScope.launch {
            syncMessages()
        }
    }

    private suspend fun updateDataInDatabase(messageSocket: NetworkMessage, currentTimestamp: Long) {
        val operatorPreview = messageSocket.operatorId?.let { getPersonPreview(it) }
        when {
            (MessageType.VISITOR_MESSAGE.valueType == messageSocket.messageType) && (messageSocket.isImage || messageSocket.isGif) -> {
                messageSocket.attachmentUrl?.let { url ->
                    getSizeMediaFile(context, url) { height, width ->
                        viewModelScope.launch {
                            insertMessage(MessageEntity.map(
                                uuid = visitor.uuid,
                                networkMessage = messageSocket,
                                arrivalTime = currentTimestamp,
                                operatorPreview = operatorPreview,
                                mediaFileHeight = height,
                                mediaFileWidth = width
                            ))
                        }
                    }
                }
            }
            (MessageType.VISITOR_MESSAGE.valueType == messageSocket.messageType) && messageSocket.isFile -> {
                messageSocket.attachmentUrl?.let { url ->
                    insertMessage(MessageEntity.map(
                        uuid = visitor.uuid,
                        networkMessage = messageSocket,
                        arrivalTime = currentTimestamp,
                        operatorPreview = operatorPreview,
                        fileSize = getWeightFile(url)
                    ))
                }
            }
            (MessageType.VISITOR_MESSAGE.valueType == messageSocket.messageType) && messageSocket.isText -> {
                val repliedMessageUrl = messageSocket.replyToMessage?.attachmentUrl
                when {
                    repliedMessageUrl != null && messageSocket.replyToMessage.isFile -> {
                        insertMessage(MessageEntity.map(
                            uuid = visitor.uuid,
                            networkMessage = messageSocket,
                            arrivalTime = currentTimestamp,
                            operatorPreview = operatorPreview,
                            repliedMessageFileSize = repliedMessageUrl.run(::getWeightFile)
                        ))
                    }
                    repliedMessageUrl != null && (messageSocket.replyToMessage.isImage || messageSocket.replyToMessage.isGif) -> {
                        getSizeMediaFile(context, repliedMessageUrl) { height, width ->
                            viewModelScope.launch {
                                insertMessage(MessageEntity.map(
                                    uuid = visitor.uuid,
                                    networkMessage = messageSocket,
                                    arrivalTime = currentTimestamp,
                                    operatorPreview = operatorPreview,
                                    repliedMessageMediaFileHeight = height,
                                    repliedMessageMediaFileWidth = width
                                ))
                            }
                        }
                    }
                    else -> {
                        insertMessage(MessageEntity.map(
                            uuid = visitor.uuid,
                            networkMessage = messageSocket,
                            arrivalTime = currentTimestamp,
                            operatorPreview = operatorPreview
                        ))
                    }
                }
            }
            (MessageType.RECEIVED_BY_MEDIATO.valueType == messageSocket.messageType) || (MessageType.RECEIVED_BY_OPERATOR.valueType == messageSocket.messageType) -> {
                messageSocket.parentMessageId?.let { parentId ->
                    messageDao.updateMessage(parentId, messageSocket.messageType)
                }
            }
            (MessageType.TRANSFER_TO_OPERATOR.valueType == messageSocket.messageType) -> {
                insertMessage(MessageEntity.mapOperatorJoinMessage(
                    uuid = visitor.uuid,
                    networkMessage = messageSocket,
                    arrivalTime = currentTimestamp,
                    operatorPreview = operatorPreview
                ))
            }
        }
        updatePersonName(messageSocket.operatorId, messageSocket.operatorName)
    }

    private fun insertMessage(message: MessageEntity) {
        if (isSynchronized) {
            if (!message.isReply) {
                updateCurrentReadMessageTime(message.timestamp)
                updateCountUnreadMessages(0, true)
            } else {
                updateCountUnreadMessages(1, false)
            }
            viewModelScope.launch {
                updateSearchMessagePosition(listOf(message))
            }
            if (!messageDao.hasThisMessage(message.id)) {
                messageDao.insertMessage(message)
            }
        } else {
            bufferNewMessages.add(message)
        }
    }

}