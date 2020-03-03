package com.crafttalk.chat.data.remote.socket_service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crafttalk.chat.Events
import com.github.nkzawa.socketio.client.Manager
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import com.crafttalk.chat.data.Repository
import com.crafttalk.chat.data.model.MessageType
import com.crafttalk.chat.data.model.Visitor
import com.crafttalk.chat.data.remote.Message
import com.crafttalk.chat.utils.NetworkUtils
import com.matchesgame.crafttalkchatsdk.utils.ConstantsUtils
import com.matchesgame.crafttalkchatsdk.utils.ConstantsUtils.TAG_SOCKET
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException

object SocketAPI {

    private val gson: Gson by lazy {
        Gson()
    }

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private val events: MutableLiveData<Events> by lazy {
        MutableLiveData<Events>()
    }

    fun getEventsFromSocket(): LiveData<Events> = events

    private val stateAuth: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getUserAuthorized(): LiveData<Boolean> = stateAuth

    private lateinit var visitor: Visitor

//    private val socketAPIScope: CoroutineScope by lazy {
//        CoroutineScope(Dispatchers.IO)// + viewModelJob)
//    }

    private val socket: Socket? by lazy {
        try {
            val  manager = Manager(URI(ConstantsUtils.SOCKET_URL));
            manager.socket(ConstantsUtils.NAMESPACE).apply {
                setAllListeners(this)
            }
        } catch (e: URISyntaxException) {
            Log.e(TAG_SOCKET, "fail init socket")
            events.value = Events.NO_INTERNET
            null
        }
    }

    fun setVisitor(visitor: Visitor?) {
        Log.d("LOGGER", "setVisitor visitor - ${visitor?.getJsonObject()}")
        if (visitor == null) {
            Log.d("LOGGER", "set new state stateAuth - ${false}")
            stateAuth.value = false
        }
        else {
            SocketAPI.visitor = visitor
            Log.d("LOGGER", "setVisitor - visitor = ${visitor.getJsonObject()}")
            // инициализация сокета не должна происходить если сравниваем с null
//            if (!socket!!.connected()) {
            if (socket == null || !socket!!.connected()) {
                Log.d("LOGGER", "setVisitor - socket connect}")
                socket!!.connect()
            }else {
                Log.d("LOGGER", "setVisitor - socket auth}")
                // или реконнект да хрен его знает
                authenticationUser(socket!!)
            }
        }
    }

    private fun setAllListeners(socket: Socket) {
        socket.on("connect") {
            Log.d(TAG_SOCKET, "connecting: ${socket.connected()}")
            authenticationUser(socket)
        }

        socket.on("reconnect") {
            Log.d(TAG_SOCKET, "reconnect")
        }

        socket.on("hide") {
            Log.d(TAG_SOCKET, "hide")
            stateAuth.postValue( false)
        }

        socket.on("authorized") {
            Log.d(TAG_SOCKET, "authorized")
            stateAuth.postValue( true)
        }

//        socket.on("authorization-required") {
//            Log.d(TAG_SOCKET, "authorization-required")
//            //
//        }

        socket.on("message") {
            Log.d(TAG_SOCKET, "message, size = ${it.size}; it = ${it}")
            val messageJson = it[0] as JSONObject
            Log.d("SOCKET_API", "json message___ methon message - ${messageJson}")
            val messageObj = gson.fromJson(messageJson.toString(), Message::class.java)

            when (messageObj.message_type) {
                MessageType.VISITOR_MESSAGE.valueType -> Repository.addNewDataAboutMessagesFromTheServer(messageObj)
                MessageType.RECIEVED_BY_OPERATOR.valueType -> {}
            }
            events.postValue(Events.MESSAGE_GET)
        }

        socket.on("history-messages-loaded") {
            Log.d(TAG_SOCKET, "history-messages-loaded")
            //
        }


        socket.on(Socket.EVENT_CONNECT_ERROR) {
            Log.d("EVENT", "EVENT_CONNECT_ERROR")
        }
        socket.on(Socket.EVENT_CONNECT_TIMEOUT) {
            Log.d("EVENT", "EVENT_CONNECT_TIMEOUT")
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("EVENT", "EVENT_DISCONNECT")
        }
        socket.on(Socket.EVENT_ERROR) {
            Log.d("EVENT", "EVENT_ERROR")
        }
        socket.on(Socket.EVENT_MESSAGE) {
            Log.d("EVENT", "EVENT_MESSAGE")
        }
        socket.on(Socket.EVENT_RECONNECT) {
            Log.d("EVENT", "EVENT_RECONNECT")
        }
        socket.on(Socket.EVENT_RECONNECTING) {
            Log.d("EVENT", "EVENT_RECONNECTING")
        }
        socket.on(Socket.EVENT_RECONNECT_ATTEMPT) {
            Log.d("EVENT", "EVENT_RECONNECT_ATTEMPT")
        }
        socket.on(Socket.EVENT_RECONNECT_ERROR) {
            Log.d("EVENT", "EVENT_RECONNECT_ERROR")
        }
        socket.on(Socket.EVENT_RECONNECT_FAILED) {
            Log.d("EVENT", "EVENT_RECONNECT_FAILED")
        }


    }


    fun destroy() {
        socket?.disconnect()
        socket?.off()
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            if (NetworkUtils.isOnline()) {
                socket!!.emit("visitor-message", message, MessageType.VISITOR_MESSAGE.valueType, null, 0, null, null, null)
                Repository.addNewMessageFromTheUser(message)
                events.postValue(Events.MESSAGE_SEND)
            } else {
                events.postValue(Events.MESSAGE_SEND_ERROR)
            }
        }
    }

    fun selectAction(actionId: String) {
        viewModelScope.launch {
            if (NetworkUtils.isOnline()) {
                socket!!.emit("visitor-action", actionId)
                events.postValue(Events.ACTION_SELECT)
            } else {
                events.postValue(Events.ACTION_SELECT_ERROR)
            }
        }

    }

    private fun authenticationUser(socket: Socket) {
        visitor.let {
            socket.emit("me", it.getJsonObject())
        }
    }



//    private fun authenticationUser(socket: Socket) {
//        socket.emit("me",
////            Visitor(
////                "0bbd6047-d26f-4777-b516-be71ba41dafb",
////                "test_fname",
////                "test_lnam​",
////                "test@gmail.com",
////                "89534566787",
////                "test_contact",
////                "28.05.1975",
////                "dc5b56a08dd9ba990d77416f806fc9a10ca7d66c681354beef20c58f0c883bc4"
////            )
////            , true)
//
//        visitor?.let {
//            Log.d("authenticationUser", "visitorJson = ${visitor}")
//            socket.emit("me", it.getJsonObjectVisitor())
////            socket.emit("register", it.getJsonObjectVisitor())
//        }
//
//    }

}

