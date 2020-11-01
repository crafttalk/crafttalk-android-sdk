package com.crafttalk.chat.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crafttalk.chat.R
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection
import com.crafttalk.chat.domain.interactors.*
import com.crafttalk.chat.initialization.ChatInternetConnectionListener
import com.crafttalk.chat.presentation.base.BaseViewModel
import com.crafttalk.chat.presentation.feature.view_picture.ShowImageDialog
import com.crafttalk.chat.utils.ConstantsUtils
import javax.inject.Inject

class ChatViewModel
@Inject constructor(
    private val visitor: Visitor?,
    private val view: ChatView,
    private val socketApi: SocketApi,
    private val authInteractor: AuthInteractor,
    private val chatMessageInteractor: ChatMessageInteractor,
    private val notificationInteractor: NotificationInteractor,
    private val fileInteractor: FileInteractor,
    private val customizingChatBehaviorInteractor: CustomizingChatBehaviorInteractor
) : BaseViewModel() {

    val messages: LiveData<List<Message>> by lazy {
        chatMessageInteractor.getAllMessages()
    }
    val internetConnection: MutableLiveData<TypeInternetConnection> = MutableLiveData()

    private val internetConnectionListener = object : ChatInternetConnectionListener {
        override fun connect() { internetConnection.postValue(TypeInternetConnection.HAS_INTERNET) }
        override fun failConnect() { internetConnection.postValue(TypeInternetConnection.NO_INTERNET) }
        override fun disconnect() { internetConnection.postValue(TypeInternetConnection.SOCKET_DESTROY) }
        override fun lossConnection() { internetConnection.postValue(TypeInternetConnection.NO_INTERNET) }
        override fun reconnect() { internetConnection.postValue(TypeInternetConnection.RECONNECT) }
    }

    init {
        customizingChatBehaviorInteractor.setInternetConnectionListener(internetConnectionListener)
        if (visitor != null) {
            // продумать логику проверки нового юзера со старым, чтобы показать пользователь чат\ даже если нет инета и не получается пройти аутентификацию
            view.showChat()
            authInteractor.logIn(
                visitor,
                {
                    // auth success;
                    view.showChat()
                    syncData()
                    launchIO {
                        Log.d("TEST_NOTIFICATION", "subscribeNotification VM")
                        notificationInteractor.subscribeNotification(visitor.uuid)
                    }
                },
                {
                    // auth fail; maybe clear data from db; user ban
                    view.showLogInForm()
                    handleError(it)
                }
            )
        } else {
            // switch ui to FormAuth
            view.showLogInForm()
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketApi.destroy()
    }

    fun registration(vararg args: String) {
        launchUI {
            Log.d(ConstantsUtils.TAG_SOCKET, "ViewModel registration")
            authInteractor.logIn(
                Visitor.map(args),
                {
                    // auth success; (save visitor into pref in VisitorRepository)
                    view.showChat()
                    syncData()
                },
                {
                    // auth fail; (delete visitor from pref in VisitorRepository); maybe clear data from db; user ban
                    view.showLogInForm()
                    handleError(it)
                }
            )
        }
    }

    fun openFile(context: Context, fileUrl: String) {
        val intentView = Intent(Intent.ACTION_VIEW).apply {
            data = fileUrl.toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val intentChooser = Intent.createChooser(intentView, context.getString(R.string.string_chooser_open_file_action_view))
        if (intentView.resolveActivity(context.packageManager) != null) {
            context.startActivity(intentChooser)
        }
    }

    fun openImage(activity: Activity, imageUrl: String, width: Int, height: Int) {
        ShowImageDialog.Builder(activity)
            .setUrl(imageUrl)
            .setType(TypeFile.IMAGE)
            .setSize(width, height)
            .show()
    }

    fun openGif(activity: Activity, gifUrl: String, width: Int, height: Int) {
        ShowImageDialog.Builder(activity)
            .setUrl(gifUrl)
            .setType(TypeFile.GIF)
            .setSize(width, height)
            .show()
    }

    fun selectAction(actionId: String) {
        launchIO {
            chatMessageInteractor.selectActionInMessage(
                actionId,
                {},
                {}
            )
        }
    }

    fun updateData(idKey: Long, height: Int, width: Int) {
        launchIO {
            Log.d("DEBUGGER", "update start")
            chatMessageInteractor.updateSizeMessage(
                idKey,
                height,
                width,
                {},
                {}
            )
            Log.d("DEBUGGER", "update end")
        }
    }

    fun sendMessage(message: String) {
        launchIO {
            chatMessageInteractor.sendMessage(
                message,
                {},
                {
                    handleError(it)
                }
            )
        }
    }

    fun sendFile(file: File) {
        launchIO {
            fileInteractor.uploadFile(file, {}, {})
        }
    }

    fun sendFiles(fileList: List<File>) {
        launchIO {
            fileInteractor.uploadFiles(fileList, {}, {})
        }
    }

    fun sendImage(bitmap: Bitmap) {
        launchIO {
            fileInteractor.uploadImage(bitmap, {}, {})
        }
    }

    // мб синхронизировать при включении экрана: пользователь выключил экран\ зашел\ обновил гдето\ и открыл прилку
    private fun syncData(timestamp: Long = 0L) {
        launchIO {
            Log.d(ConstantsUtils.TAG_SOCKET, "ViewModel sync")
            chatMessageInteractor.syncMessages(
                timestamp,//dao.getLastTime()
                {},
                {
                    handleError(it)
                }
            )
        }
    }

}