package com.crafttalk.chat.ui.chat_view.view_model

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.crafttalk.chat.Events
import com.crafttalk.chat.data.Repository
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.database.ChatDatabase
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.data.model.Visitor
import com.crafttalk.chat.data.remote.socket_service.SocketAPI
import com.crafttalk.chat.utils.ConstantsUtils
import kotlinx.coroutines.*


class ChatViewModel(application: Application, visitor: Visitor?) : AndroidViewModel(application) {

    private var dao: MessagesDao? = null
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    init {
        Log.d("ChatViewModel", "const 1")
        SocketAPI.setSharedPreferences(getSharedPreferences(application))
        dao = ChatDatabase.getInstance(application).messageDao().apply {
            Repository.setDao(this)
        }
        //        возможно сделать асинхронно тобы не блочить поток
        if (visitor == null) {
            Repository.getVisitor(getSharedPreferences(application)).apply {
                Log.d(ConstantsUtils.TAG_SOCKET, "ViewModel init")
                SocketAPI.setVisitor(this)
            }
        }
        else {
            SocketAPI.setVisitor(visitor)
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


    override fun onCleared() {
        super.onCleared()
        // а удалться ли соединение? и не выйдет ли это потом боком при возврате???????
        SocketAPI.destroy()
        dao = null
    }

    // возможно сделать асинхронно тобы не блочить поток
    fun registration(vararg args: String) {
        Log.d(ConstantsUtils.TAG_SOCKET, "ViewModel registration")
        SocketAPI.setVisitor(
            Repository.buildVisitor(args)
        )
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("data_visitor", MODE_PRIVATE)
    }

    fun sendMessage(message: String) {
        SocketAPI.sendMessage(message)
    }

    fun syncData() {
        viewModelScope.launch {
            Log.d(ConstantsUtils.TAG_SOCKET, "ViewModel sync")
            Repository.syncData()
        }
    }

}