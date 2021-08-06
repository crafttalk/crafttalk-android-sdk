package com.crafttalk.chat.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.InternetConnectionState
import com.crafttalk.chat.domain.interactors.AuthInteractor
import com.crafttalk.chat.domain.interactors.ChatMessageInteractor
import com.crafttalk.chat.domain.interactors.CustomizingChatBehaviorInteractor
import com.crafttalk.chat.domain.interactors.FileInteractor
import com.crafttalk.chat.presentation.base.BaseViewModel
import com.crafttalk.chat.presentation.feature.view_picture.ShowImageDialog
import com.crafttalk.chat.presentation.helper.groupers.groupPageByDate
import com.crafttalk.chat.presentation.helper.mappers.messageModelMapper
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.utils.ChatAttr
import javax.inject.Inject
import kotlin.math.min

class ChatViewModel
@Inject constructor(
    private val authChatInteractor: AuthInteractor,
    private val chatMessageInteractor: ChatMessageInteractor,
    private val fileInteractor: FileInteractor,
    private val customizingChatBehaviorInteractor: CustomizingChatBehaviorInteractor,
    private val context: Context
) : BaseViewModel() {

    var countUnreadMessages = MutableLiveData(0)
    val scrollToDownVisible = MutableLiveData(false)
    val feedbackContainerVisible = MutableLiveData(false)
    val uploadHistoryVisible = MutableLiveData(false)

    val firstUploadMessages = MutableLiveData<Int?>(null)
    val uploadMessagesForUser: MutableLiveData<LiveData<PagedList<MessageModel>>> = MutableLiveData()
    private fun uploadMessages() {
        val dataSource = chatMessageInteractor.getAllMessages()
            ?.map<MessageModel> { (messageModelMapper(it, context)) }
            ?.mapByPage { groupPageByDate(it) } ?: return
        val pagedListBuilder: LivePagedListBuilder<Int, MessageModel>  = LivePagedListBuilder<Int, MessageModel>(
            dataSource,
            PAGE_SIZE
        ).setBoundaryCallback(object : PagedList.BoundaryCallback<MessageModel>() {
            override fun onZeroItemsLoaded() {
                super.onZeroItemsLoaded()
                launchIO {
                    chatMessageInteractor.syncMessages(true)
                }
            }
        })
        uploadMessagesForUser.postValue(pagedListBuilder.build())
    }

    val internetConnectionState: MutableLiveData<InternetConnectionState> = MutableLiveData()
    val displayableUIObject = MutableLiveData(DisplayableUIObject.NOTHING)
    var clientInternetConnectionListener: ChatInternetConnectionListener? = null
    private val internetConnectionListener = object : ChatInternetConnectionListener {
        override fun connect() {
            launchUI { clientInternetConnectionListener?.connect() }
            internetConnectionState.postValue(InternetConnectionState.HAS_INTERNET)
        }
        override fun failConnect() {
            launchUI { clientInternetConnectionListener?.failConnect() }
            internetConnectionState.postValue(InternetConnectionState.NO_INTERNET)
        }
        override fun lossConnection() {
            launchUI { clientInternetConnectionListener?.lossConnection() }
            internetConnectionState.postValue(InternetConnectionState.NO_INTERNET)
        }
        override fun reconnect() {
            launchUI { clientInternetConnectionListener?.reconnect() }
            internetConnectionState.postValue(InternetConnectionState.RECONNECT)
        }
    }
    private val chatEventListener = object : ChatEventListener {
        override fun operatorStartWriteMessage() { displayableUIObject.postValue(DisplayableUIObject.OPERATOR_START_WRITE_MESSAGE) }
        override fun operatorStopWriteMessage()  { displayableUIObject.postValue(DisplayableUIObject.OPERATOR_STOP_WRITE_MESSAGE) }
        override fun finishDialog() { feedbackContainerVisible.postValue(true) }
    }
    var uploadFileListener: UploadFileListener? = null

    init {
        customizingChatBehaviorInteractor.setInternetConnectionListener(internetConnectionListener)
        customizingChatBehaviorInteractor.goToChatScreen()

        Handler().postDelayed({
            authChatInteractor.logIn(
                successAuthUi = {
                    displayableUIObject.postValue(DisplayableUIObject.CHAT)
                    uploadMessages()
                },
                failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                firstLogInWithForm = { displayableUIObject.value = DisplayableUIObject.FORM_AUTH },
                chatEventListener = chatEventListener
            )
        }, ChatAttr.getInstance().timeDelayed)
    }

    override fun onCleared() {
        super.onCleared()
        customizingChatBehaviorInteractor.leaveChatScreen()
    }

    fun registration(vararg args: String) {
        Handler().postDelayed({
            authChatInteractor.logIn(
                visitor = Visitor.map(args),
                successAuthUi = {
                    displayableUIObject.postValue(DisplayableUIObject.CHAT)
                    uploadMessages()
                },
                failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                chatEventListener = chatEventListener
            )
        }, ChatAttr.getInstance().timeDelayed)
    }

    fun reload() {
        Handler().postDelayed({
            authChatInteractor.logIn(
                successAuthUi = {
                    displayableUIObject.postValue(DisplayableUIObject.CHAT)
                    uploadMessages()
                },
                failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                chatEventListener = chatEventListener
            )
        }, ChatAttr.getInstance().timeDelayed)
    }

    fun uploadOldMessages() {
        launchIO {
            chatMessageInteractor.syncMessages(false)
        }
    }

    fun openFile(context: Context, fileUrl: String) {
        val intentView = Intent(Intent.ACTION_VIEW).apply {
            data = fileUrl.toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val intentChooser = Intent.createChooser(intentView, context.getString(R.string.com_crafttalk_chat_string_chooser_open_file_action_view))
        if (intentView.resolveActivity(context.packageManager) != null) {
            context.startActivity(intentChooser)
        }
    }

    fun openImage(activity: Activity, imageName: String, imageUrl: String, downloadFun: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit) {
        ShowImageDialog.Builder(activity)
            .setName(imageName)
            .setUrl(imageUrl)
            .setType(TypeFile.IMAGE)
            .setFunDownload(downloadFun)
            .show()
    }

    fun openGif(activity: Activity, gifName: String, gifUrl: String, downloadFun: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit) {
        ShowImageDialog.Builder(activity)
            .setName(gifName)
            .setUrl(gifUrl)
            .setType(TypeFile.GIF)
            .setFunDownload(downloadFun)
            .show()
    }

    fun selectAction(messageId: String, actionId: String) {
        launchIO {
            chatMessageInteractor.selectActionInMessage(messageId, actionId)
        }
    }

    fun giveFeedbackOnOperator(countStars: Int) {
        launchIO {
            customizingChatBehaviorInteractor.giveFeedbackOnOperator(countStars)
        }
    }

    fun updateData(idKey: Long, height: Int, width: Int) {
        launchIO {
            chatMessageInteractor.updateSizeMessage(idKey, height, width)
        }
    }

    fun sendMessage(message: String) {
        launchIO {
            chatMessageInteractor.sendMessage(message)
        }
    }

    fun sendFile(file: File) {
        launchIO { fileInteractor.uploadFile(file) { responseCode, responseMessage ->
            uploadFileListener?.let { listener -> handleUploadFile(listener, responseCode, responseMessage) }
        }}
    }

    fun sendFiles(fileList: List<File>) {
        launchIO { fileInteractor.uploadFiles(fileList) { responseCode, responseMessage ->
            uploadFileListener?.let { listener -> handleUploadFile(listener, responseCode, responseMessage) }
        }}
    }

    fun updateValueCountUnreadMessages(indexLastVisibleItem: Int) {
        launchIO {
            val list = uploadMessagesForUser.value?.value?.toList()?.filterNotNull() ?: return@launchIO

            when (val indexLastReadMessage = list.indexOfFirst { it.isReadMessage }) {
                -1 -> {
                    countUnreadMessages.postValue(min(list.size, indexLastVisibleItem))
                    scrollToDownVisible.postValue(indexLastVisibleItem >= MAX_COUNT_UNREAD_MESSAGES)
                    for (i in indexLastVisibleItem until list.size) {
                        chatMessageInteractor.readMessage(list[i].id)
                    }
                }
                0 -> {
                    countUnreadMessages.postValue(0)
                    scrollToDownVisible.postValue(indexLastVisibleItem >= MAX_COUNT_UNREAD_MESSAGES)
                }
                else -> {
                    countUnreadMessages.postValue(min(indexLastReadMessage, indexLastVisibleItem))
                    scrollToDownVisible.postValue(indexLastVisibleItem >= MAX_COUNT_UNREAD_MESSAGES)
                    for (i in indexLastVisibleItem until indexLastReadMessage) {
                        chatMessageInteractor.readMessage(list[i].id)
                    }
                }
            }
        }
    }

    fun setValueCountUnreadMessages() {
        launchIO {
            val list = uploadMessagesForUser.value?.value?.toList()?.filterNotNull() ?: return@launchIO

            when (val indexLastReadMessage = list.indexOfFirst { it.isReadMessage }) {
                -1 -> firstUploadMessages.postValue(list.size - 1)
                0 -> firstUploadMessages.postValue(0)
                else -> firstUploadMessages.postValue(indexLastReadMessage - 1)
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 20
        const val MAX_COUNT_UNREAD_MESSAGES = 1
    }

}