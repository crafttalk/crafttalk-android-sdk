package com.crafttalk.chat.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.InternetConnectionState
import com.crafttalk.chat.initialization.Chat
import com.crafttalk.chat.presentation.adapters.AdapterListMessages
import com.crafttalk.chat.presentation.custom_views.custom_snackbar.WarningSnackbar
import com.crafttalk.chat.presentation.feature.file_viewer.BottomSheetFileViewer
import com.crafttalk.chat.presentation.feature.file_viewer.Option
import com.crafttalk.chat.presentation.helper.file_viewer_helper.FileViewerHelper
import com.crafttalk.chat.presentation.helper.permission.PermissionHelper
import com.crafttalk.chat.presentation.helper.ui.hideSoftKeyboard
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.presentation.model.TypeMultiple
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.Permission
import com.crafttalk.chat.utils.TypeFailUpload
import kotlinx.android.synthetic.main.auth_layout.view.*
import kotlinx.android.synthetic.main.chat_layout.view.*
import kotlinx.android.synthetic.main.view_host.view.*
import kotlinx.android.synthetic.main.warning_layout.view.*
import javax.inject.Inject

class ChatView: RelativeLayout, View.OnClickListener, BottomSheetFileViewer.Listener {

    @Inject
    lateinit var viewModel: ChatViewModel
    private var liveDataMessages: LiveData<PagedList<MessageModel>>? = null
    private var isFirstUploadMessages = false
    private lateinit var adapterListMessages: AdapterListMessages
    private val fileViewerHelper = FileViewerHelper(PermissionHelper())
    private lateinit var parentFragment: Fragment
    private val inflater: LayoutInflater by lazy {
         context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
    }
    private var permissionListener: ChatPermissionListener = object : ChatPermissionListener {
        override fun requestedPermissions(permissions: Array<Permission>, messages: Array<String>) {
            permissions.forEachIndexed { index, permission ->
                WarningSnackbar.make(chat_place, null, messages[index], null).show()
            }
        }
    }
    private val defaultUploadFileListener: UploadFileListener by lazy {
        object : UploadFileListener {
            override fun successUpload() {}
            override fun failUpload(message: String, type: TypeFailUpload) {
                WarningSnackbar.make(chat_place, type).show()
            }
        }
    }

    fun setOnPermissionListener(listener: ChatPermissionListener) {
        this.permissionListener = listener
    }

    fun setOnInternetConnectionListener(listener: ChatInternetConnectionListener) {
        viewModel.clientInternetConnectionListener = listener
    }

    fun setOnUploadFileListener(listener: UploadFileListener) {
        viewModel.uploadFileListener = listener
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        inflater.inflate(R.layout.view_host, this, true)

        val attrArr = context.obtainStyledAttributes(attrs, R.styleable.ChatView)
        ChatAttr.createInstance(attrArr, context)
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
        // set company name
        company_name.text = chatAttr.companyName
        company_name.visibility = if (chatAttr.showCompanyName) View.VISIBLE else View.GONE
        warningConnection.visibility = if (chatAttr.showInternetConnectionState) View.INVISIBLE else View.GONE
        upper_limiter.visibility = if (chatAttr.showUpperLimiter) View.VISIBLE else View.GONE

        chatAttr.progressIndeterminateDrawable?.let {
            loading.indeterminateDrawable = it
            warning_loading.indeterminateDrawable = it.constantState?.newDrawable()?.mutate()
        }
    }

