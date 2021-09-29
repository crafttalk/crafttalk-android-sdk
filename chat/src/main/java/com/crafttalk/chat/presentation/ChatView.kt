package com.crafttalk.chat.presentation

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.InternetConnectionState
import com.crafttalk.chat.initialization.Chat
import com.crafttalk.chat.presentation.ChatViewModel.Companion.DELAY_RENDERING_SCROLL_BTN
import com.crafttalk.chat.presentation.ChatViewModel.Companion.MAX_COUNT_MESSAGES_NEED_SCROLLED_BEFORE_APPEARANCE_BTN_SCROLL
import com.crafttalk.chat.presentation.adapters.AdapterListMessages
import com.crafttalk.chat.presentation.custom_views.custom_snackbar.WarningSnackbar
import com.crafttalk.chat.presentation.feature.file_viewer.BottomSheetFileViewer
import com.crafttalk.chat.presentation.feature.file_viewer.Option
import com.crafttalk.chat.presentation.feature.view_picture.ShowImageDialog
import com.crafttalk.chat.presentation.helper.downloaders.downloadResource
import com.crafttalk.chat.presentation.helper.file_viewer_helper.FileViewerHelper
import com.crafttalk.chat.presentation.helper.file_viewer_helper.gellery.PickFileContract
import com.crafttalk.chat.presentation.helper.file_viewer_helper.gellery.TakePicture
import com.crafttalk.chat.presentation.helper.ui.hideSoftKeyboard
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.presentation.model.TypeMultiple
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.TypeFailUpload
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.com_crafttalk_chat_layout_auth.view.*
import kotlinx.android.synthetic.main.com_crafttalk_chat_layout_chat.view.*
import kotlinx.android.synthetic.main.com_crafttalk_chat_layout_user_feedback.view.*
import kotlinx.android.synthetic.main.com_crafttalk_chat_layout_warning.view.*
import kotlinx.android.synthetic.main.com_crafttalk_chat_view_host.view.*
import javax.inject.Inject

class ChatView: RelativeLayout, View.OnClickListener, BottomSheetFileViewer.Listener {

