package com.crafttalk.chat.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.R
import com.crafttalk.chat.data.api.FileApi
import com.crafttalk.chat.data.helper.file.FileInfoHelper
import com.crafttalk.chat.data.helper.file.RequestHelper
import com.crafttalk.chat.data.local.db.database.ChatDatabase
import com.crafttalk.chat.data.local.pref.Uuid
import com.crafttalk.chat.data.local.pref.checkVisitorInPref
import com.crafttalk.chat.data.local.pref.getVisitorFromPref
import com.crafttalk.chat.data.remote.socket_service.SocketApi
import com.crafttalk.chat.data.repository.*
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection
import com.crafttalk.chat.domain.usecase.auth.LogIn
import com.crafttalk.chat.domain.usecase.file.UploadFiles
import com.crafttalk.chat.domain.usecase.internet.SetInternetConnectionListener
import com.crafttalk.chat.domain.usecase.message.*
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
import com.crafttalk.chat.utils.ConstantsUtils.URL_UPLOAD_HOST
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_entry_field.view.*
import kotlinx.android.synthetic.main.view_chat.view.*
import kotlinx.android.synthetic.main.view_chat.view.chat_place
import kotlinx.android.synthetic.main.view_chat.view.loading
import kotlinx.android.synthetic.main.view_chat.view.upper_limiter
import kotlinx.android.synthetic.main.view_chat.view.warning
import kotlinx.android.synthetic.main.view_host.view.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.set

class ChatView: RelativeLayout, View.OnClickListener, BottomSheetFileViewer.Listener {

    private lateinit var viewModel: ChatViewModel
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

        setAllListeners()
        val attrArr = context.obtainStyledAttributes(attrs, R.styleable.ChatView)
        customizationChat(attrArr)
        attrArr.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @SuppressLint("ResourceType")
    private fun customizationChat(attrArr: TypedArray) {
        val scaleRatio = resources.displayMetrics.density
        // set content
//        title.text = attrArr.getString(R.styleable.ChatView_title_text)
        send_message.setColorFilter(attrArr.getColor(R.styleable.ChatView_color_main, ContextCompat.getColor(context, R.color.default_color_main)), PorterDuff.Mode.SRC_IN)

        val newTintColor = attrArr.getColor(R.styleable.ChatView_color_main, ContextCompat.getColor(context, R.color.default_color_main))
        val bgSignInBtn = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.background_sign_in_auth_form)!!)
        sign_in.setBackgroundDrawable(bgSignInBtn)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(bgSignInBtn, newTintColor)
        }
        else {
            bgSignInBtn.mutate().setColorFilter(newTintColor, PorterDuff.Mode.SRC_IN)
        }
        // set color
        warning.setTextColor(attrArr.getColor(R.styleable.ChatView_color_text_warning, ContextCompat.getColor(context, R.color.default_color_text_warning)))
        company_name.setTextColor(attrArr.getColor(R.styleable.ChatView_color_company, ContextCompat.getColor(context, R.color.default_color_company)))
//        title.setTextColor(attrArr.getColor(R.styleable.ChatView_color_title, ContextCompat.getColor(context, R.color.default_color_title)))
        // set dimension
