package com.crafttalk.chat.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.crafttalk.chat.R
import com.crafttalk.chat.di.DaggerSdkComponent
import com.crafttalk.chat.di.modules.VisitorModule
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection
import com.crafttalk.chat.presentation.adapters.AdapterListMessages
import com.crafttalk.chat.presentation.feature.file_viewer.BottomSheetFileViewer
import com.crafttalk.chat.presentation.feature.file_viewer.Option
import com.crafttalk.chat.presentation.helper.file_viewer_helper.FileViewerHelper
import com.crafttalk.chat.presentation.helper.mappers.messageModelMapper
import com.crafttalk.chat.presentation.helper.permission.PermissionHelper
import com.crafttalk.chat.presentation.helper.ui.hideSoftKeyboard
import com.crafttalk.chat.presentation.model.TypeMultiple
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.ConstantsUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.auth_layout.view.*
import kotlinx.android.synthetic.main.chat_layout.view.*
import kotlinx.android.synthetic.main.view_host.view.*
import javax.inject.Inject

class ChatView: RelativeLayout, View.OnClickListener, BottomSheetFileViewer.Listener {

    @Inject
    lateinit var viewModel: ChatViewModel
    private lateinit var adapterListMessages: AdapterListMessages
    private lateinit var listener: EventListener
    private val fileViewerHelper = FileViewerHelper(PermissionHelper())
    private lateinit var parentFragment: Fragment
    private val inflater: LayoutInflater by lazy {
         context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        inflater.inflate(R.layout.view_host, this, true)

        val attrArr = context.obtainStyledAttributes(attrs, R.styleable.ChatView)
        ChatAttr.createInstance(attrArr, context)
        customizationChat(attrArr)
        attrArr.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @SuppressLint("ResourceType")
    private fun customizationChat(attrArr: TypedArray) {
        val chatAttr = ChatAttr.getInstance(attrArr, context)
        // set color
        send_message.setColorFilter(chatAttr.colorMain, PorterDuff.Mode.SRC_IN)
        sign_in.setBackgroundDrawable(chatAttr.drawableBackgroundSignInButton)

        warning.setTextColor(chatAttr.colorTextInternetConnectionWarning)
        company_name.setTextColor(chatAttr.colorTextCompanyName)
        // set dimension
        warning.textSize = chatAttr.sizeTextInternetConnectionWarning
        company_name.textSize = chatAttr.sizeTextCompanyName
        // set bg
        upper_limiter.setBackgroundColor(chatAttr.colorMain)
        lower_limit.setBackgroundColor(chatAttr.colorMain)
    }

    private fun setAllListeners() {
        sign_in.setOnClickListener(this)
        send_message.setOnClickListener(this)
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

    fun onCreate(fragment: Fragment, listener: EventListener, visitor: Visitor? = null) {
        DaggerSdkComponent
            .builder()
            .context(context)
            .chatView(this)
            .parentFragment(fragment)
            .visitorModule(VisitorModule(visitor))
            .build()
            .inject(this)

        Log.d(ConstantsUtils.TAG_SOCKET, "onCreate Chat View")
        this.parentFragment = fragment
        this.listener = listener
        setAllListeners()
        setListMessages()
    }

    fun onResume(lifecycleOwner: LifecycleOwner) {
        viewModel.internetConnection.observe(lifecycleOwner, Observer {
            Log.d("CHAT_VIEW", "GET NEW EVENT")
            when (it) {
                TypeInternetConnection.NO_INTERNET -> {
                    warning.visibility = View.VISIBLE
                    sign_in.isClickable = true
                    Log.d("CHAT_VIEW", "NO_INTERNET_CONNECTION")
                }
                TypeInternetConnection.HAS_INTERNET -> {
                    Log.d(ConstantsUtils.TAG_SOCKET, "Sicnk HAS_INTERNET")
                    warning.visibility = View.GONE
                }
            }
        })
        viewModel.messages.observe(lifecycleOwner, Observer {
            val countNewMessage = it.size - adapterListMessages.itemCount
            adapterListMessages.data = messageModelMapper(it, context)
            if (countNewMessage > 0) {
                list_with_message.smoothScrollToPosition(it.size - 1)
            }
        })
    }

    private fun checkerObligatoryFields(fields: List<EditText>): Boolean {
        var result = true
        fields.forEach{
            if (it.text.trim().isEmpty()) {
                it.setBackgroundResource(R.drawable.background_error_field_auth_form)
                result = false
            }
            else {
                it.setBackgroundResource(R.drawable.background_normal_field_auth_form)
            }
        }
        return result
    }

    private fun startProgressBar() {
        loading.visibility = View.VISIBLE
    }

    private fun stopProgressBar() {
        loading.visibility = View.GONE
    }

    fun showChat() {
        chat_place.post {
            auth_form.visibility = View.GONE
            chat_place.visibility = View.VISIBLE
            stopProgressBar()
        }
    }

    fun showLogInForm() {
        chat_place.post {
            chat_place.visibility = View.GONE
            auth_form.visibility = View.VISIBLE
            stopProgressBar()
        }
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.sign_in -> {
                if (checkerObligatoryFields(listOf(first_name_user, last_name_user, phone_user))) {
                    hideSoftKeyboard(this)
                    startProgressBar()
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
        }
    }

    interface EventListener {
        fun onErrorAuth()
        fun onAuth()
    }

    private fun showWarning(warningText: String) {
        Snackbar.make(warning, warningText, Snackbar.LENGTH_LONG).show()
    }
    override fun onModalOptionSelected(tag: String?, option: Option) {
        Log.d("EVENT_PICK", "EVENT")
        when (option.id) {
            R.id.document -> {
                Log.d("EVENT_PICK", "DOC")
                fileViewerHelper.pickFiles(
                    Pair(TypeFile.FILE, TypeMultiple.SINGLE),
                    {
                        viewModel.sendFiles(
                            it.map {
                                File(it, TypeFile.FILE)
                            }
                        )
                    },
                    {
                        showWarning("У вас нет разрешения на доступ к ресурсам!")
                    },
                    parentFragment
                )
            }
            R.id.image -> {
                Log.d("EVENT_PICK", "IMAGE")
                fileViewerHelper.pickFiles(
                    Pair(TypeFile.IMAGE, TypeMultiple.SINGLE),
                    {
                        viewModel.sendFiles(
                            it.map {
                                File(it, TypeFile.IMAGE)
                            }
                        )
                    },
                    {
                        showWarning("У вас нет разрешения на доступ к ресурсам!")
                    },
                    parentFragment
                )
            }
            R.id.camera -> {
                Log.d("EVENT_PICK", "CAMERA")
                fileViewerHelper.pickImageFromCamera(
                    {
                        viewModel.sendImage(it)
                    },
                    {
                        showWarning("У вас нет разрешения на доступ к камере!")
                    },
                    parentFragment
                )
            }
        }
    }

}