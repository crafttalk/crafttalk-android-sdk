package com.crafttalk.chat.data.api.socket


import android.util.Log
import com.crafttalk.chat.data.repository.DataRepository
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection
import com.crafttalk.chat.domain.entity.message.Message
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.ConstantsUtils
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
    private val dataRepository: DataRepository,
    private val gson: Gson
) {

    private lateinit var visitor: Visitor
    private lateinit var successAuthFun: () -> Unit
    private lateinit var failAuthFun: (ex: Throwable) -> Unit
    private lateinit var changeInternetConnectionStateFun: (TypeInternetConnection) -> Unit
    private var socket: Socket? = null

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private fun initSocket() {
        socket = try {
            val manager = Manager(URI(ConstantsUtils.SOCKET_URL))
            manager.socket(ConstantsUtils.NAMESPACE)
        } catch (e: URISyntaxException) {
            Log.e(TAG_SOCKET, "fail init socket")
            changeInternetConnectionStateFun(TypeInternetConnection.NO_INTERNET)
            null
        }
    }

    fun setInternetConnectionListener(function: (TypeInternetConnection) -> Unit) {
        this.changeInternetConnectionStateFun = function
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
            changeInternetConnectionStateFun(TypeInternetConnection.RECONNECT)
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
            viewModelScope.launch {
                Log.d(TAG_SOCKET, "message, size = ${it.size}; it = $it")
                val messageJson = it[0] as JSONObject
                Log.d("SOCKET_API", "json message___ methon message - $messageJson")
                val messageSocket = gson.fromJson(messageJson.toString(), Message::class.java)
                if (!messageJson.toString().contains(""""message":"\/start"""")) {
                    dataRepository.insert(messageSocket)
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
                    dataRepository.marge(listMessages)
                }
            }
        }

        socket.on(Socket.EVENT_CONNECT) {
            changeInternetConnectionStateFun(TypeInternetConnection.HAS_INTERNET)
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) {
            changeInternetConnectionStateFun(TypeInternetConnection.NO_INTERNET)
            Log.d(TAG_SOCKET, "EVENT_CONNECT_ERROR")
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            changeInternetConnectionStateFun(TypeInternetConnection.NO_INTERNET)
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
        changeInternetConnectionStateFun(TypeInternetConnection.SOCKET_DESTROY)
        socket?.disconnect()
        socket?.off()
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
        socket.emit("me", visitor.getJsonObject(), (ChatAttr.mapAttr["auth_with_hash"] as Boolean))
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

}