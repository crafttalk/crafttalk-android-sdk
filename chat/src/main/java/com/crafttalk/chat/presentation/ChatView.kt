package com.crafttalk.chat.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.ERROR_AUDIO
import android.speech.SpeechRecognizer.ERROR_CLIENT
import android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS
import android.speech.SpeechRecognizer.ERROR_NETWORK
import android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT
import android.speech.SpeechRecognizer.ERROR_NO_MATCH
import android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY
import android.speech.SpeechRecognizer.ERROR_SERVER
import android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT
import android.speech.SpeechRecognizer.RESULTS_RECOGNITION
import android.speech.SpeechRecognizer.createSpeechRecognizer
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.crafttalk.chat.R
import com.crafttalk.chat.databinding.ComCrafttalkChatViewHostBinding
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.InternetConnectionState
import com.crafttalk.chat.domain.interactors.SearchItem
import com.crafttalk.chat.initialization.Chat
import com.crafttalk.chat.presentation.ChatViewModel.Companion.DELAY_RENDERING_SCROLL_BTN
import com.crafttalk.chat.presentation.ChatViewModel.Companion.MAX_COUNT_MESSAGES_NEED_SCROLLED_BEFORE_APPEARANCE_BTN_SCROLL
import com.crafttalk.chat.presentation.adapters.AdapterListMessages
import com.crafttalk.chat.presentation.custom_views.custom_snackbar.WarningSnackbar
import com.crafttalk.chat.presentation.feature.file_viewer.BottomSheetFileViewer
import com.crafttalk.chat.presentation.feature.file_viewer.Option
import com.crafttalk.chat.presentation.feature.view_picture.ShowImageDialog
import com.crafttalk.chat.presentation.helper.downloaders.downloadResource
import com.crafttalk.chat.presentation.helper.extensions.delayOnLifecycle
import com.crafttalk.chat.presentation.helper.extensions.loadMediaFile
import com.crafttalk.chat.presentation.helper.extensions.setFileIcon
import com.crafttalk.chat.presentation.helper.extensions.setFileName
import com.crafttalk.chat.presentation.helper.extensions.setFileSize
import com.crafttalk.chat.presentation.helper.extensions.setMessageText
import com.crafttalk.chat.presentation.helper.extensions.settingMediaFile
import com.crafttalk.chat.presentation.helper.file_viewer_helper.FileViewerHelper
import com.crafttalk.chat.presentation.helper.file_viewer_helper.gellery.PickFileContract
import com.crafttalk.chat.presentation.helper.file_viewer_helper.gellery.TakePicture
import com.crafttalk.chat.presentation.helper.permission.requestPermissionWithAction
import com.crafttalk.chat.presentation.helper.ui.hideSoftKeyboard
import com.crafttalk.chat.presentation.model.DefaultMessageItem
import com.crafttalk.chat.presentation.model.FileMessageItem
import com.crafttalk.chat.presentation.model.GifMessageItem
import com.crafttalk.chat.presentation.model.ImageMessageItem
import com.crafttalk.chat.presentation.model.InfoMessageItem
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.presentation.model.TextMessageItem
import com.crafttalk.chat.presentation.model.TransferMessageItem
import com.crafttalk.chat.presentation.model.TypeMultiple
import com.crafttalk.chat.presentation.model.UnionMessageItem
import com.crafttalk.chat.presentation.model.WidgetMessageItem
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.ChatParams
import com.crafttalk.chat.utils.TypeFailUpload
import com.redmadrobot.inputmask.MaskedTextChangedListener
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil
class ChatView: RelativeLayout, View.OnClickListener, BottomSheetFileViewer.Listener {

    @Inject
    lateinit var viewModel: ChatViewModel
    lateinit var _binding: ComCrafttalkChatViewHostBinding

    private val binding get() = _binding
    private var liveDataMessages: LiveData<PagedList<MessageModel>>? = null
    private var searchLiveDataMessages: LiveData<PagedList<MessageModel>>? = null
    private var searchItemLast: SearchItem? = null
    private var isFirstUploadMessages = false
    private lateinit var adapterListMessages: AdapterListMessages
    private val fileViewerHelper = FileViewerHelper()
    private lateinit var parentFragment: Fragment
    private val inflater: LayoutInflater by lazy {
        context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
    }
    private var dontSendPreviewToOperator: Boolean = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechRecognizerIntent: Intent? = null


    init {
        val inflater = LayoutInflater.from(context)
        _binding = ComCrafttalkChatViewHostBinding.inflate(inflater, this, true)
    }


