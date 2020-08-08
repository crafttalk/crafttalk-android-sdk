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
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.data.remote.socket_service.SocketApi
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection
import com.crafttalk.chat.domain.usecase.auth.LogIn
import com.crafttalk.chat.domain.usecase.file.UploadFiles
import com.crafttalk.chat.domain.usecase.internet.SetInternetConnectionListener
import com.crafttalk.chat.domain.usecase.message.*
import com.crafttalk.chat.presentation.adapters.AdapterListMessages
import com.crafttalk.chat.presentation.adapters.AdapterListMessages.UpdateSizeMessageListener
import com.crafttalk.chat.presentation.feature.view_picture.ShowImageDialog
import com.crafttalk.chat.utils.ConstantsUtils

class ChatViewModel constructor(
    private val uploadFiles: UploadFiles,
    private val getMessages: GetMessages,
    private val sendMessages: SendMessages,
    private val syncMessages: SyncMessages,
    private val selectAction: SelectAction,
    private val logIn: LogIn,
    private val setInternetConnectionListener: SetInternetConnectionListener,
    private val visitor: Visitor?,
    private val view: ChatView,
    private val socketApi: SocketApi,
    private val updateSizeMessages: UpdateSizeMessages
) : BaseViewModel() {

    val actionListener = object : AdapterListMessages.ActionListener {
        override fun actionSelect(actionId: String) {
            launchIO {
                selectAction(
                    actionId,
                    {},
                    {}
                )
            }
        }
    }

    val updateSizeMessageListener = object : UpdateSizeMessageListener {
        override fun updateData(idKey: Long, height: Int, width: Int) {
            launchIO {
                Log.d("DEBUGGER", "update start")
                updateSizeMessages(
                    idKey,
                    height,
                    width,
                    {},
                    {}
                )
                Log.d("DEBUGGER", "update end")
            }
        }

    }

    val messages: LiveData<List<Message>> by lazy {
        getMessages()
    }
    val internetConnection: MutableLiveData<TypeInternetConnection> = MutableLiveData()

    init {
        setInternetConnectionListener(::changeInternetConnectionState)
        if (visitor != null) {
            // продумать логику проверки нового юзера со старым, чтобы показать пользователь чат\ даже если нет инета и не получается пройти аутентификацию
            view.showChat()
            logIn(
                visitor,
                {
                    // auth success;
                    view.showChat()
                    syncData()
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
            logIn(
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

    private fun changeInternetConnectionState(type: TypeInternetConnection) {
        internetConnection.postValue(type)
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

    fun sendMessage(message: String) {
        launchIO {
            sendMessages(
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
            uploadFiles(file, {}, {})
        }
    }

    fun sendFiles(fileList: List<File>) {
        launchIO {
            uploadFiles(fileList, {}, {})
        }
    }

    fun sendImage(bitmap: Bitmap) {
        launchIO {
            uploadFiles(bitmap, {}, {})
        }
    }

    // мб синхронизировать при включении экрана: пользователь выключил экран\ зашел\ обновил гдето\ и открыл прилку
    private fun syncData(timestamp: Long = 0L) {
        launchIO {
            Log.d(ConstantsUtils.TAG_SOCKET, "ViewModel sync")
            syncMessages(
                timestamp,//dao.getLastTime()
                {},
                {
                    handleError(it)
                }
            )
        }
    }

}