    @Inject
    lateinit var viewModel: ChatViewModel
    private var liveDataMessages: LiveData<PagedList<MessageModel>>? = null
    private var isFirstUploadMessages = false
    private lateinit var adapterListMessages: AdapterListMessages
    private val fileViewerHelper = FileViewerHelper()
    private lateinit var parentFragment: Fragment
    private val inflater: LayoutInflater by lazy {
         context.getSystemService(
             Context.LAYOUT_INFLATER_SERVICE
         ) as LayoutInflater
    }
    private var permissionListener: ChatPermissionListener = object : ChatPermissionListener {
        override fun requestedPermissions(permissions: Array<String>, messages: Array<String>, action: () -> Unit) {
            permissions.forEachIndexed { index, permission ->
                WarningSnackbar.make(chat_place, null, messages[index], null)?.show()
            }
        }
    }
    private var downloadFileListener: DownloadFileListener = object : DownloadFileListener {
        override fun successDownload() {
            if (ShowImageDialog.isOpen()) {
                ShowImageDialog.showWarning(true)
            } else {
                WarningSnackbar.make(
                    chat_place,
                    null,
                    ChatAttr.getInstance().titleSuccessDownloadFileWarning,
                    null,
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
                    chat_place,
                    null,
                    ChatAttr.getInstance().titleFailDownloadFileWarning,
                    null
                )?.show()
            }
        }
        override fun failDownload(title: String) {
            if (ShowImageDialog.isOpen()) {
                ShowImageDialog.showWarning(false)
            } else {
                WarningSnackbar.make(
                    chat_place,
                    null,
                    title,
                    null
                )?.show()
            }
        }
    }
    private var stateStartingProgressListener: StateStartingProgressListener? = null
    private var downloadID: Long? = null
    private val defaultUploadFileListener: UploadFileListener by lazy {
        object : UploadFileListener {
            override fun successUpload() {}
            override fun failUpload(message: String, type: TypeFailUpload) {
                WarningSnackbar.make(chat_place, type)?.show()
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

    private fun mergeHistory() {
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
        inflater.inflate(R.layout.com_crafttalk_chat_view_host, this, true)

        val attrArr = context.obtainStyledAttributes(attrs, R.styleable.ChatView)
        customizationChat(attrArr)
        attrArr.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("ResourceType")
    private fun customizationChat(attrArr: TypedArray) {
        val chatAttr = ChatAttr.getInstance(attrArr, context)
        // set color
        send_message.setColorFilter(chatAttr.colorMain, PorterDuff.Mode.SRC_IN)
        sign_in.setBackgroundDrawable(chatAttr.drawableBackgroundSignInButton)

        warningConnection.setTextColor(chatAttr.colorTextInternetConnectionWarning)
        state_action_operator.setTextColor(chatAttr.colorTextInfo)
        company_name.setTextColor(chatAttr.colorTextInfo)
        // set dimension
        warningConnection.setTextSize(TypedValue.COMPLEX_UNIT_PX, chatAttr.sizeTextInternetConnectionWarning)
        state_action_operator.setTextSize(TypedValue.COMPLEX_UNIT_PX, chatAttr.sizeTextInfoText)
        company_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, chatAttr.sizeTextInfoText)
        // set bg
        upper_limiter.setBackgroundColor(chatAttr.colorMain)
        lower_limit.setBackgroundColor(chatAttr.colorMain)
        ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_background_count_unread_message)?.let { unwrappedDrawable ->
            val wrappedDrawable: Drawable = DrawableCompat.wrap(unwrappedDrawable)
            DrawableCompat.setTint(wrappedDrawable, chatAttr.colorMain)
            count_unread_message.background = wrappedDrawable
        }
        // set company name
        company_name.text = chatAttr.companyName
        company_name.visibility = if (chatAttr.showCompanyName) View.VISIBLE else View.GONE
        warningConnection.visibility = if (chatAttr.showInternetConnectionState) View.INVISIBLE else View.GONE
        infoChatState.visibility = if (chatAttr.showChatState) View.INVISIBLE else View.GONE
        upper_limiter.visibility = if (chatAttr.showUpperLimiter) View.VISIBLE else View.GONE
        feedback_title.apply {
            setTextColor(ChatAttr.getInstance().colorFeedbackTitle)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeFeedbackTitle)
        }
        feedback_star_1.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)
        feedback_star_2.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)
        feedback_star_3.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)
        feedback_star_4.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)
        feedback_star_5.setColorFilter(ChatAttr.getInstance().colorFeedbackStar)

        chatAttr.drawableProgressIndeterminate?.let {
            loading.indeterminateDrawable = it
            warning_loading.indeterminateDrawable = it.constantState?.newDrawable()?.mutate()
        }
        send_message.setImageDrawable(ChatAttr.getInstance().drawableAttachFile)
    }

    private fun setAllListeners() {
        phone_user.apply {
            val maskedListener = MaskedTextChangedListener(context.getString(R.string.com_crafttalk_chat_russian_phone_format), this)
            addTextChangedListener(maskedListener)
            onFocusChangeListener = maskedListener
        }
        sign_in.setOnClickListener(this)
        user_feedback.setOnClickListener(this)
        send_message.setOnClickListener(this)
        warning_refresh.setOnClickListener(this)
        scroll_to_down.setOnClickListener(this)
        entry_field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if ((s ?: "").isEmpty()) {
                    send_message.setImageDrawable(ChatAttr.getInstance().drawableAttachFile)
                } else {
                    send_message.setImageDrawable(ChatAttr.getInstance().drawableSendMessage)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        list_with_message.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                list_with_message.postDelayed({
                    val indexLastVisible = (list_with_message.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: return@postDelayed

                    if (indexLastVisible == -1) {
                        viewModel.scrollToDownVisible.value = false
                        return@postDelayed
                    }
                    viewModel.scrollToDownVisible.value = indexLastVisible >= MAX_COUNT_MESSAGES_NEED_SCROLLED_BEFORE_APPEARANCE_BTN_SCROLL
                    viewModel.readMessage(adapterListMessages.getMessageTimestampByPosition(indexLastVisible))
                }, DELAY_RENDERING_SCROLL_BTN)
            }
        })
        close_feedback.setOnClickListener(this)
        upload_history_btn.setOnClickListener(this)
        setFeedbackListeners()
    }

    private fun setFeedbackListeners() {
        feedback_star_1.setOnClickListener(this)
        feedback_star_2.setOnClickListener(this)
        feedback_star_3.setOnClickListener(this)
        feedback_star_4.setOnClickListener(this)
        feedback_star_5.setOnClickListener(this)
    }

    private fun removeFeedbackListeners() {
        feedback_star_1.setOnClickListener(null)
        feedback_star_2.setOnClickListener(null)
        feedback_star_3.setOnClickListener(null)
        feedback_star_4.setOnClickListener(null)
        feedback_star_5.setOnClickListener(null)
    }

    private fun setListMessages() {
        adapterListMessages = AdapterListMessages(
            viewModel::downloadOrOpenDocument,
            viewModel::openImage,
            viewModel::openGif,
            { fileName, fileUrl, fileType ->
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
            viewModel::selectAction,
            viewModel::updateData
        ).apply {
            list_with_message.adapter = this
        }
    }

    fun onViewCreated(
        fragment: Fragment,
        lifecycleOwner: LifecycleOwner
    ) {
        Chat.getSdkComponent().createChatComponent()
            .parentFragment(fragment)
            .build()
            .inject(this)
        this.parentFragment = fragment

        if (viewModel.uploadFileListener == null) viewModel.uploadFileListener = defaultUploadFileListener
        context.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

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

        setAllListeners()
        setListMessages()

        viewModel.internetConnectionState.observe(lifecycleOwner) { state ->
            when (state) {
                InternetConnectionState.NO_INTERNET -> {
                    if (ChatAttr.getInstance().showChatState) {
                        infoChatState.visibility = View.INVISIBLE
                    }
                    if (ChatAttr.getInstance().showInternetConnectionState) {
                        warningConnection.visibility = View.VISIBLE
                    }
                    sign_in.isClickable = true
                }
                InternetConnectionState.HAS_INTERNET, InternetConnectionState.RECONNECT -> {
                    if (ChatAttr.getInstance().showInternetConnectionState) {
                        warningConnection.visibility = View.INVISIBLE
                    }
                }
            }
        }
        viewModel.displayableUIObject.observe(lifecycleOwner) {
            Log.d("CHAT_VIEW", "displayableUIObject - ${it};")
            when (it) {
                DisplayableUIObject.NOTHING -> {
                    chat_place.visibility = View.GONE
                    auth_form.visibility = View.GONE
                    warning.visibility = View.GONE
                    if (ChatAttr.getInstance().showStartingProgress) {
                        startProgressBar(loading)
                    }
                    stateStartingProgressListener?.start()
                }
                DisplayableUIObject.SYNCHRONIZATION -> {
                    auth_form.visibility = View.GONE
                    warning.visibility = View.GONE
                    chat_place.visibility = View.VISIBLE
                    if (ChatAttr.getInstance().showStartingProgress) {
                        stopProgressBar(loading)
                    }
                    stateStartingProgressListener?.stop()
                    if (ChatAttr.getInstance().showChatState) {
                        infoChatState.visibility = View.VISIBLE
                    }
                }
                DisplayableUIObject.CHAT -> {
                    auth_form.visibility = View.GONE
                    warning.visibility = View.GONE
                    chat_place.visibility = View.VISIBLE
                    if (ChatAttr.getInstance().showStartingProgress) {
                        stopProgressBar(loading)
                    }
                    stateStartingProgressListener?.stop()
                    if (ChatAttr.getInstance().showChatState) {
                        infoChatState.visibility = View.INVISIBLE
                    }
                }
                DisplayableUIObject.FORM_AUTH -> {
                    chat_place.visibility = View.GONE
                    warning.visibility = View.GONE
                    auth_form.visibility = View.VISIBLE
                    if (ChatAttr.getInstance().showStartingProgress) {
                        stopProgressBar(loading)
                    }
                    stateStartingProgressListener?.stop()
                }
                DisplayableUIObject.WARNING -> {
                    chat_place.visibility = View.GONE
                    auth_form.visibility = View.GONE
                    warning.visibility = View.VISIBLE
                    warning_refresh.visibility = View.VISIBLE
                    stopProgressBar(warning_loading)
                    if (ChatAttr.getInstance().showStartingProgress) {
                        stopProgressBar(loading)
                    }
                    stateStartingProgressListener?.stop()
                    if (ChatAttr.getInstance().showChatState) {
                        infoChatState.visibility = View.INVISIBLE
                    }
                }
                DisplayableUIObject.OPERATOR_START_WRITE_MESSAGE -> {
                    state_action_operator.visibility = View.VISIBLE
                }
                DisplayableUIObject.OPERATOR_STOP_WRITE_MESSAGE -> {
                    state_action_operator.visibility = View.GONE
                }
            }
        }

        viewModel.countUnreadMessages.observe(lifecycleOwner) {
            if (it <= 0) {
                count_unread_message.visibility = View.GONE
            } else {
                count_unread_message.text = if (it < 10) it.toString() else "9+"
                count_unread_message.visibility = if (scroll_to_down.visibility == View.GONE) View.GONE else View.VISIBLE
            }
        }
        viewModel.scrollToDownVisible.observe(lifecycleOwner) {
            if (it) {
                scroll_to_down.visibility = View.VISIBLE
                if (viewModel.countUnreadMessages.value != null && viewModel.countUnreadMessages.value != 0) {
                    count_unread_message.visibility = View.VISIBLE
                } else {
                    count_unread_message.visibility = View.GONE
                }
            } else {
                count_unread_message.visibility = View.GONE
                scroll_to_down.visibility = View.GONE
            }
        }
        viewModel.feedbackContainerVisible.observe(lifecycleOwner) {
            user_feedback.visibility = if (it) {
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
                if (documentIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intentChooser)
                } else {
                    downloadFileListener.failDownload(context.getString(R.string.com_crafttalk_chat_open_file_fail))
                }
            } catch (ex: ActivityNotFoundException) {
                downloadFileListener.failDownload(context.getString(R.string.com_crafttalk_chat_open_file_fail))
            }
        }
        viewModel.mergeHistoryBtnVisible.observe(lifecycleOwner) {
            if (it) {
                upload_history_btn.visibility = View.VISIBLE
            } else {
                upload_history_btn.visibility = View.GONE
            }
        }
        viewModel.mergeHistoryProgressVisible.observe(lifecycleOwner) {
            if (it) {
                startProgressBar(upload_history_loading)
            } else {
                stopProgressBar(upload_history_loading)
            }
        }

        viewModel.uploadMessagesForUser.observe(lifecycleOwner) { liveDataPagedList ->
            liveDataPagedList ?: return@observe
            liveDataMessages?.removeObservers(lifecycleOwner)
            liveDataMessages = liveDataPagedList
            isFirstUploadMessages = true
            liveDataMessages?.observe(lifecycleOwner, { pagedList ->
                pagedList ?: return@observe

                val countItemsLastVersion = adapterListMessages.itemCount
                adapterListMessages.submitList(pagedList) {
                    if (isFirstUploadMessages) {
                        viewModel.initialLoadKey.run(list_with_message::scrollToPosition)
                        viewModel.updateCountUnreadMessages()
                    } else {
                        val indexLastVisible = (list_with_message.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                        if (
                            indexLastVisible != null &&
                            indexLastVisible != -1 &&
                            indexLastVisible < MAX_COUNT_MESSAGES_NEED_SCROLLED_BEFORE_APPEARANCE_BTN_SCROLL &&
                            countItemsLastVersion != pagedList.size
                        ) {
                            viewModel.updateCountUnreadMessages(pagedList.getOrNull(0)?.timestamp) { countUnreadMessages ->
                                scroll(countUnreadMessages)
                            }
                        } else {
                            viewModel.updateCountUnreadMessages()
                        }
                    }
                    isFirstUploadMessages = false
                }
            })
        }
    }

    fun onResume(visitor: Visitor? = null) {
        viewModel.onStartChatView(visitor)
    }

    fun onStop() {
        viewModel.onStop()
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
                if (checkerObligatoryFields(listOf(first_name_user, last_name_user, phone_user))) {
                    hideSoftKeyboard(this)
                    if (ChatAttr.getInstance().showStartingProgress) {
                        startProgressBar(loading)
                    }
                    stateStartingProgressListener?.start()
                    val firstName = first_name_user.text.toString()
                    val lastName = last_name_user.text.toString()
                    val phone = phone_user.text.toString()
                    viewModel.registration(firstName, lastName, phone)
                    sign_in.isClickable = false
                }
            }
            R.id.send_message -> {
                val message = entry_field.text.toString().trim()
                when {
                    message.isNotEmpty() -> {
                        hideSoftKeyboard(this)
                        scroll(0)
                        viewModel.sendMessage(message)
                        entry_field.text.clear()
                    }
                    entry_field.text.toString().isEmpty() -> {
                        send_message.isClickable = false
                        BottomSheetFileViewer.Builder()
                            .add(R.menu.com_crafttalk_chat_options)
                            .setListener(this)
                            .show(parentFragment.parentFragmentManager)
                    }
                    else -> {
                        hideSoftKeyboard(this)
                        entry_field.text.clear()
                    }
                }
            }
            R.id.warning_refresh -> {
                startProgressBar(warning_loading)
                warning_refresh.visibility = View.INVISIBLE
                viewModel.reload()
            }
            R.id.scroll_to_down -> {
                val countUnreadMessages = viewModel.countUnreadMessages.value ?: return
                scroll(countUnreadMessages)
            }
            R.id.close_feedback -> {
                viewModel.feedbackContainerVisible.value = false
                feedback_star_1.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
                feedback_star_2.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
                feedback_star_3.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
                feedback_star_4.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
                feedback_star_5.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
            }
            R.id.feedback_star_1 -> giveFeedback(1)
            R.id.feedback_star_2 -> giveFeedback(2)
            R.id.feedback_star_3 -> giveFeedback(3)
            R.id.feedback_star_4 -> giveFeedback(4)
            R.id.feedback_star_5 -> giveFeedback(5)
        }
    }

    private fun scroll(countUnreadMessages: Int) {
        fun scrollToDesiredPosition(position: Int, actionScroll: (position: Int) -> Unit) {
            if (adapterListMessages.currentList?.getOrNull(position) == null) {
                list_with_message.smoothScrollToPosition(position)
            } else {
                actionScroll(position)
            }
        }
        val isExist = (adapterListMessages.currentList ?: return).getOrNull(countUnreadMessages) != null
        val position = when {
            countUnreadMessages <= 0 -> 0
            else -> countUnreadMessages - 1
        }
        val indexLastVisible = (list_with_message.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: return

        when {
            isExist && indexLastVisible >= 20 -> scrollToDesiredPosition(position, list_with_message::scrollToPosition)
            isExist && indexLastVisible < 20 -> scrollToDesiredPosition(position, list_with_message::smoothScrollToPosition)
            !isExist -> scrollToDesiredPosition(position, list_with_message::scrollToPosition)
        }
    }

    private fun giveFeedback(countStars: Int) {
        when (countStars) {
            1 -> {
                feedback_star_1.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
            }
            2 -> {
                feedback_star_1.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_2.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
            }
            3 -> {
                feedback_star_1.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_2.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_3.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
            }
            4 -> {
                feedback_star_1.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_2.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_3.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_4.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
            }
            5 -> {
                feedback_star_1.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_2.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_3.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_4.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
                feedback_star_5.setImageResource(R.drawable.com_crafttalk_chat_ic_star)
            }
        }
        removeFeedbackListeners()
        viewModel.giveFeedbackOnOperator(countStars)
        Handler().postDelayed({
            viewModel.feedbackContainerVisible.value = false
            feedback_star_1.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
            feedback_star_2.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
            feedback_star_3.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
            feedback_star_4.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
            feedback_star_5.setImageResource(R.drawable.com_crafttalk_chat_ic_star_outline)
            setFeedbackListeners()
        }, ChatAttr.getInstance().delayFeedbackScreenAppears)
    }

    override fun onModalOptionSelected(tag: String?, option: Option) {
        when (option.id) {
            R.id.document -> {
                fileViewerHelper.pickFiles(
                    pickFile,
                    Pair(TypeFile.FILE, TypeMultiple.SINGLE),
                    { permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit ->
                        permissionListener.requestedPermissions(
                            permissions,
                            arrayOf(context.getString(R.string.com_crafttalk_chat_requested_permission_storage)),
                            actionsAfterObtainingPermission
                        )
                    },
                    parentFragment
                )
            }
            R.id.image -> {
                fileViewerHelper.pickFiles(
                    pickImage,
                    Pair(TypeFile.IMAGE, TypeMultiple.SINGLE),
                    { permissions: Array<String>, actionsAfterObtainingPermission: () -> Unit ->
                        permissionListener.requestedPermissions(
                            permissions,
                            arrayOf(context.getString(R.string.com_crafttalk_chat_requested_permission_storage)),
                            actionsAfterObtainingPermission
                        )
                    },
                    parentFragment
                )
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
        send_message.isClickable = true
    }

}