    private val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }
    private var permissionListener: ChatPermissionListener = object : ChatPermissionListener {
        override fun requestedPermissions(permissions: Array<String>, messages: Array<String>, action: () -> Unit) {
            permissions.forEachIndexed { index, permission ->
                WarningSnackbar.make(
                    view = binding.chatPlace.root,
                    title = messages[index]
                )?.show()
            }
        }
    }
    private var downloadFileListener: DownloadFileListener = object : DownloadFileListener {
        override fun successDownload() {
            if (ShowImageDialog.isOpen()) {
                ShowImageDialog.showWarning(true)
            } else {
                WarningSnackbar.make(
                    view = binding.chatPlace.root,
                    title = ChatAttr.getInstance().titleSuccessDownloadFileWarning,
                    iconRes = R.drawable.com_crafttalk_chat_ic_file_download_done,
                    textColor = ChatAttr.getInstance().colorSuccessDownloadFileWarning,
                    backgroundColor = ChatAttr.getInstance().backgroundSuccessDownloadFileWarning
                )?.show()
            }
        }
        override fun failDownload() {
            if (ShowImageDialog.isOpen()) {
                ShowImageDialog.showWarning(false)
            } else {
                WarningSnackbar.make(
                    view = binding.chatPlace.root,
                    title = ChatAttr.getInstance().titleFailDownloadFileWarning
                )?.show()
            }
        }
        override fun failDownload(title: String) {
            if (ShowImageDialog.isOpen()) {
                ShowImageDialog.showWarning(false)
            } else {
                WarningSnackbar.make(
                    view = binding.chatPlace.root,
                    title = title
                )?.show()
            }
        }
    }
    private var stateStartingProgressListener: StateStartingProgressListener? = null
    private var searchListener: SearchListener = object : SearchListener {
        override fun start() {
            binding.searchPlace.searchInput.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_ic_hourglass),
                binding.searchPlace.searchInput.compoundDrawables[1],
                binding.searchPlace.searchInput.compoundDrawables[2],
                binding.searchPlace.searchInput.compoundDrawables[3]
            )
        }
        override fun stop() {
            binding.searchPlace.searchInput.compoundDrawables
            binding.searchPlace.searchInput.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_ic_search),
                binding.searchPlace.searchInput.compoundDrawables[1],
                binding.searchPlace.searchInput.compoundDrawables[2],
                binding.searchPlace.searchInput.compoundDrawables[3]
            )
        }
    }
    private var downloadID: Long? = null
    private val defaultUploadFileListener: UploadFileListener by lazy {
        object : UploadFileListener {
            override fun successUpload() {}
            override fun failUpload(message: String, type: TypeFailUpload) {
                WarningSnackbar.make(
                    view = binding.chatPlace.root,
                    typeFailUpload = type
                )?.show()
            }
        }
    }
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id: Long? = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID != null && id != null && id != -1L && downloadID == id) {
                downloadFileListener.successDownload()
            }
        }
    }
    private var takePicture: ActivityResultLauncher<Uri>? = null
    private var pickImage: ActivityResultLauncher<Pair<TypeFile, TypeMultiple>>? = null
    private var pickFile: ActivityResultLauncher<Pair<TypeFile, TypeMultiple>>? = null

    private var pickPictureSafe: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickFileSafe: ActivityResultLauncher<Array<String>>? = null

    private var methodGetWidgetView: (widgetId: String) -> View? = { null }
    private var methodFindItemsViewOnWidget: (widgetId: String, widget: View, mapView: MutableMap<String, View>) -> Unit = { _,_,_ -> }
    private var methodBindWidget: (widgetId: String, message: SpannableString?, mapView: MutableMap<String, View>, payload: Any) -> Unit = { _, _, _, _ -> }

    fun setMethodGetPayloadTypeWidget(methodGetPayloadTypeWidget: (widgetId: String) -> Class<out Any>?) {
        ChatParams.methodGetPayloadTypeWidget = methodGetPayloadTypeWidget
    }

    fun setMethodGetWidgetView(methodGetWidgetView: (widgetId: String) -> View?) {
        this.methodGetWidgetView = methodGetWidgetView
    }

    fun setMethodFindItemsViewOnWidget(methodFindItemsViewOnWidget: (widgetId: String, widgetView: View, mapView: MutableMap<String, View>) -> Unit) {
        this.methodFindItemsViewOnWidget = methodFindItemsViewOnWidget
    }

    fun setMethodBindWidget(methodBindWidget: (widgetId: String, message: SpannableString?, mapView: MutableMap<String, View>, payload: Any) -> Unit) {
        this.methodBindWidget = methodBindWidget
    }

    fun clickButtonInWidget(actionId: String) {
        viewModel.selectButtonInWidget(actionId)
    }

    fun setOnPermissionListener(listener: ChatPermissionListener) {
        this.permissionListener = listener
    }

    fun setOnDownloadFileListener(listener: DownloadFileListener) {
        this.downloadFileListener = listener
    }

    fun setOnInternetConnectionListener(listener: ChatInternetConnectionListener) {
        viewModel.clientInternetConnectionListener = listener
    }

    fun setOnChatStateListener(listener: ChatStateListener) {
        viewModel.chatStateListener = listener
    }

    fun setMergeHistoryListener(listener: MergeHistoryListener) {
        viewModel.mergeHistoryListener = listener
    }

    fun setSearchListener(listener: SearchListener) {
        searchListener = listener
    }

    fun mergeHistory() {
        viewModel.mergeHistoryListener.startMerge()
        viewModel.uploadOldMessages(
            uploadHistoryComplete = viewModel.mergeHistoryListener::endMerge,
            executeAnyway = true
        )
    }

    fun setOnUploadFileListener(listener: UploadFileListener) {
        viewModel.uploadFileListener = listener
    }

    fun setOnStateStartingProgressListener(listener: StateStartingProgressListener) {
        this.stateStartingProgressListener = listener
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val attrArr = context.obtainStyledAttributes(attrs, R.styleable.ChatView)
        customizationChat(attrArr)
        attrArr.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("ResourceType")
    private fun customizationChat(attrArr: TypedArray) {

        val chatAttr = ChatAttr.getInstance(attrArr, context)

        // set color
        binding.chatPlace.sendMessage.setColorFilter(chatAttr.colorMain, PorterDuff.Mode.SRC_IN)
        binding.authForm.signIn.setBackgroundDrawable(chatAttr.drawableBackgroundSignInButton)
        binding.warningConnection.setTextColor(chatAttr.colorTextInternetConnectionWarning)
        binding.chatPlace.stateActionOperator.setTextColor(chatAttr.colorTextInfo)
        binding.chatPlace.companyName.setTextColor(chatAttr.colorTextInfo)
        binding.chatPlace.searchSwitchPlace.searchCoincidence.setTextColor(chatAttr.colorTextSearchCoincidence)
        // set dimension
        binding.warningConnection.setTextSize(TypedValue.COMPLEX_UNIT_PX, chatAttr.sizeTextInternetConnectionWarning)
        binding.chatPlace.stateActionOperator.setTextSize(TypedValue.COMPLEX_UNIT_PX, chatAttr.sizeTextInfoText)
        binding.chatPlace.companyName.setTextSize(TypedValue.COMPLEX_UNIT_PX, chatAttr.sizeTextInfoText)
        binding.chatPlace.searchSwitchPlace.searchCoincidence.setTextSize(TypedValue.COMPLEX_UNIT_PX, chatAttr.sizeTextSearchCoincidenceText)
        // set bg
        binding.upperLimiter.setBackgroundColor(chatAttr.colorMain)
        binding.chatPlace.lowerLimit.setBackgroundColor(chatAttr.colorMain)
        ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_background_count_unread_message)?.let { unwrappedDrawable ->
            val wrappedDrawable: Drawable = DrawableCompat.wrap(unwrappedDrawable)
            DrawableCompat.setTint(wrappedDrawable, chatAttr.colorMain)
            binding.chatPlace.countUnreadMessage.background = wrappedDrawable
        }
        binding.chatPlace.searchSwitchPlace.root.setBackgroundColor(chatAttr.backgroundSearchSwitch)
        // set company name
        binding.chatPlace.companyName.text = chatAttr.companyName
        binding.chatPlace.companyName.visibility = if (chatAttr.showCompanyName) View.VISIBLE else View.GONE
        binding.warningConnection.visibility = if (chatAttr.showInternetConnectionState) View.INVISIBLE else View.GONE
        binding.infoChatState.visibility = if (chatAttr.showChatState) View.INVISIBLE else View.GONE
        binding.upperLimiter.visibility = if (chatAttr.showUpperLimiter) View.VISIBLE else View.GONE
        ChatParams.enableSearch = chatAttr.enableSearch
        binding.search.visibility = when {
            chatAttr.showInternetConnectionState || chatAttr.showChatState -> View.INVISIBLE
            else -> View.GONE
        }
        binding.chatPlace.voiceInput.visibility = if (chatAttr.showVoiceInput) View.VISIBLE else View.VISIBLE
        binding.chatPlace.userFeedback.feedbackTitle.apply {
            setTextColor(ChatAttr.getInstance().colorFeedbackTitle)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeFeedbackTitle)
        }
        binding.chatPlace.userFeedback.feedbackStar1.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)
        binding.chatPlace.userFeedback.feedbackStar2.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)
        binding.chatPlace.userFeedback.feedbackStar3.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)
        binding.chatPlace.userFeedback.feedbackStar4.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)
        binding.chatPlace.userFeedback.feedbackStar5.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)
        binding.chatPlace.searchSwitchPlace.searchTop.setColorFilter(ChatAttr.getInstance().colorSearchTop)
        binding.chatPlace.searchSwitchPlace.searchBottom.setColorFilter(ChatAttr.getInstance().colorSearchBottom)

        chatAttr.drawableProgressIndeterminate?.let {
            binding.loading.indeterminateDrawable = it
            binding.warning.warningLoading.indeterminateDrawable = it.constantState?.newDrawable()?.mutate()
        }
        binding.chatPlace.sendMessage.setImageDrawable(ChatAttr.getInstance().drawableAttachFile)
        binding.chatPlace.voiceInput.setImageDrawable(ChatAttr.getInstance().drawableVoiceInputMicOff)
    }
    val executorService = Executors.newSingleThreadScheduledExecutor()
    val sendServiceMessageUserIsTypingTextTask = Runnable {
        val deltaTime = viewModel.userTypingInterval
        if (!dontSendPreviewToOperator) {
            if (System.currentTimeMillis() >= currentTimestamp + deltaTime) {
                viewModel.sendServiceMessageUserIsTypingText(binding.chatPlace.entryField.text.toString())
                currentTimestamp = System.currentTimeMillis()
            }
        }
    }

    val userStopTypingTask = Runnable {
        viewModel.sendServiceMessageUserStopTypingText()
    }

    private var currentTimestamp = System.currentTimeMillis()
    @SuppressLint("ClickableViewAccessibility")
    private fun setAllListeners() {
        binding.authForm.phoneUser.apply {
            val maskedListener = MaskedTextChangedListener(context.getString(R.string.com_crafttalk_chat_russian_phone_format), this)
            addTextChangedListener(maskedListener)
            onFocusChangeListener = maskedListener
        }
        binding.authForm.signIn.setOnClickListener(this)
        binding.chatPlace.sendMessage.setOnClickListener(this)
        binding.chatPlace.voiceInput.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                delayOnLifecycle(ChatAttr.getInstance().delayVoiceInputPostRecording) {
                    speechRecognizer?.stopListening()
                }
            }
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                binding.chatPlace.voiceInput.setImageDrawable(ChatAttr.getInstance().drawableVoiceInputMicOn)
                speechRecognizerIntent?.let { intent ->
                    speechRecognizer?.startListening(intent)
                }
            }
            true
        }
        binding.warning.warningRefresh.setOnClickListener(this)
        binding.chatPlace.scrollToDown.setOnClickListener(this)
        binding.chatPlace.entryField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if ((s ?: "").isEmpty()) {
                    binding.chatPlace.sendMessage.setImageDrawable(ChatAttr.getInstance().drawableAttachFile)
                } else {
                    binding.chatPlace.sendMessage.setImageDrawable(ChatAttr.getInstance().drawableSendMessage)
                }
                if (viewModel.userTyping == true) {
                    dontSendPreviewToOperator = false
                    executorService.schedule(sendServiceMessageUserIsTypingTextTask, viewModel.userTypingInterval.toLong() + 200, TimeUnit.MILLISECONDS)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.chatPlace.listWithMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                binding.chatPlace.listWithMessage.delayOnLifecycle(DELAY_RENDERING_SCROLL_BTN) {
                    val indexLastVisible = (binding.chatPlace.listWithMessage.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: return@delayOnLifecycle

                    if (indexLastVisible == -1) {
                        viewModel.scrollToDownVisible.value = false
                        return@delayOnLifecycle
                    }
                    viewModel.scrollToDownVisible.value = (indexLastVisible > MAX_COUNT_MESSAGES_NEED_SCROLLED_BEFORE_APPEARANCE_BTN_SCROLL) ||
                            ((indexLastVisible == MAX_COUNT_MESSAGES_NEED_SCROLLED_BEFORE_APPEARANCE_BTN_SCROLL) &&
                                    adapterListMessages.currentList?.get(0) !is InfoMessageItem)
                    viewModel.readMessage(adapterListMessages.getMessageByPosition(indexLastVisible))
                }
            }
        })
        binding.chatPlace.userFeedback.closeFeedback.setOnClickListener(this)
        binding.search.setOnClickListener(this)
        binding.searchPlace.searchCancel.setOnClickListener(this)
        binding.searchPlace.searchInput.setOnTouchListener { view, motionEvent ->
            val drawableLeft = 0
            val drawableRight = 2

            if(motionEvent.action == MotionEvent.ACTION_UP) {
                if(motionEvent.x + left >= (binding.searchPlace.searchInput.right - binding.searchPlace.searchInput.compoundDrawables[drawableRight].bounds.width() - binding.searchPlace.searchInput.compoundDrawablePadding)) {
                    binding.searchPlace.searchInput.text.clear()
                } else if (motionEvent.x < (binding.searchPlace.searchInput.paddingLeft + binding.searchPlace.searchInput.compoundDrawables[drawableLeft].bounds.width())) {
                    searchText(binding.searchPlace.searchInput.text.toString())
                }
            }
            false
        }
        if (ChatAttr.getInstance().enableAutoSearch) {
            binding.searchPlace.searchInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    searchText(binding.searchPlace.searchInput.text.toString())
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        binding.chatPlace.searchSwitchPlace.searchTop.setOnClickListener(this)
        binding.chatPlace.searchSwitchPlace.searchBottom.setOnClickListener(this)
        binding.chatPlace.uploadHistoryBtn.setOnClickListener(this)
        binding.chatPlace.replyPreview.replyPreviewClose.setOnClickListener(this)
        setFeedbackListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setFeedbackListener() {
        binding.chatPlace.userFeedback.root.setOnTouchListener { view, motionEvent ->
            var countStars = ceil((motionEvent.rawX - binding.chatPlace.userFeedback.feedbackStar1.left) / (binding.chatPlace.userFeedback.feedbackStar2.left - binding.chatPlace.userFeedback.feedbackStar1.left).toDouble()).toInt()
            if (countStars < 1) countStars = 1
            if (countStars > 5) countStars = 5
            when {
                motionEvent.action == MotionEvent.ACTION_MOVE -> giveFeedback(countStars, false)
                motionEvent.action == MotionEvent.ACTION_UP && countStars > 0 -> giveFeedback(countStars, true)
            }
            true
        }
    }

    private fun setListMessages() {
        adapterListMessages = AdapterListMessages(
            downloadOrOpenDocument = viewModel::downloadOrOpenDocument,
            openImage = viewModel::openImage,
            openGif = viewModel::openGif,
            downloadFile = { fileName, fileUrl, fileType ->
                downloadResource(
                    context,
                    fileName,
                    fileUrl,
                    fileType,
                    downloadFileListener,
                    { permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit ->
                        permissionListener.requestedPermissions(
                            permissions,
                            arrayOf(context.getString(R.string.com_crafttalk_chat_requested_permission_download)),
                            actionsAfterObtainingPermission
                        )
                    },
                    { id -> downloadID = id }
                )
            },
            selectAction = viewModel::selectAction,
            selectButton = viewModel::selectButton,
            selectReplyMessage = viewModel::selectReplyMessage,
            getWidgetView = methodGetWidgetView,
            findItemsViewOnWidget = methodFindItemsViewOnWidget,
            bindWidget = methodBindWidget,
            updateData = viewModel::updateData
        ).apply {
            binding.chatPlace.listWithMessage.adapter = this
            if (ChatAttr.getInstance().replyEnable) {
                ItemTouchHelper(MessageSwipeController {
                    viewModel.replyMessage.value = it
                }).attachToRecyclerView(binding.chatPlace.listWithMessage)
            }
        }
    }

    fun onViewCreated(
        fragment: Fragment,
        lifecycleOwner: LifecycleOwner
    ):View {
        val view = binding.root
        binding.root.visibility = View.VISIBLE
        Chat.getSdkComponent().createChatComponent()
            .parentFragment(fragment)
            .build()
            .inject(this)
        this.parentFragment = fragment

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (viewModel.uploadFileListener == null) viewModel.uploadFileListener = defaultUploadFileListener
            context.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED)
        }else {
            if (viewModel.uploadFileListener == null) viewModel.uploadFileListener = defaultUploadFileListener
            context.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
        if (viewModel.uploadFileListener == null) viewModel.uploadFileListener = defaultUploadFileListener
        settingVoiceInput()

        pickPictureSafe = fragment.registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri.size > FileViewerHelper.PHOTOS_LIMIT) {
                viewModel.sendFiles(uri.slice(0 until FileViewerHelper.PHOTOS_LIMIT).map { File(it, TypeFile.IMAGE) })
                FileViewerHelper.showFileLimitExceededMessage(fragment, FileViewerHelper.PHOTOS_LIMIT_EXCEEDED)
            } else {
                viewModel.sendFiles(uri.map { File(it, TypeFile.IMAGE)})
            }
        }

        pickFileSafe = fragment.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                Log.d("DocumentPicker", "Selected URI: $uri")
                uri?.let { viewModel.sendFile(File(it, TypeFile.FILE))}
            } else {
                Log.d("DocumentPicker", "No document selected")
            }

        }


        takePicture = fragment.registerForActivityResult(TakePicture()) { uri ->
            uri?.let { viewModel.sendFile(File(it, TypeFile.IMAGE)) }
        }
        pickImage = fragment.registerForActivityResult(PickFileContract()) { listUri ->
            if (listUri.size > FileViewerHelper.PHOTOS_LIMIT) {
                viewModel.sendFiles(listUri.slice(0 until FileViewerHelper.PHOTOS_LIMIT).map { File(it, TypeFile.IMAGE) })
                FileViewerHelper.showFileLimitExceededMessage(fragment, FileViewerHelper.PHOTOS_LIMIT_EXCEEDED)
            } else viewModel.sendFiles(listUri.map { File(it, TypeFile.IMAGE) })
        }
        pickFile = fragment.registerForActivityResult(PickFileContract()) { listUri ->
            if (listUri.size > FileViewerHelper.DOCUMENTS_LIMIT) {
                viewModel.sendFiles(listUri.slice(0 until FileViewerHelper.DOCUMENTS_LIMIT).map { File(it, TypeFile.FILE) })
                FileViewerHelper.showFileLimitExceededMessage(fragment, FileViewerHelper.DOCUMENTS_LIMIT_EXCEEDED)
            } else viewModel.sendFiles(listUri.map { File(it, TypeFile.FILE) })
        }
        //сомнительный код, условие всегда ложно
        if (viewModel.chatIsClosed)
        {
            binding.chatOffMessage.visibility = View.VISIBLE
            try {
                binding.chatOffMessage.text = viewModel.chatClosedMessage
            }
            catch (e: Exception){
                Log.e("",e.toString())
            }
            binding.chatPlace.entryField.inputType = 0
            binding.chatPlace.entryField.hint = resources.getString(R.string.com_crafttalk_chat_entry_field_hint_chat_off)
        }

        setAllListeners()
        setListMessages()

        viewModel.internetConnectionState.observe(lifecycleOwner) { state ->
            when (state) {
                InternetConnectionState.NO_INTERNET -> {
                    if (ChatAttr.getInstance().showChatState) {
                        binding.infoChatState.visibility = View.INVISIBLE
                    }
                    if (ChatAttr.getInstance().showInternetConnectionState) {
                        binding.warningConnection.visibility = if (binding.search.isVisible) View.GONE else View.VISIBLE //TODO может вызывать проблемы с работой поиска
                    }
                    binding.authForm.signIn.isClickable = true
                }
                InternetConnectionState.HAS_INTERNET, InternetConnectionState.RECONNECT -> {
                    if (ChatAttr.getInstance().showInternetConnectionState) {
                        binding.warningConnection.visibility = View.INVISIBLE
                    }
                }
            }
        }
        viewModel.displayableUIObject.observe(lifecycleOwner) {
            Log.d("CTALK_CHAT_VIEW", "displayableUIObject - ${it};")
            when (it) {
                DisplayableUIObject.CHATCLOSED -> {
                    binding.chatOffMessage.visibility = View.VISIBLE
                    try {
                        binding.chatOffMessage.text = viewModel.chatClosedMessage
                    }
                    catch (e: Exception){
                        Log.e("",e.toString())
                    }
                    binding.chatPlace.entryField.inputType = 0
                    binding.chatPlace.entryField.hint = resources.getString(R.string.com_crafttalk_chat_entry_field_hint_chat_off)
                }

                DisplayableUIObject.NOTHING -> {
                    binding.chatPlace.root.visibility = View.GONE
                    binding.searchPlace.root.visibility = View.GONE
                    binding.search.visibility = when (binding.search.visibility) {
                        View.GONE -> View.GONE
                        else -> View.INVISIBLE
                    }
                    binding.authForm.root.visibility = View.GONE
                    binding.warning.root.visibility = View.GONE
                    if (ChatAttr.getInstance().showStartingProgress) {
                        startProgressBar(binding.loading)
                    }
                    stateStartingProgressListener?.start()
                }
                DisplayableUIObject.SYNCHRONIZATION -> {
                    binding.authForm.root.visibility = View.GONE
                    binding.warning.root.visibility = View.GONE
                    binding.searchPlace.root.visibility = View.GONE
                    binding.chatPlace.root.visibility = View.VISIBLE
                    binding.search.visibility = when {
                        binding.search.visibility == View.GONE -> View.GONE
                        ChatParams.enableSearch == true -> View.VISIBLE
                        else -> View.INVISIBLE
                    }
                    if (ChatAttr.getInstance().showStartingProgress) {
                        stopProgressBar(binding.loading)
                    }
                    stateStartingProgressListener?.stop()
                    if (ChatAttr.getInstance().showChatState) {
                        binding.infoChatState.visibility = if (binding.searchPlace.root.isVisible) View.GONE else View.VISIBLE
                    }
                }
                DisplayableUIObject.CHAT -> {
                    binding.authForm.root.visibility = View.GONE
                    binding.warning.root.visibility = View.GONE
                    binding.searchPlace.root.visibility = View.GONE
                    binding.chatPlace.root.visibility = View.VISIBLE
                    binding.search.visibility = when {
                        binding.search.visibility == View.GONE -> View.GONE
                        ChatParams.enableSearch == true -> View.VISIBLE
                        else -> View.INVISIBLE
                    }
                    if (ChatAttr.getInstance().showStartingProgress) {
                        stopProgressBar(binding.loading)
                    }
                    stateStartingProgressListener?.stop()
                    if (ChatAttr.getInstance().showChatState) {
                        binding.infoChatState.visibility = View.INVISIBLE
                    }
                }
                DisplayableUIObject.FORM_AUTH -> {
                    binding.chatPlace.root.visibility = View.GONE
                    binding.searchPlace.root.visibility = View.GONE
                    binding.search.visibility = when (binding.search.rootView.visibility) {
                        View.GONE -> View.GONE
                        else -> View.INVISIBLE
                    }
                    binding.warning.root.visibility = View.GONE
                    binding.authForm.root.visibility = View.VISIBLE
                    if (ChatAttr.getInstance().showStartingProgress) {
                        stopProgressBar(binding.loading)
                    }
                    stateStartingProgressListener?.stop()
                }
                DisplayableUIObject.WARNING -> {
                    binding.chatPlace.root.visibility = View.GONE
                    binding.searchPlace.root.visibility = View.GONE
                    binding.search.visibility = when (binding.search.rootView.visibility) {
                        View.GONE -> View.GONE
                        else -> View.INVISIBLE
                    }
                    binding.authForm.root.visibility = View.GONE
                    binding.warning.root.visibility = View.VISIBLE
                    binding.warning.warningRefresh.visibility = View.VISIBLE
                    stopProgressBar(binding.warning.warningLoading)
                    if (ChatAttr.getInstance().showStartingProgress) {
                        stopProgressBar(binding.loading)
                    }
                    stateStartingProgressListener?.stop()
                    if (ChatAttr.getInstance().showChatState) {
                        binding.infoChatState.visibility = View.INVISIBLE
                    }
                }
                DisplayableUIObject.OPERATOR_START_WRITE_MESSAGE -> {
                    binding.chatPlace.stateActionOperator.visibility = View.VISIBLE
                }
                DisplayableUIObject.OPERATOR_STOP_WRITE_MESSAGE -> {
                    binding.chatPlace.stateActionOperator.visibility = View.GONE
                }
                DisplayableUIObject.CLOSE_FEEDBACK_CONTAINER -> {
                    viewModel.feedbackContainerVisible.value = false
                }
            }
        }

        viewModel.countUnreadMessages.observe(lifecycleOwner) {
            if (it <= 0) {
                binding.chatPlace.countUnreadMessage.visibility = View.GONE
            } else {
                binding.chatPlace.countUnreadMessage.text = if (it < 10) it.toString() else "9+"
                binding.chatPlace.countUnreadMessage.visibility = if (binding.chatPlace.scrollToDown.visibility == View.GONE) View.GONE else View.VISIBLE
            }
        }
        viewModel.scrollToDownVisible.observe(lifecycleOwner) {
            if (it) {
                binding.chatPlace.scrollToDown.visibility = View.VISIBLE
                if (viewModel.countUnreadMessages.value != null && viewModel.countUnreadMessages.value != 0) {
                    binding.chatPlace.countUnreadMessage.visibility = View.VISIBLE
                } else {
                    binding.chatPlace.countUnreadMessage.visibility = View.GONE
                }
            } else {
                binding.chatPlace.countUnreadMessage.visibility = View.GONE
                binding.chatPlace.scrollToDown.visibility = View.GONE
            }
        }
        viewModel.showSearchNavigate.observe(lifecycleOwner) {
            binding.chatPlace.searchSwitchPlace.searchTop.visibility = if (it) View.VISIBLE else View.GONE
            binding.chatPlace.searchSwitchPlace.searchBottom.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.enabledSearchTop.observe(lifecycleOwner) {
            binding.chatPlace.searchSwitchPlace.searchTop.isEnabled = it
            if (it) {
                binding.chatPlace.searchSwitchPlace.searchTop.setColorFilter(ContextCompat.getColor(context, R.color.com_crafttalk_chat_black))
            } else {
                binding.chatPlace.searchSwitchPlace.searchTop.setColorFilter(ContextCompat.getColor(context, R.color.com_crafttalk_chat_gray_bdbdbd))
            }
        }
        viewModel.enabledSearchBottom.observe(lifecycleOwner) {
            binding.chatPlace.searchSwitchPlace.searchBottom.isEnabled = it
            if (it) {
                binding.chatPlace.searchSwitchPlace.searchBottom.setColorFilter(ContextCompat.getColor(context, R.color.com_crafttalk_chat_black))
            } else {
                binding.chatPlace.searchSwitchPlace.searchBottom.setColorFilter(ContextCompat.getColor(context, R.color.com_crafttalk_chat_gray_bdbdbd))
            }
        }
        viewModel.searchCoincidenceText.observe(lifecycleOwner) {
            binding.chatPlace.searchSwitchPlace.root.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
            binding.chatPlace.searchSwitchPlace.searchCoincidence.text = it
            searchListener.stop()
        }
        viewModel.searchScrollToPosition.observe(lifecycleOwner) { searchItem ->
            searchLiveDataMessages?.removeObservers(lifecycleOwner)
            val searchText = viewModel.searchText ?: return@observe
            searchLiveDataMessages = viewModel.uploadSearchMessages(searchText, searchItem)
            searchLiveDataMessages?.observe(lifecycleOwner, { pagedList ->
                adapterListMessages.submitList(pagedList!!) {
                    if (searchItem != null && searchItem != searchItemLast) {
                        val position = searchItem.scrollPosition ?: return@submitList
                        delayOnLifecycle(300) {
                            scroll(position + 1, true)
                        }
                    }
                    searchItemLast = searchItem
                    viewModel.updateCountUnreadMessages()
                }
            })
        }
        viewModel.feedbackContainerVisible.observe(lifecycleOwner) {
            binding.chatPlace.userFeedback.root.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        viewModel.openDocument.observe(lifecycleOwner) {
            it ?: return@observe
            val (file, isSuccess) = it
            if (!isSuccess) {
                downloadFileListener.failDownload(context.getString(R.string.com_crafttalk_chat_download_file_fail))
                return@observe
            }
            file ?: return@observe
            viewModel.openDocument.value = null

            val uri: Uri = fileViewerHelper.getUriForFile(context, file)
            val documentIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, fileViewerHelper.getMimeType(context, uri))
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            try {
                val intentChooser = Intent.createChooser(documentIntent, context.getString(R.string.com_crafttalk_chat_string_chooser_open_file_action_view))
                context.startActivity(intentChooser)
            } catch (ex: ActivityNotFoundException) {
                downloadFileListener.failDownload(context.getString(R.string.com_crafttalk_chat_open_file_fail))
            }
        }
        viewModel.mergeHistoryBtnVisible.observe(lifecycleOwner) {
            if (it) {
                binding.chatPlace.uploadHistoryBtn.visibility = View.VISIBLE
            } else {
                binding.chatPlace.uploadHistoryBtn.visibility = View.GONE
            }
        }
        viewModel.mergeHistoryProgressVisible.observe(lifecycleOwner) {
            if (it) {
                startProgressBar(binding.chatPlace.uploadHistoryLoading)
            } else {
                stopProgressBar(binding.chatPlace.uploadHistoryLoading)
            }
        }

        viewModel.uploadMessagesForUser.observe(lifecycleOwner) { liveDataPagedList ->
            liveDataPagedList ?: return@observe
            liveDataMessages?.removeObservers(lifecycleOwner)
            liveDataMessages = liveDataPagedList
            isFirstUploadMessages = true
            liveDataMessages?.observe(lifecycleOwner, { pagedList ->
                pagedList ?: return@observe
                if (viewModel.searchText != null && viewModel.searchScrollToPosition.value != null) return@observe

                val countItemsLastVersion = adapterListMessages.itemCount
                adapterListMessages.submitList(pagedList) {
                    if (isFirstUploadMessages) {
                        viewModel.initialLoadKey.run(binding.chatPlace.listWithMessage::scrollToPosition)
                        viewModel.updateCountUnreadMessages()
                    } else {
                        val indexLastVisible = (binding.chatPlace.listWithMessage.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                        if (
                            indexLastVisible != null &&
                            indexLastVisible != -1 &&
                            indexLastVisible < MAX_COUNT_MESSAGES_NEED_SCROLLED_BEFORE_APPEARANCE_BTN_SCROLL &&
                            countItemsLastVersion != pagedList.size
                        ) {
                            viewModel.updateCountUnreadMessages(pagedList.getOrNull(0)?.timestamp) { countUnreadMessages ->
                                delayOnLifecycle(300) {
                                    if (viewModel.searchText == null) {
                                        scroll(countUnreadMessages)
                                    }
                                }
                            }
                        } else {
                            viewModel.updateCountUnreadMessages()
                        }
                    }
                    isFirstUploadMessages = false
                }
            })
        }
        viewModel.replyMessage.observe(lifecycleOwner) {
            if (it == null) {
                binding.chatPlace.replyPreview.root.visibility = View.GONE
                return@observe
            }
            binding.chatPlace.replyPreview.root.visibility = View.VISIBLE

            val replyPreviewBarrier = binding.chatPlace.replyPreview.root.findViewById<View>(R.id.replied_barrier)
            val replyPreviewMessage = binding.chatPlace.replyPreview.root.findViewById<TextView>(R.id.replied_message)
            val replyPreviewFileInfo = binding.chatPlace.replyPreview.root.findViewById<ViewGroup>(R.id.replied_file_info)
            val replyPreviewFileIcon = binding.chatPlace.replyPreview.root.findViewById<ImageView>(R.id.file_icon)
            val replyPreviewProgressDownload = binding.chatPlace.replyPreview.root.findViewById<ProgressBar>(R.id.progress_download)
            val replyPreviewFileName = binding.chatPlace.replyPreview.root.findViewById<TextView>(R.id.file_name)
            val replyPreviewFileSize = binding.chatPlace.replyPreview.root.findViewById<TextView>(R.id.file_size)
            val replyPreviewMediaFile = binding.chatPlace.replyPreview.root.findViewById<ImageView>(R.id.replied_media_file)
            val replyPreviewMediaFileWarning = binding.chatPlace.replyPreview.root.findViewById<ViewGroup>(R.id.replied_media_file_warning)

            replyPreviewBarrier?.setBackgroundColor(ChatAttr.getInstance().colorMain)
            when (it) {
                is TextMessageItem, is UnionMessageItem -> {
                    replyPreviewMessage.visibility = View.VISIBLE
                    replyPreviewFileInfo.visibility = View.GONE
                    replyPreviewMediaFile.visibility = View.GONE
                    replyPreviewMediaFileWarning.visibility = View.GONE
                    replyPreviewMessage.setMessageText(
                        textMessage = (it as? TextMessageItem)?.message ?: (it as? UnionMessageItem)?.message,
                        maxWidthTextMessage = (ChatAttr.getInstance().widthScreenInPx * 0.8f).toInt(),
                        colorTextMessage = ContextCompat.getColor(context, R.color.com_crafttalk_chat_gray_707070),
                        sizeTextMessage = ChatAttr.getInstance().sizeTextUserRepliedMessage,
                        resFontFamilyMessage = ChatAttr.getInstance().resFontFamilyUserMessage
                    )
                }
                is ImageMessageItem, is GifMessageItem -> {
                    replyPreviewMessage.visibility = GONE
                    replyPreviewFileInfo.visibility = GONE
                    replyPreviewMediaFile.apply {
                        visibility = VISIBLE
                        settingMediaFile(true)
                        loadMediaFile(
                            id = it.id,
                            mediaFile = (it as? ImageMessageItem)?.image ?: (it as? GifMessageItem)?.gif,
                            updateData = viewModel::updateData,
                            isUserMessage = true,
                            isUnionMessage = true,
                            warningContainer = replyPreviewMediaFileWarning,
                            maxHeight = 200,
                            maxWidth = 200
                        )
                    }
                }
                is FileMessageItem -> {
                    replyPreviewMessage.visibility = View.GONE
                    replyPreviewProgressDownload.visibility = View.INVISIBLE
                    replyPreviewMediaFile.visibility = View.GONE
                    replyPreviewMediaFileWarning.visibility = View.GONE
                    replyPreviewFileInfo.visibility = View.VISIBLE
                    replyPreviewFileIcon.setFileIcon(it.document.typeDownloadProgress)
                    replyPreviewFileIcon.setColorFilter(ContextCompat.getColor(context, R.color.com_crafttalk_chat_gray_707070), PorterDuff.Mode.SRC_IN)
                    replyPreviewFileName.setFileName(
                        file = it.document,
                        maxWidthTextFileName = (ChatAttr.getInstance().widthScreenInPx * 0.6f).toInt(),
                        colorTextFileName = ContextCompat.getColor(context, R.color.com_crafttalk_chat_gray_707070),
                        sizeTextFileName = ChatAttr.getInstance().sizeUserRepliedFileName
                    )
                    replyPreviewFileSize.setFileSize(
                        file = it.document,
                        maxWidthTextFileSize = (ChatAttr.getInstance().widthScreenInPx * 0.6f).toInt(),
                        colorTextFileSize = ContextCompat.getColor(context, R.color.com_crafttalk_chat_gray_707070),
                        sizeTextFileSize = ChatAttr.getInstance().sizeUserRepliedFileSize
                    )
                }

                is DefaultMessageItem -> {Unit}
                is InfoMessageItem -> {Unit}
                is TransferMessageItem -> {Unit}
                is WidgetMessageItem -> {Unit}
            }
        }
        viewModel.replyMessagePosition.observe(lifecycleOwner) {
            it ?: return@observe
            smoothScroller.targetPosition = adapterListMessages.itemCount - it
            binding.chatPlace.listWithMessage.layoutManager?.startSmoothScroll(smoothScroller)
        }
        return view
    }

    private fun settingVoiceInput() {
        speechRecognizer = createSpeechRecognizer(context)

        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            var idLastWarning: Int? = null
            var timestampLastWarning: Long? = null

            override fun onReadyForSpeech(bundle: Bundle) {
                binding.chatPlace.entryField.text.clear()
                binding.chatPlace.entryField.hint = resources.getString(R.string.com_crafttalk_chat_voice_input_entry_field_hint)
                binding.chatPlace.entryField.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }

            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}

            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}

            override fun onError(i: Int) {
                if (
                    idLastWarning != null &&
                    timestampLastWarning != null &&
                    idLastWarning == i &&
                    System.currentTimeMillis() - (timestampLastWarning ?: 0L) <= ChatAttr.getInstance().delayVoiceInputBetweenRecurringWarnings
                ) {
                    idLastWarning = i
                    timestampLastWarning = System.currentTimeMillis()
                } else {
                    val warningTitle = when (i) {
                        ERROR_AUDIO, ERROR_CLIENT, ERROR_NO_MATCH -> {
                            if (idLastWarning in listOf(ERROR_AUDIO, ERROR_CLIENT, ERROR_NO_MATCH))
                                resources.getString(R.string.com_crafttalk_chat_voice_input_instruction_warning_title)
                            else
                                resources.getString(R.string.com_crafttalk_chat_voice_input_failed_attempt_warning_title)
                        }
                        ERROR_NETWORK, ERROR_NETWORK_TIMEOUT -> resources.getString(R.string.com_crafttalk_chat_voice_input_network_warning_title)
                        ERROR_RECOGNIZER_BUSY, ERROR_SERVER -> resources.getString(R.string.com_crafttalk_chat_voice_input_service_warning_title)
                        ERROR_SPEECH_TIMEOUT -> resources.getString(R.string.com_crafttalk_chat_voice_input_instruction_warning_title)
                        ERROR_INSUFFICIENT_PERMISSIONS -> {
                            requestPermissionWithAction(
                                context = context,
                                permissions = arrayOf(Manifest.permission.RECORD_AUDIO),
                                noPermission = { permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit ->
                                    permissionListener.requestedPermissions(
                                        permissions,
                                        arrayOf(context.getString(R.string.com_crafttalk_chat_voice_input_permission_warning_title)),
                                        actionsAfterObtainingPermission
                                    )
                                }
                            )
                            null
                        }
                        else -> resources.getString(R.string.com_crafttalk_chat_voice_input_failed_attempt_warning_title)
                    }
                    idLastWarning = i
                    timestampLastWarning = System.currentTimeMillis()

                    warningTitle?.let { title ->
                        WarningSnackbar.make(
                            view = binding.chatPlace.entryField,
                            parentViewGroup = binding.chatPlace.warningInputContainerCl,
                            title = title
                        )?.show()
                    }
                }
                binding.chatPlace.entryField.text.clear()
                binding.chatPlace.entryField.hint = resources.getString(R.string.com_crafttalk_chat_entry_field_hint)
                binding.chatPlace.voiceInput.setImageDrawable(ChatAttr.getInstance().drawableVoiceInputMicOff)
            }
            override fun onResults(bundle: Bundle) {
                idLastWarning = null

                bundle.getStringArrayList(RESULTS_RECOGNITION)?.get(0)?.let { data ->
                    binding.chatPlace.entryField.setText(data)
                    binding.chatPlace.entryField.setSelection(data.length)
                }
                binding.chatPlace.entryField.hint = resources.getString(R.string.com_crafttalk_chat_entry_field_hint)
                binding.chatPlace.voiceInput.setImageDrawable(ChatAttr.getInstance().drawableVoiceInputMicOff)
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })

    }

    fun onResume(visitor: Visitor? = null) {
        viewModel.onStartChatView(visitor)
    }

    fun onStop() {
        viewModel.onStop()
    }

    fun onDestroyView() {
        speechRecognizer?.destroy()
    }

    private fun checkerObligatoryFields(fields: List<EditText>): Boolean {
        var result = true
        fields.forEach{
            if (it.text.trim().isEmpty()) {
                it.setBackgroundResource(R.drawable.com_crafttalk_chat_background_error_field_auth_form)
                result = false
            } else {
                it.setBackgroundResource(R.drawable.com_crafttalk_chat_background_normal_field_auth_form)
            }
        }
        return result
    }

    private fun startProgressBar(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
    }

    private fun stopProgressBar(progressBar: ProgressBar) {
        progressBar.visibility = View.GONE
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.upload_history_btn -> {
                mergeHistory()
            }
            R.id.sign_in -> {
                if (checkerObligatoryFields(listOf(binding.authForm.firstNameUser, binding.authForm.lastNameUser, binding.authForm.phoneUser))) {
                    hideSoftKeyboard(this)
                    if (ChatAttr.getInstance().showStartingProgress) {
                        startProgressBar(binding.loading)
                    }
                    stateStartingProgressListener?.start()
                    val firstName = binding.authForm.firstNameUser.text.toString()
                    val lastName = binding.authForm.lastNameUser.text.toString()
                    val phone = binding.authForm.phoneUser.text.toString()
                    viewModel.registration(firstName, lastName, phone)
                    binding.authForm.signIn.isClickable = false
                }
            }
            R.id.send_message -> {
                val message = binding.chatPlace.entryField.text.toString()
                when {
                    message.trim().isNotEmpty() -> {
                        hideSoftKeyboard(this)
                        if (viewModel.searchText == null) scroll(0)
                        viewModel.sendMessage(message, viewModel.replyMessage.value?.id)
                        viewModel.replyMessage.value = null
                        binding.chatPlace.entryField.text.clear()
                        executorService.schedule(userStopTypingTask, 200, TimeUnit.MILLISECONDS)
                        dontSendPreviewToOperator = true
                    }
                    message.isEmpty() -> {
                        binding.chatPlace.sendMessage.isClickable = false
                        BottomSheetFileViewer.Builder()
                            .add(R.menu.com_crafttalk_chat_options)
                            .setListener(this)
                            .show(parentFragment.parentFragmentManager)
                    }
                    else -> {
                        viewModel.replyMessage.value = null
                        hideSoftKeyboard(this)
                        binding.chatPlace.entryField.text.clear()
                        executorService.schedule(userStopTypingTask, 200, TimeUnit.MILLISECONDS)
                        dontSendPreviewToOperator = true
                    }
                }
            }
            R.id.reply_preview_close -> {
                viewModel.replyMessage.value = null
            }
            R.id.warning_refresh -> {
                startProgressBar(binding.warning.warningLoading)
                binding.warning.warningRefresh.visibility = View.INVISIBLE
                viewModel.reload()
            }
            R.id.scroll_to_down -> {
                val countUnreadMessages = viewModel.countUnreadMessages.value ?: return
                scroll(countUnreadMessages)
            }
            R.id.close_feedback -> {
                viewModel.feedbackContainerVisible.value = false
                binding.chatPlace.userFeedback.feedbackStar1.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
                binding.chatPlace.userFeedback.feedbackStar2.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
                binding.chatPlace.userFeedback.feedbackStar3.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
                binding.chatPlace.userFeedback.feedbackStar4.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
                binding.chatPlace.userFeedback.feedbackStar5.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
            }
            R.id.search -> {
                binding.chatOffMessage.visibility = View.GONE
                binding.warningConnection.visibility = View.GONE
                binding.infoChatState.visibility = View.GONE
                binding.search.visibility = View.GONE
                binding.searchPlace.root.visibility = View.VISIBLE
                hideSoftKeyboard(this)
            }
            R.id.search_cancel -> {
                if (viewModel.chatIsClosed) {binding.chatOffMessage.visibility = View.VISIBLE}
                binding.searchPlace.root.visibility = View.GONE
                binding.warningConnection.visibility = View.GONE
                binding.infoChatState.visibility = View.INVISIBLE
                binding.search.visibility = View.VISIBLE
                onSearchCancelClick()
            }
            R.id.search_top -> viewModel.onSearchTopClick()
            R.id.search_bottom -> viewModel.onSearchBottomClick()
        }
    }

    private fun scroll(countUnreadMessages: Int, isSearchScroll: Boolean = false) {
        Log.d("CTALK_SEARCH_LOG", "scroll countUnreadMessages: ${countUnreadMessages}; isSearchScroll: $isSearchScroll;")
        fun scrollToDesiredPosition(position: Int, actionScroll: (position: Int) -> Unit) {
            if (adapterListMessages.currentList?.getOrNull(position) == null) {
                binding.chatPlace.listWithMessage.smoothScrollToPosition(position)
            } else {
                actionScroll(position)
            }
        }
        val isExist = (adapterListMessages.currentList ?: return).getOrNull(countUnreadMessages) != null
        val position = when {
            countUnreadMessages <= 0 -> 0
            else -> countUnreadMessages - 1
        }
        val indexLastVisible = (binding.chatPlace.listWithMessage.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: return

        when {
            isSearchScroll -> scrollToDesiredPosition(position, binding.chatPlace.listWithMessage::scrollToPosition)
            isExist && indexLastVisible >= 20 -> scrollToDesiredPosition(position, binding.chatPlace.listWithMessage::scrollToPosition)
            isExist && indexLastVisible < 20 -> scrollToDesiredPosition(position, binding.chatPlace.listWithMessage::smoothScrollToPosition)
            !isExist -> scrollToDesiredPosition(position, binding.chatPlace.listWithMessage::scrollToPosition)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun giveFeedback(countStars: Int, isLastDecision: Boolean) {
        val userFeedback = binding.chatPlace.userFeedback
        val stars = listOf(
            userFeedback.feedbackStar1,
            userFeedback.feedbackStar2,
            userFeedback.feedbackStar3,
            userFeedback.feedbackStar4,
            userFeedback.feedbackStar5
        )

        val filledStar = R.drawable.com_crafttalk_chat_ic_star
        val outlineStar = R.drawable.com_crafttalk_chat_ic_star_outline

        stars.forEachIndexed { index, imageView ->
            imageView.setImageResource(if (index < countStars) filledStar else outlineStar)
        }

        if (isLastDecision) {
            binding.chatPlace.userFeedback.root.setOnTouchListener(null)
            viewModel.giveFeedbackOnOperator(countStars,null, viewModel.dialogID1)
            binding.chatPlace.userFeedback.root.delayOnLifecycle(ChatAttr.getInstance().delayFeedbackScreenAppears) {
                viewModel.feedbackContainerVisible.value = false
                stars.forEachIndexed { index, imageView ->
                    imageView.setImageResource(outlineStar)
                }
                setFeedbackListener()
            }
        }
    }

    fun searchText(searchText: String) {
        viewModel.onSearchClick(searchText, searchListener::start)
    }

    fun onSearchCancelClick() {
        binding.chatPlace.searchSwitchPlace.root.visibility = View.GONE
        binding.searchPlace.searchInput.text.clear()
        scroll(0)
        hideSoftKeyboard(this)
        searchItemLast = null
        viewModel.onSearchCancel()
    }

    override fun onModalOptionSelected(tag: String?, option: Option) {
        when (option.id) {
            R.id.document -> {
                pickFileSafe?.launch(arrayOf(TypeFile.FILE.value))
                Log.d("","")
            }
            R.id.image -> {
                pickPictureSafe?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            R.id.camera -> {
                fileViewerHelper.pickImageFromCamera(
                    takePicture,
                    { permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit ->
                        permissionListener.requestedPermissions(
                            permissions,
                            arrayOf(context.getString(R.string.com_crafttalk_chat_requested_permission_camera)),
                            actionsAfterObtainingPermission
                        )
                    },
                    parentFragment
                )
            }
        }
    }

    override fun onCloseBottomSheet() {
        binding.chatPlace.sendMessage.isClickable = true
    }
}