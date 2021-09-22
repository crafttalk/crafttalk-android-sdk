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
import com.crafttalk.chat.domain.interactors.*
import com.crafttalk.chat.presentation.base.BaseViewModel
import com.crafttalk.chat.presentation.feature.view_picture.ShowImageDialog
import com.crafttalk.chat.presentation.helper.groupers.groupPageByDate
import com.crafttalk.chat.presentation.helper.mappers.messageModelMapper
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.ChatParams
import javax.inject.Inject

class ChatViewModel
@Inject constructor(
    private val authChatInteractor: AuthInteractor,
    private val messageInteractor: MessageInteractor,
    private val fileInteractor: FileInteractor,
    private val conditionInteractor: ConditionInteractor,
    private val feedbackInteractor: FeedbackInteractor,
    private val context: Context
) : BaseViewModel() {

    var currentReadMessageTime = conditionInteractor.getCurrentReadMessageTime()
    var isAllHistoryLoaded = conditionInteractor.checkFlagAllHistoryLoaded()
    var initialLoadKey = conditionInteractor.getInitialLoadKey()

    var countUnreadMessages = MutableLiveData<Int>()
    val scrollToDownVisible = MutableLiveData(false)
    val feedbackContainerVisible = MutableLiveData(false)

    val uploadMessagesForUser: MutableLiveData<LiveData<PagedList<MessageModel>>> = MutableLiveData()
    private fun uploadMessages() {
        val config = PagedList.Config.Builder()
            .setPageSize(ChatParams.pageSize)
            .build()
        val dataSource = messageInteractor.getAllMessages()
            .map { (messageModelMapper(it, context)) }
            .mapByPage { groupPageByDate(it) }
        val pagedListBuilder: LivePagedListBuilder<Int, MessageModel>  = LivePagedListBuilder(
            dataSource,
            config
        ).setBoundaryCallback(object : PagedList.BoundaryCallback<MessageModel>() {
            override fun onItemAtEndLoaded(itemAtEnd: MessageModel) {
                super.onItemAtEndLoaded(itemAtEnd)
                if (!isAllHistoryLoaded) {
                    uploadOldMessages()
                }
            }
        }).setInitialLoadKey(initialLoadKey)
        uploadMessagesForUser.postValue(pagedListBuilder.build())
    }
    private fun syncMessagesAcrossDevices(indexFirstUnreadMessage: Int) {
        initialLoadKey = indexFirstUnreadMessage
        uploadMessages()
    }
    private fun deliverMessagesToUser() {
        if (uploadMessagesForUser.value == null) {
            uploadMessages()
        }
    }
    private val eventAllHistoryLoaded: () -> Unit = {
        isAllHistoryLoaded = true
    }
    private val sync: suspend () -> Unit = {
        launchUI { chatStateListener?.startSynchronization() }
        displayableUIObject.postValue(DisplayableUIObject.SYNCHRONIZATION)
        messageInteractor.syncMessages(
            updateReadPoint = updateCurrentReadMessageTime,
            syncMessagesAcrossDevices = ::syncMessagesAcrossDevices,
            eventAllHistoryLoaded = eventAllHistoryLoaded
        )
    }
    private val updateCurrentReadMessageTime: (Long) -> Boolean = { newTimeMark ->
        if (newTimeMark > currentReadMessageTime) {
            currentReadMessageTime = newTimeMark
            true
        } else {
            false
        }
    }

    val internetConnectionState: MutableLiveData<InternetConnectionState> = MutableLiveData()
    val displayableUIObject = MutableLiveData(DisplayableUIObject.NOTHING)
    var clientInternetConnectionListener: ChatInternetConnectionListener? = null
    var mergeHistoryListener: MergeHistoryListener? = null
    var chatStateListener: ChatStateListener? = null
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
        override fun showUploadHistoryBtn() { mergeHistoryListener?.showDialog() }
        override fun synchronized() {
            launchUI { chatStateListener?.endSynchronization() }
            displayableUIObject.postValue(DisplayableUIObject.CHAT)
        }
    }
    var uploadFileListener: UploadFileListener? = null

    init {
        conditionInteractor.setInternetConnectionListener(internetConnectionListener)
        conditionInteractor.goToChatScreen()
    }

    fun onStartChatView(visitor: Visitor?) {
        Handler().postDelayed({
            authChatInteractor.logIn(
                visitor = visitor,
                successAuthUi = ::deliverMessagesToUser,
                sync = sync,
                failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                firstLogInWithForm = { displayableUIObject.value = DisplayableUIObject.FORM_AUTH },
                updateCurrentReadMessageTime = updateCurrentReadMessageTime,
                chatEventListener = chatEventListener
            )
        }, ChatAttr.getInstance().timeDelayed)
    }

    fun onStop() {
        currentReadMessageTime.run(conditionInteractor::saveCurrentReadMessageTime)
        countUnreadMessages.value?.run(conditionInteractor::saveCountUnreadMessages)
    }

    override fun onCleared() {
        super.onCleared()
        conditionInteractor.leaveChatScreen()
    }

    fun registration(vararg args: String) {
        Handler().postDelayed({
            authChatInteractor.logIn(
                visitor = Visitor.map(args),
                successAuthUi = ::deliverMessagesToUser,
                sync = sync,
                failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                updateCurrentReadMessageTime = updateCurrentReadMessageTime,
                chatEventListener = chatEventListener
            )
        }, ChatAttr.getInstance().timeDelayed)
    }

    fun reload() {
        Handler().postDelayed({
            authChatInteractor.logIn(
                successAuthUi = ::deliverMessagesToUser,
                sync = sync,
                failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                updateCurrentReadMessageTime = updateCurrentReadMessageTime,
                chatEventListener = chatEventListener
            )
        }, ChatAttr.getInstance().timeDelayed)
    }

    fun uploadOldMessages(uploadHistoryComplete: () -> Unit = {}) {
        launchIO {
            messageInteractor.uploadHistoryMessages(
                eventAllHistoryLoaded = eventAllHistoryLoaded,
                uploadHistoryComplete = uploadHistoryComplete
            )
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
            messageInteractor.selectActionInMessage(messageId, actionId)
        }
    }

    fun giveFeedbackOnOperator(countStars: Int) {
        launchIO {
            feedbackInteractor.giveFeedbackOnOperator(countStars)
        }
    }

    fun updateData(id: String, height: Int, width: Int) {
        launchIO {
            messageInteractor.updateSizeMessage(id, height, width)
        }
    }

    fun sendMessage(message: String) {
        launchIO {
            messageInteractor.sendMessage(message)
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

    fun readMessage(lastTimestamp: Long?) {
        val isReadNewMessage = lastTimestamp?.run(updateCurrentReadMessageTime) ?: false
        if (isReadNewMessage) {
            updateCountUnreadMessages()
        }
    }

    fun updateCountUnreadMessages(actionUiAfter: (Int) -> Unit = {}) {
        launchIO {
            val unreadMessagesCount = messageInteractor.getCountUnreadMessages(currentReadMessageTime)
            unreadMessagesCount?.run(countUnreadMessages::postValue)
            launchUI {
                unreadMessagesCount?.run(actionUiAfter)
            }
        }
    }

    companion object {
        const val MAX_COUNT_MESSAGES_NEED_SCROLLED_BEFORE_APPEARANCE_BTN_SCROLL = 1
        const val DELAY_RENDERING_SCROLL_BTN = 100L
    }

}