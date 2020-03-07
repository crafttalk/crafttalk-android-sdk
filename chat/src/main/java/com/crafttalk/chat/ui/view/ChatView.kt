package com.crafttalk.chat.ui.view

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
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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

        val newTintColor = attrArr.getColor(R.styleable.ChatView_main_color, ContextCompat.getColor(context, R.color.default_main_color))
        val bgSignInBtn = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.background_sign_in_auth_form)!!)
        sign_in.setBackgroundDrawable(bgSignInBtn)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(bgSignInBtn, newTintColor)
        }
        else {
            bgSignInBtn.mutate().setColorFilter(newTintColor, PorterDuff.Mode.SRC_IN)
        }

        title.text = attrArr.getString(R.styleable.ChatView_title_text)
        title.textSize = attrArr.getDimension(R.styleable.ChatView_title_size, 22f)/scaleRatio
        title.setTextColor(attrArr.getColor(R.styleable.ChatView_title_color, ContextCompat.getColor(context, R.color.default_color_title)))
        upper_limiter.setBackgroundColor(attrArr.getColor(R.styleable.ChatView_main_color, ContextCompat.getColor(context, R.color.default_main_color)))
        lower_limit.setBackgroundColor(attrArr.getColor(R.styleable.ChatView_main_color, ContextCompat.getColor(context, R.color.default_main_color)))
        send_message.setColorFilter(attrArr.getColor(R.styleable.ChatView_main_color, ContextCompat.getColor(context, R.color.default_main_color)), PorterDuff.Mode.SRC_IN)

        val mapAttr = HashMap<String, Any>()
        mapAttr["color_main"] = attrArr.getColor(R.styleable.ChatView_main_color, ContextCompat.getColor(context, R.color.default_main_color))
        return mapAttr
    }

    private fun setAllListeners() {
//        attach_file.setOnClickListener(this)
        sign_in.setOnClickListener(this)
//        like.setOnClickListener(this)
        send_message.setOnClickListener(this)
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
            when (it) {
                Events.MESSAGE_SEND -> {
                    entry_field.text.clear()
                }
                Events.MESSAGE_GET_SERVER -> {}
                Events.USER_NOT_FAUND -> {
                    form_place.visibility = View.VISIBLE
                    chat_place.visibility = View.GONE
                }
                Events.USER_AUTHORIZAT -> {
                    stopProgressBar()
                    form_place.visibility = View.GONE
                    chat_place.visibility = View.VISIBLE
                    viewModel.syncData()
                }
                Events.NO_INTERNET -> {
                    stopProgressBar()
                    Log.d("CHAT_VIEW", "NO_INTERNET_CONNECTION")
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