package com.crafttalk.chat.ui.chat_view

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.crafttalk.chat.Events
import com.crafttalk.chat.R
import com.crafttalk.chat.data.model.Visitor
import com.crafttalk.chat.data.remote.socket_service.SocketAPI
import com.crafttalk.chat.ui.chat_view.adapters.AdapterListMessages
import com.crafttalk.chat.ui.chat_view.view_model.ChatViewModel
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SOCKET
import com.crafttalk.chat.utils.hideSoftKeyboard
import kotlinx.android.synthetic.main.fragment_entry_field.view.*
import kotlinx.android.synthetic.main.view_chat.view.*


class ChatView: RelativeLayout, View.OnClickListener {

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapterListMessages: AdapterListMessages
    private lateinit var listener: ListenerChat

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val service = Context.LAYOUT_INFLATER_SERVICE
        val layoutInflater = getContext().getSystemService(service) as LayoutInflater
        layoutInflater.inflate(R.layout.view_chat, this, true)

        setAllListeners()
        val attrArr = context.obtainStyledAttributes(attrs, R.styleable.ChatView)
        customizationChat(attrArr)
        setListMessages(layoutInflater)
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
        warning.textSize = attrArr.getDimension(R.styleable.ChatView_size_warning, resources.getDimension(R.dimen.default_size_warning))/scaleRatio
        company_name.textSize = attrArr.getDimension(R.styleable.ChatView_size_company, resources.getDimension(R.dimen.default_size_company))/scaleRatio
        // set bg
        upper_limiter.setBackgroundColor(attrArr.getColor(R.styleable.ChatView_color_main, ContextCompat.getColor(context, R.color.default_color_main)))
        lower_limit.setBackgroundColor(attrArr.getColor(R.styleable.ChatView_color_main, ContextCompat.getColor(context, R.color.default_color_main)))


