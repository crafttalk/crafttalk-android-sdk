package com.crafttalk.chat.presentation

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.InternetConnectionState
import com.crafttalk.chat.domain.interactors.AuthInteractor
import com.crafttalk.chat.domain.interactors.ConditionInteractor
import com.crafttalk.chat.domain.interactors.ConfigurationInteractor
import com.crafttalk.chat.domain.interactors.FeedbackInteractor
import com.crafttalk.chat.domain.interactors.FileInteractor
import com.crafttalk.chat.domain.interactors.MessageInteractor
import com.crafttalk.chat.domain.interactors.SearchInteractor
import com.crafttalk.chat.domain.interactors.SearchItem
import com.crafttalk.chat.presentation.base.BaseViewModel
import com.crafttalk.chat.presentation.feature.view_picture.ShowMediaDialog2
import com.crafttalk.chat.presentation.helper.groupers.groupPageByDate
import com.crafttalk.chat.presentation.helper.mappers.messageModelMapper
import com.crafttalk.chat.presentation.helper.mappers.messageSearchMapper
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.ChatParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject
import com.crafttalk.chat.domain.entity.file.File as DomainFile
import java.io.File as IOFile


class ChatViewModel
@Inject constructor(
    private val authChatInteractor: AuthInteractor,
    private val messageInteractor: MessageInteractor,
    private val searchInteractor: SearchInteractor,
    private val fileInteractor: FileInteractor,
    private val conditionInteractor: ConditionInteractor,
    private val feedbackInteractor: FeedbackInteractor,
    private val configurationInteractor: ConfigurationInteractor,
    private val context: Context
) : BaseViewModel() {
    var currentReadMessageTime = conditionInteractor.getCurrentReadMessageTime()
    var isAllHistoryLoaded = conditionInteractor.checkFlagAllHistoryLoaded()
    var initialLoadKey = conditionInteractor.getInitialLoadKey()
    private var initSearchInitialLoadKey = initialLoadKey

    var countUnreadMessages = MutableLiveData<Int>()
    val scrollToDownVisible = MutableLiveData(false)
    val feedbackContainerVisible = MutableLiveData(false)
    val openDocument = MutableLiveData<Pair<IOFile?, Boolean>?>()
    val mergeHistoryBtnVisible = MutableLiveData(false)
    val mergeHistoryProgressVisible = MutableLiveData(false)
    var userTypingInterval: Int = 1000
    var userTyping: Boolean = true
    var chatIsClosed: Boolean = false
    var chatClosedMessage: String = ""
    var dialogID1: String? = null

    var searchText: String? = null
    val showSearchNavigate: MutableLiveData<Boolean> = MutableLiveData(false)
    val enabledSearchTop: MutableLiveData<Boolean> = MutableLiveData(false)
    val enabledSearchBottom: MutableLiveData<Boolean> = MutableLiveData(false)
    val searchCoincidenceText: MutableLiveData<String> = MutableLiveData()
    val searchScrollToPosition: MutableLiveData<SearchItem?> = MutableLiveData()

    val uploadMessagesForUser: MutableLiveData<LiveData<PagedList<MessageModel>>> =
        MutableLiveData()
    val replyMessage: MutableLiveData<MessageModel?> = MutableLiveData(null)
    val replyMessagePosition: MutableLiveData<Int?> = MutableLiveData(null)

    private fun uploadMessages() {
        val config = PagedList.Config.Builder()
            .setPageSize(ChatParams.pageSize)
            .build()
        val dataSource = messageInteractor.getAllMessages()
            .map { (messageModelMapper(it, context)) }
            .mapByPage { groupPageByDate(it) }
        val pagedListBuilder: LivePagedListBuilder<Int, MessageModel> = LivePagedListBuilder(
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

    private val eventStateHistoryLoaded: (isAllHistoryLoaded: Boolean) -> Unit = {
        isAllHistoryLoaded = it
    }
    private val sync: suspend () -> Unit = {
        launchUI { chatStateListener?.startSynchronization() }
        displayableUIObject.postValue(DisplayableUIObject.SYNCHRONIZATION)
        Log.d("CTALK_TEST_DATA_LOP_S", "sync")
        messageInteractor.syncMessages(
            updateReadPoint = updateCurrentReadMessageTime,
            syncMessagesAcrossDevices = ::syncMessagesAcrossDevices,
            eventStateHistoryLoaded = eventStateHistoryLoaded,
            updateSearchMessagePosition = searchInteractor::updateMessagePosition
        )
    }
    private val updateCurrentReadMessageTime: (List<Pair<String, Long>>) -> Boolean =
        { newTimeMarks ->
            //Log.d("CTALK_TEST_DATA_LOP", "updateCurrentReadMessageTime 1 newTimeMark - $newTimeMarks; currentReadMessageTime - ${currentReadMessageTime}")
            newTimeMarks.forEach { pair ->
                val id = pair.first
                val time = pair.second
                if (time > currentReadMessageTime) {
                    launchIO {
                        //Log.d("CTALK_TEST_DATA_LOP", "readMessage id - ${id}; time - ${time};")
                        messageInteractor.readMessage(
                            messageId = id
                        )
                    }
                }
            }
            val maxTime = newTimeMarks.maxByOrNull { it.second }?.second
            //Log.d("CTALK_TEST_DATA_LOP", "updateCurrentReadMessageTime 2 currentReadMessageTime - $currentReadMessageTime; maxTime - $maxTime;")
            if (maxTime != null && maxTime > currentReadMessageTime) {
                currentReadMessageTime = maxTime
                //.d("CTALK_TEST_DATA_LOP", "updateCurrentReadMessageTime 3 true;")
                true
            } else {
                //Log.d("CTALK_TEST_DATA_LOP", "updateCurrentReadMessageTime 4 false;")
                false
            }
        }

    val internetConnectionState: MutableLiveData<InternetConnectionState> = MutableLiveData()
    val displayableUIObject = MutableLiveData(DisplayableUIObject.NOTHING)
    var clientInternetConnectionListener: ChatInternetConnectionListener? = null
    var mergeHistoryListener: MergeHistoryListener = object : MergeHistoryListener {
        override fun showDialog() {
            mergeHistoryProgressVisible.postValue(false)
            mergeHistoryBtnVisible.postValue(true)
        }

        override fun startMerge() {
            mergeHistoryBtnVisible.postValue(false)
            mergeHistoryProgressVisible.postValue(true)
        }

        override fun endMerge() {
            mergeHistoryProgressVisible.postValue(false)
            mergeHistoryBtnVisible.postValue(false)
        }
    }
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
        override fun operatorStartWriteMessage() {
            displayableUIObject.postValue(DisplayableUIObject.OPERATOR_START_WRITE_MESSAGE)
        }

        override fun operatorStopWriteMessage() {
            displayableUIObject.postValue(DisplayableUIObject.OPERATOR_STOP_WRITE_MESSAGE)
        }

        override fun finishDialog(dialogId: String?) {
            feedbackContainerVisible.postValue(true)
            dialogID1 = dialogId
        }

        override fun showUploadHistoryBtn() {
            mergeHistoryListener.showDialog()
        }

        override fun synchronized() {
            launchUI { chatStateListener?.endSynchronization() }
            displayableUIObject.postValue(DisplayableUIObject.CHAT)
        }

        override fun updateDialogScore() {
            displayableUIObject.postValue(DisplayableUIObject.CLOSE_FEEDBACK_CONTAINER)
        }

        override fun setUserTypingInterval(int: Int) {
            userTypingInterval = int
        }

        override fun setUserTyping(boolean: Boolean) {
            userTyping = boolean
        }

        override fun setChatStateClosed(boolean: Boolean, string: String) {
            chatIsClosed = boolean
            chatClosedMessage = string
            displayableUIObject.postValue(DisplayableUIObject.CHATCLOSED)
        }
    }
    var uploadFileListener: UploadFileListener? = null

    init {
        conditionInteractor.setInternetConnectionListener(internetConnectionListener)
        conditionInteractor.goToChatScreen()
        launchIO {
            configurationInteractor.getConfiguration()
        }
        messageInteractor.setUpdateSearchMessagePosition(searchInteractor::updateMessagePosition)
    }

    fun onStartChatView(visitor: Visitor?) {
        launchIO {
            messageInteractor.clearDbIfMessagesDuplicated(context)
            launchUI {
                delay(ChatAttr.getInstance().timeDelayed)
                authChatInteractor.logIn(
                    visitor = visitor,
                    successAuthUi = ::deliverMessagesToUser,
                    sync = sync,
                    failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                    firstLogInWithForm = {
                        displayableUIObject.value = DisplayableUIObject.FORM_AUTH
                    },
                    updateCurrentReadMessageTime = updateCurrentReadMessageTime,
                    chatEventListener = chatEventListener
                )
            }
        }
    }

    fun onStop() {
        currentReadMessageTime.run(conditionInteractor::saveCurrentReadMessageTime)
        countUnreadMessages.value?.run(conditionInteractor::saveCountUnreadMessages)
    }

    override fun onCleared() {
        super.onCleared()
        conditionInteractor.leaveChatScreen()
        removeAllInfoMessages()
    }

    fun registration(vararg args: String) {
        launchIO {
            messageInteractor.clearDbIfMessagesDuplicated(context)
            launchUI {
                delay(ChatAttr.getInstance().timeDelayed)
                authChatInteractor.logIn(
                    visitor = Visitor.map(args, ChatParams.addedFieldsForRegistrationVisitor),
                    successAuthUi = ::deliverMessagesToUser,
                    sync = sync,
                    failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                    updateCurrentReadMessageTime = updateCurrentReadMessageTime,
                    chatEventListener = chatEventListener
                )
            }
        }
    }

    fun reload() {
        launchIO {
            messageInteractor.clearDbIfMessagesDuplicated(context)
            launchUI {
                delay(ChatAttr.getInstance().timeDelayed)
                authChatInteractor.logIn(
                    successAuthUi = ::deliverMessagesToUser,
                    sync = sync,
                    failAuthUi = { displayableUIObject.postValue(DisplayableUIObject.WARNING) },
                    updateCurrentReadMessageTime = updateCurrentReadMessageTime,
                    chatEventListener = chatEventListener
                )
            }
        }
    }

    fun uploadOldMessages(uploadHistoryComplete: () -> Unit = {}, executeAnyway: Boolean = false) {
        launchIO {
            messageInteractor.uploadHistoryMessages(
                eventStateHistoryLoaded = eventStateHistoryLoaded,
                uploadHistoryComplete = uploadHistoryComplete,
                updateSearchMessagePosition = searchInteractor::updateMessagePosition,
                executeAnyway = executeAnyway
            )
        }
    }

    fun downloadOrOpenDocument(
        id: String,
        documentName: String,
        documentUrl: String
    ) {
        launchIO {
            fileInteractor.downloadDocument(
                id = id,
                documentName = documentName,
                documentUrl = documentUrl,
                directory = context.filesDir,
                openDocument = { documentFile ->
                    delay(ChatAttr.getInstance().delayDownloadDocument)
                    openDocument.postValue(Pair(documentFile, true))
                },
                downloadFailed = {
                    openDocument.postValue(Pair(null, false))
                }
            )
        }
    }

    fun openImage(
        imageName: String,
        imageUrl: String,
        downloadFun: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit
    ) {
        val intent = Intent(context, ShowMediaDialog2::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("url", imageUrl)
        intent.putExtra("imageName", imageName)
        intent.putExtra("typeFile", TypeFile.IMAGE.toString())
        startActivity(context, intent, null)
    }

    fun openGif(
        gifName: String,
        gifUrl: String,
        downloadFun: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit
    ) {
        val intent = Intent(context, ShowMediaDialog2::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("url", gifUrl)
        intent.putExtra("imageName", gifName)
        intent.putExtra("typeFile", TypeFile.GIF.toString())
        startActivity(context, intent, null)

    }

    fun selectAction(messageId: String, actionId: String) {
        launchIO {
            messageInteractor.selectActionInMessage(messageId, actionId)
        }
    }

    fun selectButton(messageId: String, actionId: String, buttonId: String) {
        launchIO {
            messageInteractor.selectButtonInMessage(messageId, actionId, buttonId)
        }
    }

    fun selectButtonInWidget(actionId: String) {
        launchIO {
            messageInteractor.selectButtonInWidget(actionId)
        }
    }

    fun selectReplyMessage(messageId: String) {
        launchIO {
            replyMessagePosition.postValue(
                messageInteractor.getCountMessagesInclusiveTimestampById(
                    messageId
                )
            )
        }
    }

    /**Отправляет системное сообщение с оценкой диалога
     * Вызывается когда пользоватль нажимает на звездочки
     *
     *      countStars: Int -- оценка диалога от 1 до 5.
     *      finishReason:String -- причина закрытия, по умолчанию null,
     *      Если клиент закрывает чат не оценивая диалог то нужно отправить "CLOSED_BY_CLIENT".
     *      dialogID:String -- ID диалога который нужно оценить, если null то оценивается самый последний диалог
     */
    fun giveFeedbackOnOperator(countStars: Int?, finishReason: String?, dialogID: String?) {
        launchIO {
            feedbackInteractor.giveFeedbackOnOperator(countStars, finishReason, dialogID)
        }
    }

    fun updateData(id: String, height: Int, width: Int) {
        launchIO {
            messageInteractor.updateSizeMessage(id, height, width)
        }
    }

    fun sendMessage(message: String, repliedMessageId: String?) {
        launchIO {
            messageInteractor.sendMessage(
                message = message,
                repliedMessageId = repliedMessageId
            )
        }
    }

    fun sendServiceMessageUserIsTypingText(message: String) {
        launchIO {
            messageInteractor.sendServiceMessageUserIsTypingText(message)
        }

    }

    fun sendServiceMessageUserStopTypingText() {
        launchIO {
            messageInteractor.sendServiceMessageUserStopTypingText()
        }
    }

    fun sendFile(file: DomainFile) {
        launchIO {
            fileInteractor.uploadFile(file) { responseCode, responseMessage ->
                uploadFileListener?.let { listener ->
                    handleUploadFile(
                        listener,
                        responseCode,
                        responseMessage
                    )
                }
            }
        }
    }

    fun sendFiles(fileList: List<DomainFile>) {
        launchIO {
            fileInteractor.uploadFiles(fileList) { responseCode, responseMessage ->
                uploadFileListener?.let { listener ->
                    handleUploadFile(
                        listener,
                        responseCode,
                        responseMessage
                    )
                }
            }
        }
    }

    fun uploadSearchMessages(
        searchText: String,
        currentSearchItem: SearchItem?
    ): LiveData<PagedList<MessageModel>> {
        val config = PagedList.Config.Builder()
            .setPageSize(ChatParams.pageSize)
            .build()
        val dataSource = messageInteractor.getAllMessages()
            .map { (messageModelMapper(it, context)) }
            .map {
                messageSearchMapper(
                    it,
                    searchText.trim(),
                    currentSearchItem,
                    searchInteractor.getAllSearchedItems()
                )
            }
            .mapByPage { groupPageByDate(it) }
        val pagedListBuilder: LivePagedListBuilder<Int, MessageModel> = LivePagedListBuilder(
            dataSource,
            config
        ).setBoundaryCallback(object : PagedList.BoundaryCallback<MessageModel>() {
            override fun onItemAtEndLoaded(itemAtEnd: MessageModel) {
                super.onItemAtEndLoaded(itemAtEnd)
                if (!isAllHistoryLoaded) {
                    uploadOldMessages()
                }
            }
        }).setInitialLoadKey(initSearchInitialLoadKey)
        return pagedListBuilder.build()
    }

    private var lastSearchJob: Job? = null

    fun onSearchClick(searchText: String, searchStart: () -> Unit) {
        if (lastSearchJob?.isActive == true) {
            lastSearchJob?.cancel()
        }
        lastSearchTopJob?.cancel()
        if (searchText.isEmpty()) {
            showSearchNavigate.postValue(false)
            enabledSearchTop.postValue(false)
            enabledSearchBottom.postValue(false)
            searchCoincidenceText.postValue("")
            searchScrollToPosition.postValue(null)
            searchInteractor.cancelSearch()
            return
        }
        lastSearchJob = launchIO {
            this.searchText = searchText
            delay(1000)
            val searchItem = searchInteractor.preloadMessages(searchText.trim()) {
                launchUI { searchStart() }
            }
            if (searchItem == null) {
                searchCoincidenceText.postValue(
                    context.resources.getString(
                        R.string.com_crafttalk_chat_coincidence_not_found
                    )
                )
                showSearchNavigate.postValue(false)
                enabledSearchTop.postValue(false)
                enabledSearchBottom.postValue(false)
                searchScrollToPosition.postValue(null)
            } else {
                initSearchInitialLoadKey = searchItem.scrollPosition ?: initialLoadKey
                searchCoincidenceText.postValue(
                    context.resources.getString(
                        R.string.com_crafttalk_chat_coincidence,
                        searchItem.searchPosition,
                        searchItem.allCount
                    )
                )
                enabledSearchTop.postValue(searchItem.allCount != 1)
                enabledSearchBottom.postValue(searchItem.searchPosition != 1)
                showSearchNavigate.postValue(searchItem.allCount != 1)
                searchScrollToPosition.postValue(searchItem)
            }
        }
    }

    private var lastSearchTopJob: Job? = null

    fun onSearchTopClick() {
        if (lastSearchTopJob?.isActive == true) return
        lastSearchTopJob = launchIO {
            val searchItem = searchInteractor.onSearchTopClick()
            if (searchItem == null) {
                showSearchNavigate.postValue(true)
                enabledSearchTop.postValue(false)
                enabledSearchBottom.postValue(true)
            } else {
                initSearchInitialLoadKey = searchItem.scrollPosition ?: initialLoadKey
                searchCoincidenceText.postValue(
                    context.resources.getString(
                        R.string.com_crafttalk_chat_coincidence,
                        searchItem.searchPosition,
                        searchItem.allCount
                    )
                )
                enabledSearchTop.postValue(!searchItem.isLast)
                enabledSearchBottom.postValue(true)
                showSearchNavigate.postValue(true)
                searchScrollToPosition.postValue(searchItem)
            }
        }
    }

    fun onSearchBottomClick() {
        val searchItem = searchInteractor.onSearchBottomClick()
        if (searchItem == null) {
            showSearchNavigate.postValue(true)
            enabledSearchTop.postValue(true)
            enabledSearchBottom.postValue(false)
        } else {
            initSearchInitialLoadKey = searchItem.scrollPosition ?: initialLoadKey
            searchCoincidenceText.postValue(
                context.resources.getString(
                    R.string.com_crafttalk_chat_coincidence,
                    searchItem.searchPosition,
                    searchItem.allCount
                )
            )
            enabledSearchTop.postValue(true)
            enabledSearchBottom.postValue(!searchItem.isLast)
            showSearchNavigate.postValue(true)
            searchScrollToPosition.postValue(searchItem)
        }
    }

    fun onSearchCancel() {
        lastSearchJob?.cancel()
        lastSearchTopJob?.cancel()
        initSearchInitialLoadKey = initialLoadKey
        searchText = null
        showSearchNavigate.postValue(false)
        enabledSearchTop.postValue(false)
        enabledSearchBottom.postValue(false)
        searchCoincidenceText.postValue("")
        searchScrollToPosition.postValue(null)
        uploadMessages()
        searchInteractor.cancelSearch()
    }

    fun readMessage(messageModel: MessageModel?) {
        messageModel ?: return

        val isReadNewMessage = updateCurrentReadMessageTime(
            listOf(Pair(messageModel.id, messageModel.timestamp))
        )
        //Log.d("CTALK_TEST_DATA_LOP", "VM readMessage 2 isReadNewMessage - $isReadNewMessage; messageModel - $messageModel;")
        if (isReadNewMessage) {
            updateCountUnreadMessages()
        }
    }

    fun updateCountUnreadMessages(
        timestampLastMessage: Long? = null,
        actionUiAfter: (Int) -> Unit = {}
    ) {
        launchIO {
            val unreadMessagesCount = messageInteractor.getCountUnreadMessages(
                currentReadMessageTime,
                timestampLastMessage
            )
            unreadMessagesCount?.run(countUnreadMessages::postValue)
            launchUI {
                unreadMessagesCount?.run(actionUiAfter)
            }
        }
    }

    private fun removeAllInfoMessages() {
        launchIO {
            messageInteractor.removeAllInfoMessages()
        }
    }

    companion object {
        const val MAX_COUNT_MESSAGES_NEED_SCROLLED_BEFORE_APPEARANCE_BTN_SCROLL = 1
        const val DELAY_RENDERING_SCROLL_BTN = 100L
    }
}