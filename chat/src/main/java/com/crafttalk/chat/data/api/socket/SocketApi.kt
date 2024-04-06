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
import com.crafttalk.chat.presentation.helper.ui.getWeightMediaFile
import com.crafttalk.chat.utils.*
import com.crafttalk.chat.utils.ConstantsUtils.TAG_DATABASE_INSERT
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET_API_SETTING
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET_EVENT
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import kotlinx.android.synthetic.main.com_crafttalk_chat_layout_chat.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class SocketApi(
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
    private var updateCurrentReadMessageTime: (newReadPoints: List<Pair<String, Long>>) -> Unit = {}
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

    val executorService = Executors.newSingleThreadScheduledExecutor()

    /**
     * Получает с сервера json файл с настройками канала, парсит полученый файл и применяет полученные настройки
     */
    private val getSettingsFromServerTask = Runnable {
        try {
            val apiResponse:String = URL("${ChatParams.urlChatScheme}://${ChatParams.urlChatHost}/configuration/${ChatParams.urlChatNameSpace}").readText()
            //var i = JSONObject(apiResponse)

            //var testModel = gson.fromJson(i.toString(),SettingFromServerJSON::class.java)
            // var testModel = gson.fromJson(,SettingFromServerJSON::class.java)
            //settingJSON = SettingFromServerJSON()

            //settingJSON = gson.fromJson(apiResponse, SettingFromServerJSON::class.java)
            //val settingFromServerJSON = Gson().fromJson(apiResponse, configuration::class.java)

            /**
             *  устанваливает интревал отправки печатемого пользователем сообщения
             */
            if (apiResponse.contains("\"sendUserIsTyping\":true")){
                val str = apiResponse
                // Находим индекс начала числа
                val startIndex = str.indexOf("\"userTypingInterval\":") + "\"userTypingInterval\":".length
                // Находим индекс конца числа
                val endIndex = str.indexOf(',',startIndex)
                // Извлекаем строку с числом
                val numberStr = str.substring(startIndex, endIndex)
                // Преобразуем строку в число
                val number = numberStr.toInt()
                // Выводим число
                chatEventListener?.setUserTypingInterval(number)
            }
            else {
                chatEventListener?.setUserTyping(false)
            }

            /**
             * Проверяет доступен ли чат для общения
             */
            if (apiResponse.contains("\"block\":false")){
                Log.d("TAG_SOCKET_API_SETTING","get Server setting, Chat not closed")
            }
            else {
                val str = apiResponse

                val startIndex = str.indexOf("\"blockMessage\":\"") + "\"blockMessage\":\"".length

                val endIndex = str.indexOf("\",\"",startIndex)

                val blockMessageStr = str.substring(startIndex, endIndex)

                Log.d("TAG_SOCKET_API_SETTING","get Server setting, Chat closed")
                chatEventListener?.setChatStateClosed(true,  blockMessageStr)
            }
        }
        catch (e:Exception){
            Log.e(TAG_SOCKET_API_SETTING,e.message.toString())
        }
    }

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
                Log.e(TAG_SOCKET,"Can't create socket. Incorrect URI, maybe: " + "${ChatParams.urlChatScheme}://${ChatParams.urlChatHost}")
                null
            }
        }
    }

    fun destroySocket() {
        isSendGreet = false
        socket?.off()
        socket = null
        Log.i(TAG_SOCKET_EVENT, "Socket destroyed")
    }

    fun dropChat() {
        socket?.disconnect()
        Log.i(TAG_SOCKET_EVENT, "Socket disconnect")
    }

    fun setInternetConnectionListener(listener: ChatInternetConnectionListener) {
        this.chatInternetConnectionListener = listener
        Log.d(TAG_SOCKET_EVENT,"setInternetConnectionListener")
    }

    fun setMessageListener(listener: ChatMessageListener) {
        this.chatMessageListener = listener
        Log.d(TAG_SOCKET_EVENT,"setMessageListener")
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
        updateCurrentReadMessageTime: (newTimeMarks: List<Pair<String, Long>>) -> Unit,
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
        executorService.schedule(getSettingsFromServerTask,0,TimeUnit.MILLISECONDS)
    }

    private fun setAllListeners(socket: Socket) {

        socket.on("connect") {
            Log.d(TAG_SOCKET_EVENT, "connect connecting event- ${socket.connected()}")
            authenticationUser(socket)
        }

        socket.on("reconnect") {
            Log.d(TAG_SOCKET_EVENT, "reconnect event")
            chatInternetConnectionListener?.reconnect()
        }

        socket.on("hide") {
            Log.d(TAG_SOCKET_EVENT, "hide event")
            isAuthorized = false
            isSynchronized = false
            failAuthUiFun()
            viewModelScope.launch {
                failAuthUxFun()
            }
        }

        socket.on("authorized") {
            Log.d(TAG_SOCKET_EVENT, "authorized event")
            isAuthorized = true
            successAuthUiFun()
            viewModelScope.launch {
                successAuthUxFun()
            }
            syncChat {
                if (ChatParams.showInitialMessage == false) {
                    messageDao.deleteAllMessageByType(MessageType.INITIAL_MESSAGE.valueType)
                    if (
                        (ChatParams.sendInitialMessageOnStartDialog == true) ||
                        (ChatParams.sendInitialMessageOnOpen == true && chatStatus == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP)
                    ) {
                        greet()
                    }
                }
//                ChatParams.startMessage?.let {
//                    sendMessage(it, null)
//                    ChatParams.startMessage = null
//                }
            }
        }

        socket.on("authorization-required") {
            Log.d(TAG_SOCKET_EVENT, "authorization-required event")
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
                Log.d(TAG_SOCKET, "message, size event= ${it.size}; it = $it")
                val messageJson = it[0] as JSONObject

                val currentTimestamp = System.currentTimeMillis()
                Log.d(TAG_SOCKET_EVENT, "json message___ methon message - $messageJson")

                val gson = GsonBuilder()
                    .registerTypeAdapter(NetworkWidget::class.java, NetworkWidgetDeserializer())
                    .create()
                var messageSocket = NetworkMessage(UUID.randomUUID().toString(),null,-1,false,null,0)
                try {
                    //messageSocket = gson.fromJson(messageJson.toString().replace("&amp;", "&"), NetworkMessage::class.java) ?: return@launch //philip, понятия не имею для чего это было сделано
                    messageSocket = gson.fromJson(messageJson.toString(), NetworkMessage::class.java) ?: return@launch
                }
                catch (e: Exception){
                    Log.e(TAG_SOCKET, "An error occurred while getting message from server. Info: " + e.message)
                }
                when (messageSocket.messageType) {
                    MessageType.UPDATE_DIALOG_SCORE.valueType -> chatEventListener?.updateDialogScore()
                    MessageType.OPERATOR_IS_TYPING.valueType -> chatEventListener?.operatorStartWriteMessage()
                    MessageType.OPERATOR_STOPPED_TYPING.valueType -> chatEventListener?.operatorStopWriteMessage()
                    MessageType.MESSAGE.valueType -> chatEventListener?.operatorStopWriteMessage()
                    MessageType.INITIAL_MESSAGE.valueType -> chatEventListener?.operatorStopWriteMessage()
                    MessageType.FINISH_DIALOG.valueType -> chatEventListener?.finishDialog(messageSocket.dialogId)
                    MessageType.USER_WAS_MERGED.valueType -> chatEventListener?.showUploadHistoryBtn()
                }
                if (
                    (!messageSocket.toString().contains(""""message":"\/start"""") && !messageSocket.toString().contains(""""message":"/start"""") && !messageSocket.toString().contains("/start") && !messageSocket.toString().contains("""\/start""")) &&
                    (messageSocket.id != null || !messageDao.isNotEmpty())
                ) {
                    when {
                        (chatStatus == ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP) && (messageSocket.messageType in listOf(
                            MessageType.MESSAGE.valueType,
                            MessageType.INITIAL_MESSAGE.valueType,
                            MessageType.CONNECTED_OPERATOR.valueType
                        )) -> {
                            countNewMessages++
                            chatMessageListener?.getNewMessages(countNewMessages)
                        }
                    }
                    if (messageSocket.id == null) {
                        messageSocket.id = System.currentTimeMillis().toString()
                    }

                    try {
                        Log.d(TAG_DATABASE_INSERT, "Insert in database message: " + messageSocket.id.toString() + messageSocket.message.toString())
                        updateDataInDatabase(messageSocket, currentTimestamp)
                    }
                    catch (e: Exception){
                        Log.e(TAG_DATABASE_INSERT,"An error occurred while adding to the database. Info: " + e.message)
                    }

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
            syncChat {
                if (ChatParams.showInitialMessage == false) {
                    messageDao.deleteAllMessageByType(MessageType.INITIAL_MESSAGE.valueType)
                    if (
                        ChatParams.sendInitialMessageOnOpen == true &&
                        chatStatus == ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP
                    ) {
                        greet()
                    }
                }
//                ChatParams.startMessage?.let {
//                    sendMessage(it, null)
//                    ChatParams.startMessage = null
//                }
            }
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
            socket?.emit("visitor-message", "/start", MessageType.MESSAGE.valueType, null, 0, null, null, null)
        }
    }

    /**
     * Функция отправляет на сервер оператору текст который в данный момент печтатет пользователь
     *
     * @param message текст который в данные момент находится у пользователя в поле для ввода
     */
    fun sendServiceMessageUserIsTypingText(message: String){
        socket?.emit("visitor-message", message, MessageType.USER_MESSAGE_SENT.valueType, null, 0, null, null)
    }

    fun sendServiceMessageUserStopTypingText(){
        socket?.emit("visitor-message", "", MessageType.USER_STOPPED_TYPING.valueType, null, 0, null, null)
    }


    fun sendMessage(message: String, repliedMessage: NetworkMessage?) {
        if (ChatAttr.getInstance().replyEnable) {
            val repliedMessageJSONObject = repliedMessage?.let {
                JSONObject(gson.toJson(it))
            }
            socket?.emit(
                "visitor-message",
                message,
                MessageType.MESSAGE.valueType,
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
                MessageType.MESSAGE.valueType,
                null,
                0,
                null,
                null,
                null
            )
        }
    }

    fun readMessage(messageId: String) {
        socket?.emit(
            "visitor-message",
            "",
            MessageType.READING_CONFIRMATION.valueType,
            messageId,
            0,
            null,
            null,
            null,
            null
        )
    }

    fun selectAction(actionId: String) {
        socket?.emit("visitor-action", actionId)
    }

    /**Отправляет системное сообщение с оценкой диалога
     * Вызывается когда пользоватль нажимает на звездочки
     *
     *      countStars: Int -- оценка диалога от 1 до 5.
     *      finishReason:String -- причина закрытия, по умолчанию null,
     *      Если клиент закрывает чат не оценивая диалог то нужно отправить "CLOSED_BY_CLIENT".
     *      dialogID:String -- ID диалога который нужно оценить, если null то оценивается самый последний диалог
     */
    fun giveFeedbackOnOperator(countStars: Int?, finishReason:String?, dialogID:String?) {
        Log.i("TAG_SOCKET_EVENT", dialogID ?: "Ошибка: Шальное null исключение")
        socket?.emit("visitor-message", "", MessageType.UPDATE_DIALOG_SCORE.valueType, null, countStars, finishReason, null, null, null, dialogID, "finish_dialog")
    }

    fun mergeNewMessages() {
        isSynchronized = true
        val maxUserTimestamp = bufferNewMessages.filter { !it.isReply }.maxByOrNull { it.timestamp }?.timestamp
        if (maxUserTimestamp != null) {
            val messagesForUpdateReadPoint = bufferNewMessages.filter { it.timestamp <= maxUserTimestamp }
                .map { Pair(it.id, it.timestamp) }
            updateCurrentReadMessageTime(messagesForUpdateReadPoint)
        }
        updateCountUnreadMessages(bufferNewMessages.filter { it.timestamp > (maxUserTimestamp ?: 0) }.size, maxUserTimestamp != null)
        bufferNewMessages.distinctBy { it.id }.filter { !messageDao.hasThisMessage(it.id) }.let { messages ->
            viewModelScope.launch {
                updateSearchMessagePosition(messages)
            }
            messageDao.insertMessages(messages)
        }
        bufferNewMessages.clear()
        chatEventListener?.synchronized()
    }

    suspend fun uploadMessages(
        timestamp: Long
    ): List<NetworkMessage>? {
        val channel = Channel<List<NetworkMessage>?>()

        socket?.on("history-messages-loaded") {
            viewModelScope.launch {
                val listMessages = gson.fromJson(
                    it[0].toString().replace("&amp;", "&"),
                    Array<NetworkMessage>::class.java
                ) ?: arrayOf()
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

    private fun syncChat(actionAfter: () -> Unit) {
        viewModelScope.launch {
            syncMessages()
            actionAfter()
        }
    }

    private suspend fun updateDataInDatabase(messageSocket: NetworkMessage, currentTimestamp: Long) {
        val operatorPreview = messageSocket.operatorId?.let { getPersonPreview(it) }
        when {
            (MessageType.MESSAGE.valueType == messageSocket.messageType) && (messageSocket.isImage || messageSocket.isGif) -> {
                messageSocket.correctAttachmentUrl?.let { url ->
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
            (MessageType.MESSAGE.valueType == messageSocket.messageType) && (messageSocket.isFile || messageSocket.isUnknownType) -> {
                messageSocket.correctAttachmentUrl?.let { url ->
                    insertMessage(MessageEntity.map(
                        uuid = visitor.uuid,
                        networkMessage = messageSocket,
                        arrivalTime = currentTimestamp,
                        operatorPreview = operatorPreview,
                        fileSize = getWeightFile(url) ?: getWeightMediaFile(context, url)
                    ))
                }
            }
            (messageSocket.messageType in listOf(MessageType.MESSAGE.valueType, MessageType.INITIAL_MESSAGE.valueType)) && messageSocket.isText -> {
                val repliedMessageUrl = messageSocket.replyToMessage?.correctAttachmentUrl
                when {
                    repliedMessageUrl != null && messageSocket.replyToMessage.isFile -> {
                        insertMessage(MessageEntity.map(
                            uuid = visitor.uuid,
                            networkMessage = messageSocket,
                            arrivalTime = currentTimestamp,
                            operatorPreview = operatorPreview,
                            repliedMessageFileSize = getWeightFile(repliedMessageUrl) ?: getWeightMediaFile(context, repliedMessageUrl)
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
            (MessageType.RECEIVED_BY_MEDIATOR.valueType == messageSocket.messageType) || (MessageType.RECEIVED_BY_OPERATOR.valueType == messageSocket.messageType) -> {
                messageSocket.parentMessageId?.let { parentId ->
                    messageDao.updateMessage(parentId, messageSocket.messageType)
                }
            }
            (MessageType.CONNECTED_OPERATOR.valueType == messageSocket.messageType) -> {
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
//                Log.d("TEST_DATA_LOP_S", "insertMessage")
                updateCountUnreadMessages(0, true)
            } else {
                updateCountUnreadMessages(1, false)
            }
            if (!messageDao.hasThisMessage(message.id)) {
                viewModelScope.launch {
                    updateSearchMessagePosition(listOf(message))
                }
                messageDao.insertMessage(message)
            }
        } else {
            bufferNewMessages.add(message)
        }
    }

}