    private fun setAllListeners() {
        sign_in.setOnClickListener(this)
        send_message.setOnClickListener(this)
        warning_refresh.setOnClickListener(this)
        scroll_to_down.setOnClickListener(this)
        entry_field.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if ((s?:"").isEmpty()) {
                    send_message.setImageResource(R.drawable.ic_attach_file)
                    send_message.rotation = 45f
                }
                else {
                    send_message.setImageResource(R.drawable.ic_send)
                    send_message.rotation = 0f
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        list_with_message.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val indexLastVisible = (list_with_message.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: return
                if (indexLastVisible >= 1) {
                    scroll_to_down.visibility = View.VISIBLE
                    if (viewModel.countUnreadMessages.value != 0) {
                        count_unread_message.visibility = View.VISIBLE
                    } else {
                        count_unread_message.visibility = View.GONE
                    }
                } else {
                    count_unread_message.visibility = View.GONE
                    scroll_to_down.visibility = View.GONE
                }

                viewModel.updateValueCountUnreadMessages(indexLastVisible)
            }
        })
    }

    private fun setListMessages() {
        adapterListMessages =
            AdapterListMessages(
                viewModel::openFile,
                viewModel::openImage,
                viewModel::openGif,
                viewModel::selectAction,
                viewModel::updateData
            ).apply {
                list_with_message.adapter = this
            }
    }

    fun onCreate(fragment: Fragment, lifecycleOwner: LifecycleOwner) {
        Chat.getSdkComponent().createChatComponent()
            .parentFragment(fragment)
            .build()
            .inject(this)
        this.parentFragment = fragment
        if (viewModel.uploadFileListener == null) viewModel.uploadFileListener = defaultUploadFileListener
        setAllListeners()
        setListMessages()

        viewModel.uploadMessagesForUser.observe(lifecycleOwner, Observer { liveDataPagedList ->
            liveDataPagedList ?: return@Observer
            liveDataMessages?.removeObservers(lifecycleOwner)
            liveDataMessages = liveDataPagedList
            isFirstUploadMessages = true
            liveDataMessages?.observe(lifecycleOwner, Observer { pagedList ->
                pagedList ?: return@Observer

                adapterListMessages.submitList(pagedList)

                if (isFirstUploadMessages) {
                    viewModel.setValueCountUnreadMessages()
                } else {
                    if (scroll_to_down.visibility == View.GONE) {
                        list_with_message.smoothScrollToPosition(0)
                    }
                }
                isFirstUploadMessages = false
            })
        })
        viewModel.firstUploadMessages.observe(lifecycleOwner, Observer {
            it ?: return@Observer
            chat_place.visibility = View.VISIBLE
            stopProgressBar(loading)
            stopProgressBar(warning_loading)

            list_with_message.scrollToPosition(it)
        })

    }

    fun onResume(lifecycleOwner: LifecycleOwner) {
        viewModel.displayableUIObject.observe(lifecycleOwner, Observer {
            Log.d("CHAT_VIEW", "displayableUIObject - ${it};")
            when (it) {
                DisplayableUIObject.NOTHING -> {
                    chat_place.visibility = View.GONE
                    auth_form.visibility = View.GONE
                    warning.visibility = View.GONE
                    if (ChatAttr.getInstance().showInternetConnectionState) {
                        warningConnection.visibility = View.INVISIBLE
                    }
                    stopProgressBar(warning_loading)
                    startProgressBar(loading)
                }
                DisplayableUIObject.CHAT -> {
                    auth_form.visibility = View.GONE
                    warning.visibility = View.GONE
                }
                DisplayableUIObject.FORM_AUTH -> {
                    chat_place.visibility = View.GONE
                    warning.visibility = View.GONE
                    auth_form.visibility = View.VISIBLE
                    stopProgressBar(loading)
                    stopProgressBar(warning_loading)
                }
                DisplayableUIObject.WARNING -> {
                    chat_place.visibility = View.GONE
                    auth_form.visibility = View.GONE
                    if (ChatAttr.getInstance().showInternetConnectionState) {
                        warningConnection.visibility = View.INVISIBLE
                    }
                    warning.visibility = View.VISIBLE
                    warning_refresh.visibility = View.VISIBLE
                    stopProgressBar(loading)
                    stopProgressBar(warning_loading)
                }
                DisplayableUIObject.OPERATOR_START_WRITE_MESSAGE -> {
                    state_action_operator.visibility = View.VISIBLE
                }
                DisplayableUIObject.OPERATOR_STOP_WRITE_MESSAGE -> {
                    state_action_operator.visibility = View.GONE
                }
            }
        })
        viewModel.internetConnectionState.observe(lifecycleOwner, Observer { state ->
            when (state) {
                InternetConnectionState.NO_INTERNET -> {
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
        })
        viewModel.countUnreadMessages.observe(lifecycleOwner, Observer {
            if (it == 0) {
                count_unread_message.visibility = View.GONE
            } else {
                count_unread_message.text = if (it < 10) it.toString() else "9+"
                count_unread_message.visibility = if (scroll_to_down.visibility == View.GONE) View.GONE else View.VISIBLE
            }
        })
    }

    private fun checkerObligatoryFields(fields: List<EditText>): Boolean {
        var result = true
        fields.forEach{
            if (it.text.trim().isEmpty()) {
                it.setBackgroundResource(R.drawable.background_error_field_auth_form)
                result = false
            } else {
                it.setBackgroundResource(R.drawable.background_normal_field_auth_form)
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
            R.id.sign_in -> {
                if (checkerObligatoryFields(listOf(first_name_user, last_name_user, phone_user))) {
                    hideSoftKeyboard(this)
                    startProgressBar(loading)
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
                        viewModel.sendMessage(message)
                        entry_field.text.clear()
                    }
                    entry_field.text.toString().isEmpty() -> {
                        send_message.isClickable = false
                        BottomSheetFileViewer.Builder()
                            .add(R.menu.options)
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
                warning_refresh.visibility = View.GONE
                viewModel.reload()
            }
            R.id.scroll_to_down -> {
                val countUnreadMessages = viewModel.countUnreadMessages.value
                val position = when {
                    countUnreadMessages == null || countUnreadMessages <= 0 -> 0
                    else -> countUnreadMessages - 1
                }
                list_with_message.smoothScrollToPosition(position)
            }
        }
    }

    override fun onModalOptionSelected(tag: String?, option: Option) {
        when (option.id) {
            R.id.document -> {
                fileViewerHelper.pickFiles(
                    Pair(TypeFile.FILE, TypeMultiple.SINGLE),
                    { viewModel.sendFiles(it.map { File(it, TypeFile.FILE) }) },
                    {
                        permissionListener.requestedPermissions(
                            arrayOf(Permission.STORAGE),
                            arrayOf(context.getString(R.string.requested_permission_storage))
                        )
                    },
                    parentFragment
                )
            }
            R.id.image -> {
                fileViewerHelper.pickFiles(
                    Pair(TypeFile.IMAGE, TypeMultiple.SINGLE),
                    { viewModel.sendFiles(it.map { File(it, TypeFile.IMAGE) }) },
                    {
                        permissionListener.requestedPermissions(
                            arrayOf(Permission.STORAGE),
                            arrayOf(context.getString(R.string.requested_permission_storage))
                        )
                    },
                    parentFragment
                )
            }
            R.id.camera -> {
                fileViewerHelper.pickImageFromCamera(
                    { uri -> viewModel.sendFile(File(uri, TypeFile.IMAGE)) },
                    {
                        permissionListener.requestedPermissions(
                            arrayOf(Permission.CAMERA),
                            arrayOf(context.getString(R.string.requested_permission_camera))
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