//        title.textSize = attrArr.getDimension(R.styleable.ChatView_size_title, 22f)/scaleRatio
        warning.textSize = attrArr.getDimension(
            R.styleable.ChatView_size_warning, resources.getDimension(
                R.dimen.default_size_warning))/scaleRatio
        company_name.textSize = attrArr.getDimension(
            R.styleable.ChatView_size_company, resources.getDimension(
                R.dimen.default_size_company))/scaleRatio
        // set bg
        upper_limiter.setBackgroundColor(attrArr.getColor(R.styleable.ChatView_color_main, ContextCompat.getColor(context, R.color.default_color_main)))
        lower_limit.setBackgroundColor(attrArr.getColor(R.styleable.ChatView_color_main, ContextCompat.getColor(context, R.color.default_color_main)))


        ChatAttr.mapAttr["color_bg_user_message"] = attrArr.getColor(R.styleable.ChatView_color_bg_user_message, ContextCompat.getColor(context, R.color.default_color_bg_user_message))
        ChatAttr.mapAttr["color_bg_server_message"] = attrArr.getColor(R.styleable.ChatView_color_bg_server_message, ContextCompat.getColor(context, R.color.default_color_bg_server_message))
        ChatAttr.mapAttr["color_bg_server_action"] = attrArr.getColor(R.styleable.ChatView_color_bg_server_action, ContextCompat.getColor(context, R.color.default_color_bg_server_action))
        ChatAttr.mapAttr["color_borders_server_action"] = attrArr.getColor(R.styleable.ChatView_color_borders_server_action, ContextCompat.getColor(context, R.color.default_color_borders_server_action))
        ChatAttr.mapAttr["color_text_user_message"] = attrArr.getColor(R.styleable.ChatView_color_text_user_message, ContextCompat.getColor(context, R.color.default_color_text_user_message))
        ChatAttr.mapAttr["color_text_server_message"] = attrArr.getColor(R.styleable.ChatView_color_text_server_message, ContextCompat.getColor(context, R.color.default_color_text_server_message))
        ChatAttr.mapAttr["color_text_server_action"] = attrArr.getColor(R.styleable.ChatView_color_text_server_action, ContextCompat.getColor(context, R.color.default_color_text_server_action))
        ChatAttr.mapAttr["color_time_mark"] = attrArr.getColor(R.styleable.ChatView_color_time_mark, ContextCompat.getColor(context, R.color.default_color_time_mark))
        ChatAttr.mapAttr["size_user_message"] = attrArr.getDimension(
            R.styleable.ChatView_size_user_message, resources.getDimension(
                R.dimen.default_size_user_message))
        ChatAttr.mapAttr["size_server_message"] = attrArr.getDimension(
            R.styleable.ChatView_size_server_message, resources.getDimension(
                R.dimen.default_size_server_message))
        ChatAttr.mapAttr["size_server_action"] = attrArr.getDimension(
            R.styleable.ChatView_size_server_action, resources.getDimension(
                R.dimen.default_size_server_action))
        ChatAttr.mapAttr["size_time_mark"] = attrArr.getDimension(
            R.styleable.ChatView_size_time_mark, resources.getDimension(
                R.dimen.default_size_time_mark))
        ChatAttr.mapAttr["auth_with_hash"] = attrArr.getBoolean(R.styleable.ChatView_auth_with_hash, false)
        ChatAttr.mapAttr["auth_with_form"] = attrArr.getBoolean(R.styleable.ChatView_auth_with_form, true)
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

    private fun setListMessages(layoutInflater: LayoutInflater) {
        adapterListMessages =
            AdapterListMessages(
                layoutInflater,
                listOf(),
                viewModel.actionListener,
                viewModel.updateSizeMessageListener,
                viewModel::openFile,
                viewModel::openImage,
                viewModel::openGif
            ).apply {
                list_with_message.adapter = this
            }
    }

    fun onCreate(fragment: Fragment, listener: EventListener, visitor: Visitor? = null) {
        Log.d(ConstantsUtils.TAG_SOCKET, "onCreate Chat View")
        this.parentFragment = fragment
        this.listener = listener
        this.viewModel = if (!(ChatAttr.mapAttr["auth_with_form"] as Boolean) && visitor != null) {
            deleteThisFun(visitor)
        }
        else {
            val pref = context.getSharedPreferences("data_visitor", MODE_PRIVATE)
            if (checkVisitorInPref(pref)) {
                deleteThisFun(getVisitorFromPref(pref))
            }
            deleteThisFun(null)
        }
        setListMessages(inflater)
    }


    fun deleteThisFun(visitor: Visitor?): ChatViewModel {
        val dao = ChatDatabase.getInstance(context.applicationContext).messageDao()
        val socketApi = SocketApi(
            DataRepository(
                dao
            ),
            Gson()
        )
        val messsageRepository = MessageRepository(
            dao,
            socketApi
        )

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val factory = ChatViewModelFactory(
            UploadFiles(
                FileRepository(
                    Retrofit.Builder()
                        .baseUrl(URL_UPLOAD_HOST)
                        .addConverterFactory(GsonConverterFactory.create(gson))
//            .addConverterFactory(ScalarsConverterFactory.create())
                        .build().create(FileApi::class.java),
                    Uuid.generateUUID(false),
                    FileInfoHelper(
                        context
                    ),
                    RequestHelper(
                        context
                    )
                )
            ),
            GetMessages(messsageRepository),
            SendMessages(messsageRepository),
            SyncMessages(
                messsageRepository
            ),
            SelectAction(
                messsageRepository
            ),
            LogIn(
                VisitorRepository(
                    context.getSharedPreferences("data_visitor", MODE_PRIVATE),
                    socketApi
                )
            ),
            SetInternetConnectionListener(
                InternetConnectionRepository(
                    socketApi
                )
            ),
            visitor,
            this,
            socketApi,
            UpdateSizeMessages(
                MessageRepository(
                    dao,
                    socketApi
                )
            )
        )
        return ViewModelProvider(parentFragment, factory).get(ChatViewModel::class.java)
    }


    fun onResume(lifecycleOwner: LifecycleOwner) {
        viewModel.internetConnection.observe(lifecycleOwner, Observer {
            Log.d("CHAT_VIEW", "GET NEW EVENT")
            when (it) {
                TypeInternetConnection.NO_INTERNET -> {
                    warning.text = "Waiting for network..."
                    sign_in.isClickable = true
                    Log.d("CHAT_VIEW", "NO_INTERNET_CONNECTION")
                }
                TypeInternetConnection.HAS_INTERNET -> {
                    Log.d(ConstantsUtils.TAG_SOCKET, "Sicnk HAS_INTERNET")
                    warning.text = ""
                }
            }
        })
        viewModel.messages.observe(lifecycleOwner, Observer {
            Log.d("CHAT_VIEW", "adding new message size = ${it.size}")
            val countNewMessage = it.size - list_with_message.adapter!!.itemCount
            adapterListMessages.setData(messageModelMapper(it, context))
            if (countNewMessage > 0) {
                list_with_message.smoothScrollToPosition(it.size - 1)
            }
            adapterListMessages.notifyDataSetChanged()
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
                if (message.isNotEmpty()) {
                    hideSoftKeyboard(this)
                    viewModel.sendMessage(message)
                    entry_field.text.clear()
                }
                else {

                    BottomSheetFileViewer.Builder()
                        .add(R.menu.options)
                        .setListener(this)
                        .show(parentFragment.parentFragmentManager)
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