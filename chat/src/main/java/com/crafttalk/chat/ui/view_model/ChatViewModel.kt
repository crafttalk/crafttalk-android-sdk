package com.crafttalk.chat.ui.view_model

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.*
import com.crafttalk.chat.Events
import com.crafttalk.chat.data.Repository
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.database.ChatDatabase
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.data.remote.socket_service.SocketAPI


class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private var dao: MessagesDao? = null
//    private val viewModelJob = Job()
//    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    init {
        dao = ChatDatabase.getInstance(application).messageDao().apply {
            Repository.setDao(this)
//            возможно сделать асинхронно тобы не блочить поток
            Repository.getVisitor(application.getSharedPreferences("data_visitor", MODE_PRIVATE)).apply {
                SocketAPI.setVisitor(this)
            }
        }
    }

    val messages: LiveData<List<Message>> by lazy {
        Repository.getMessagesList()
    }

    // сливаем все эвенты со всех сервисов в одну LiveData
    val allEvents: MediatorLiveData<Events> by lazy {
        val mediatorEvents = MediatorLiveData<Events>()
        mediatorEvents.addSource(SocketAPI.getEventsFromSocket(), Observer {
            mediatorEvents.value = it
        })
        mediatorEvents
    }

    val stateUserIsAuth: LiveData<Boolean> by lazy {
        SocketAPI.getUserAuthorized()
    }

    override fun onCleared() {
        super.onCleared()
        // а удалться ли соединение? и не выйдет ли это потом боком при возврате???????
        SocketAPI.destroy()
        dao = null
    }

    // возможно сделать асинхронно тобы не блочить поток
    fun registration(context: Context, vararg args: String) {
        val pref = context.getSharedPreferences("data_visitor", MODE_PRIVATE)
        SocketAPI.setVisitor(
            Repository.buildVisitor(pref, args)
        )
    }

    fun sendMessage(message: String) {
        SocketAPI.sendMessage(message)
    }

}