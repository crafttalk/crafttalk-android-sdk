package com.crafttalk.chat.ui.view

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.crafttalk.chat.Events
import com.crafttalk.chat.R
import com.crafttalk.chat.ui.view_model.ChatViewModel
import kotlinx.android.synthetic.main.view_chat.view.*
import com.crafttalk.chat.ui.ListenerChat
import com.crafttalk.chat.ui.view.adapters.AdapterListMessages


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
        val mapAttr = customizationChat(attrArr)
        setListMessages(layoutInflater, mapAttr)
        attrArr.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @SuppressLint("ResourceType")
    private fun customizationChat(attrArr: TypedArray): Map<String, Any> {
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


        val mapAttr = HashMap<String, Any>()
        mapAttr["color_bg_user_message"] = attrArr.getColor(R.styleable.ChatView_color_bg_user_message, ContextCompat.getColor(context, R.color.default_color_bg_user_message))
        mapAttr["color_bg_server_message"] = attrArr.getColor(R.styleable.ChatView_color_bg_server_message, ContextCompat.getColor(context, R.color.default_color_bg_server_message))
        mapAttr["color_bg_server_action"] = attrArr.getColor(R.styleable.ChatView_color_bg_server_action, ContextCompat.getColor(context, R.color.default_color_bg_server_action))
        mapAttr["color_borders_server_action"] = attrArr.getColor(R.styleable.ChatView_color_borders_server_action, ContextCompat.getColor(context, R.color.default_color_borders_server_action))
        mapAttr["color_text_user_message"] = attrArr.getColor(R.styleable.ChatView_color_text_user_message, ContextCompat.getColor(context, R.color.default_color_text_user_message))
        mapAttr["color_text_server_message"] = attrArr.getColor(R.styleable.ChatView_color_text_server_message, ContextCompat.getColor(context, R.color.default_color_text_server_message))
        mapAttr["color_text_server_action"] = attrArr.getColor(R.styleable.ChatView_color_text_server_action, ContextCompat.getColor(context, R.color.default_color_text_server_action))
        mapAttr["color_time_mark"] = attrArr.getColor(R.styleable.ChatView_color_time_mark, ContextCompat.getColor(context, R.color.default_color_time_mark))
        mapAttr["size_user_message"] = attrArr.getDimension(R.styleable.ChatView_size_user_message, resources.getDimension(R.dimen.default_size_user_message))
        mapAttr["size_server_message"] = attrArr.getDimension(R.styleable.ChatView_size_server_message, resources.getDimension(R.dimen.default_size_server_message))
        mapAttr["size_server_action"] = attrArr.getDimension(R.styleable.ChatView_size_server_action, resources.getDimension(R.dimen.default_size_server_action))
        mapAttr["size_time_mark"] = attrArr.getDimension(R.styleable.ChatView_size_time_mark, resources.getDimension(R.dimen.default_size_time_mark))
        return mapAttr
    }

    private fun setAllListeners() {
//        attach_file.setOnClickListener(this)
        sign_in.setOnClickListener(this)
//        like.setOnClickListener(this)
        send_message.setOnClickListener(this)

        entry_field.addTextChangedListener(
            object:TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    Log.d("ChatView", "afterTextChanged s - ${s}, check - ${((s?:"").isEmpty())}")
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
            }
        )
    }

    private fun setListMessages(layoutInflater: LayoutInflater, mapAttr: Map<String, Any>) {
        adapterListMessages =
            AdapterListMessages(
                layoutInflater,
                listOf(),
                mapAttr
            )
        list_with_message.adapter = adapterListMessages
    }

    fun onCreate(app: Application, listener: ListenerChat/*viewModel: ViewModel*/) {
        this.listener = listener
        this.viewModel = ChatViewModel(app)
    }

    fun onResume(lifecycleOwner: LifecycleOwner) {
        viewModel.allEvents.observe(lifecycleOwner, Observer {
            Log.d("CHAT_VIEW", "GET NEW EVENT")
            when (it) {
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
                    // ограничения на ввод сообщений
                }
                Events.USER_AUTHORIZAT -> {
                    Log.d("CHAT_VIEW", "USER_AUTHORIZAT")
                    stopProgressBar()
                    form_place.visibility = View.GONE
                    chat_place.visibility = View.VISIBLE
                    viewModel.syncData()
                }
                Events.NO_INTERNET -> {
                    stopProgressBar()
                    warning.text = "Waiting for network..."
                    Log.d("CHAT_VIEW", "NO_INTERNET_CONNECTION")
                }
                Events.HAS_INTERNET -> {
                    warning.text = ""
                }
            }
        })
        viewModel.messages.observe(lifecycleOwner, Observer {
            Log.d("CHAT_VIEW", "adding new message size = ${it.size}")
//            обновление листа
            adapterListMessages.setData(it)
            val countMessages = list_with_message.adapter!!.itemCount - 1
            if (countMessages > 0) {
                list_with_message.smoothScrollToPosition(countMessages)
            }
            adapterListMessages.notifyDataSetChanged()
        })
    }

    fun onDestroy() {}

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

    private fun hideSoftKeyboard(view: View?) {
        if (view != null) {
            val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun startProgressBar() {
        loading.visibility = View.VISIBLE
    }

    private fun stopProgressBar() {
        loading.visibility = View.GONE
    }

    override fun onClick(view: View) {
        when(view.id) {
//            R.id.attach_file -> {}
//            R.id.like -> {}
            R.id.sign_in -> {
                if (checkerObligatoryFields(listOf(first_name_user, last_name_user, phone_user))) {
                    hideSoftKeyboard(this)
                    startProgressBar()
                    val firstName = first_name_user.text.toString()
                    val lastName = last_name_user.text.toString()
                    val phone = phone_user.text.toString()
                    viewModel.registration(firstName, lastName, phone)
                }
            }
            R.id.send_message -> {
                val message = entry_field.text.toString().trim()
                if (message.isNotEmpty()) {
                    hideSoftKeyboard(this)
                    viewModel.sendMessage(message)
                }
            }
        }
    }

}