        ChatAttr.mapAttr["color_bg_user_message"] = attrArr.getColor(R.styleable.ChatView_color_bg_user_message, ContextCompat.getColor(context, R.color.default_color_bg_user_message))
        ChatAttr.mapAttr["color_bg_server_message"] = attrArr.getColor(R.styleable.ChatView_color_bg_server_message, ContextCompat.getColor(context, R.color.default_color_bg_server_message))
        ChatAttr.mapAttr["color_bg_server_action"] = attrArr.getColor(R.styleable.ChatView_color_bg_server_action, ContextCompat.getColor(context, R.color.default_color_bg_server_action))
        ChatAttr.mapAttr["color_borders_server_action"] = attrArr.getColor(R.styleable.ChatView_color_borders_server_action, ContextCompat.getColor(context, R.color.default_color_borders_server_action))
        ChatAttr.mapAttr["color_text_user_message"] = attrArr.getColor(R.styleable.ChatView_color_text_user_message, ContextCompat.getColor(context, R.color.default_color_text_user_message))
        ChatAttr.mapAttr["color_text_server_message"] = attrArr.getString(R.styleable.ChatView_color_text_server_message) ?: context.getString(R.string.default_color_text_server_message)
        ChatAttr.mapAttr["color_text_server_action"] = attrArr.getColor(R.styleable.ChatView_color_text_server_action, ContextCompat.getColor(context, R.color.default_color_text_server_action))
        ChatAttr.mapAttr["color_time_mark"] = attrArr.getColor(R.styleable.ChatView_color_time_mark, ContextCompat.getColor(context, R.color.default_color_time_mark))
        ChatAttr.mapAttr["size_user_message"] = attrArr.getDimension(R.styleable.ChatView_size_user_message, resources.getDimension(R.dimen.default_size_user_message))
        ChatAttr.mapAttr["size_server_message"] = attrArr.getDimension(R.styleable.ChatView_size_server_message, resources.getDimension(R.dimen.default_size_server_message))
        ChatAttr.mapAttr["size_server_action"] = attrArr.getDimension(R.styleable.ChatView_size_server_action, resources.getDimension(R.dimen.default_size_server_action))
        ChatAttr.mapAttr["size_time_mark"] = attrArr.getDimension(R.styleable.ChatView_size_time_mark, resources.getDimension(R.dimen.default_size_time_mark))
        ChatAttr.mapAttr["auth_with_hash"] = attrArr.getBoolean(R.styleable.ChatView_auth_with_hash, false)
        ChatAttr.mapAttr["auth_with_form"] = attrArr.getBoolean(R.styleable.ChatView_auth_with_form, true)
    }

    private fun setAllListeners() {
        sign_in.setOnClickListener(this)
    }

    private fun setListMessages(layoutInflater: LayoutInflater) {
        adapterListMessages =
            AdapterListMessages(
                layoutInflater,
                listOf()
            )
    }

    fun onCreate(app: Application, listener: ListenerChat, visitor: Visitor?){//}, viewModel: ChatViewModel) {
        Log.d(TAG_SOCKET, "onCreate Chat View")
        this.listener = listener
        this.viewModel = if (!(ChatAttr.mapAttr["auth_with_form"] as Boolean) && visitor != null) {
            ChatViewModel(app, visitor)
        }
        else {
            ChatViewModel(app, null)
        }
        ((context as FragmentActivity).supportFragmentManager.findFragmentById(R.id.fragment_entry_field) as EntryFieldFragment).setViewModel(viewModel)
    }

    fun onResume(lifecycleOwner: LifecycleOwner) {
        viewModel.allEvents.observe(lifecycleOwner, Observer {
            Log.d("CHAT_VIEW", "GET NEW EVENT")
            when (it) {
                Events.SOCKET_DESTROY -> {
                    Log.d("CHAT_VIEW", "SOCKET_DESTROY")
                }
                Events.MESSAGE_SEND -> {
                    entry_field.text.clear()
                }
                Events.MESSAGE_GET_SERVER -> {}
                Events.USER_NOT_FAUND -> {
                    Log.d("CHAT_VIEW", "USER_NOT_FAUND")
                    form_place.visibility = View.VISIBLE
                    chat_place.visibility = View.GONE
                }
                Events.USER_FAUND_WITHOUT_AUTH -> {
                    Log.d("CHAT_VIEW", "USER_FAUND_WITHOUT_AUTH")
                    stopProgressBar()
                    form_place.visibility = View.GONE
                    chat_place.visibility = View.VISIBLE
                    if (list_with_message.adapter == null) {
                        list_with_message.adapter = adapterListMessages
                    }
                    // ограничения на ввод сообщений
                }
                Events.USER_AUTHORIZAT -> {
                    Log.d("CHAT_VIEW", "USER_AUTHORIZAT")
                    stopProgressBar()
                    form_place.visibility = View.GONE
                    chat_place.visibility = View.VISIBLE
                    if (list_with_message.adapter == null) {
                        list_with_message.adapter = adapterListMessages
                    }
                    viewModel.syncData()
                }
                Events.NO_INTERNET -> {
                    warning.text = "Waiting for network..."
                    sign_in.isClickable = true
                    Log.d("CHAT_VIEW", "NO_INTERNET_CONNECTION")
                }
                Events.HAS_INTERNET -> {
                    Log.d(TAG_SOCKET, "Sicnk HAS_INTERNET")
                    warning.text = ""
                }
            }
        })
        viewModel.messages.observe(lifecycleOwner, Observer {
            Log.d("CHAT_VIEW", "adding new message size = ${it.size}")
//            обновление листа
            adapterListMessages.setData(it)
//            list_with_message.adapter?.let {
//                val countMessages = it.itemCount - 1
//                if (countMessages > 0) {
//                    list_with_message.scrollToPosition(countMessages)
//                }
//            }
            adapterListMessages.notifyDataSetChanged()
        })
    }

    fun onDestroy() {
        val fm = (context as FragmentActivity).supportFragmentManager

        val fragmentEntryField = fm.findFragmentById(R.id.fragment_entry_field) as? EntryFieldFragment

        Log.d("ChatView", "onDestroy ${fm}; ${fragmentEntryField}")

        if (fragmentEntryField != null)
            fm.beginTransaction().remove(fragmentEntryField).commitAllowingStateLoss()
        // Когда буду настраивать VM нужно убрать эту стр
        SocketAPI.destroy()
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
        }
    }

}