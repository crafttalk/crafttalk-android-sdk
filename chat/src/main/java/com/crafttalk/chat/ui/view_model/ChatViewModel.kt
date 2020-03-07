package com.crafttalk.chat.ui.view_model

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.crafttalk.chat.Events
import com.crafttalk.chat.data.Repository
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.database.ChatDatabase
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.data.remote.socket_service.SocketAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private var dao: MessagesDao? = null
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    init {
        SocketAPI.setSharedPreferences(getSharedPreferences(application))
        dao = ChatDatabase.getInstance(application).messageDao().apply {
            Repository.setDao(this)
//            возможно сделать асинхронно тобы не блочить поток
            Repository.getVisitor(getSharedPreferences(application)).apply {
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


    override fun onCleared() {
        super.onCleared()
        // а удалться ли соединение? и не выйдет ли это потом боком при возврате???????
        SocketAPI.destroy()
        dao = null
    }

    // возможно сделать асинхронно тобы не блочить поток
    fun registration(vararg args: String) {
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
            Repository.syncData()
        }
    }

}