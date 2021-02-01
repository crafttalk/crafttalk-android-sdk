package com.crafttalk.chat.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.crafttalk.chat.R

class ChatAttr
private constructor(
    attrArr: TypedArray,
    context: Context
) {

    init {
        attrArr.getInt(R.styleable.ChatView_auth, -1).let { if (it != -1) ChatParams.authType = AuthType.values()[it] }
        attrArr.getInt(R.styleable.ChatView_timeDelayed, 0).let { ChatParams.timeDelayed = it.toLong() }
        attrArr.getString(R.styleable.ChatView_urlSocketNameSpace)?.let { ChatParams.urlSocketNameSpace = it }
        attrArr.getString(R.styleable.ChatView_urlSocketHost)?.let { ChatParams.urlSocketHost = it }
        attrArr.getString(R.styleable.ChatView_urlUploadNameSpace)?.let { ChatParams.urlUploadNameSpace = it }
        attrArr.getString(R.styleable.ChatView_urlUploadHost)?.let { ChatParams.urlUploadHost = it }
    }

    // UI part
    private val scaleRatio = context.resources.displayMetrics.density

    val colorBackgroundUserMessage = attrArr.getColor(R.styleable.ChatView_color_bg_user_message, ContextCompat.getColor(context, R.color.default_color_bg_user_message))
    val colorBackgroundOperatorMessage = attrArr.getColor(R.styleable.ChatView_color_bg_server_message, ContextCompat.getColor(context, R.color.default_color_bg_server_message))
    val colorBackgroundOperatorAction = attrArr.getColor(R.styleable.ChatView_color_bg_server_action, ContextCompat.getColor(context, R.color.default_color_bg_server_action))
    val colorBordersOperatorAction = attrArr.getColor(R.styleable.ChatView_color_borders_server_action, ContextCompat.getColor(context, R.color.default_color_borders_server_action))

    val colorMain = attrArr.getColor(R.styleable.ChatView_color_main, ContextCompat.getColor(context, R.color.default_color_main))
    val colorTextInternetConnectionWarning = attrArr.getColor(R.styleable.ChatView_color_text_warning, ContextCompat.getColor(context, R.color.default_color_text_warning))
    val colorTextCompanyName = attrArr.getColor(R.styleable.ChatView_color_company, ContextCompat.getColor(context, R.color.default_color_company))
    val colorTextUserMessage = attrArr.getColor(R.styleable.ChatView_color_text_user_message, ContextCompat.getColor(context, R.color.default_color_text_user_message))
    val colorTextOperatorMessage = attrArr.getColor(R.styleable.ChatView_color_text_server_message, ContextCompat.getColor(context, R.color.default_color_text_server_message))
    val colorTextOperatorAction = attrArr.getColor(R.styleable.ChatView_color_text_server_action, ContextCompat.getColor(context, R.color.default_color_text_server_action))
    val colorTextTimeMark = attrArr.getColor(R.styleable.ChatView_color_time_mark, ContextCompat.getColor(context, R.color.default_color_time_mark))

    val progressIndeterminateDrawable = attrArr.getDrawable(R.styleable.ChatView_progressIndeterminateDrawable)

    val sizeTextInternetConnectionWarning = attrArr.getDimension(R.styleable.ChatView_size_warning, context.resources.getDimension(R.dimen.default_size_warning)) / scaleRatio
    val sizeTextInfoText = attrArr.getDimension(R.styleable.ChatView_size_info, context.resources.getDimension(R.dimen.default_size_company)) / scaleRatio
    val sizeTextUserMessage = attrArr.getDimension(R.styleable.ChatView_size_user_message, context.resources.getDimension(R.dimen.default_size_user_message)) / scaleRatio
    val sizeTextOperatorMessage = attrArr.getDimension(R.styleable.ChatView_size_server_message, context.resources.getDimension(R.dimen.default_size_server_message)) / scaleRatio
    val sizeTextOperatorAction = attrArr.getDimension(R.styleable.ChatView_size_server_action, context.resources.getDimension(R.dimen.default_size_server_action)) / scaleRatio
    val sizeTextTimeMark = attrArr.getDimension(R.styleable.ChatView_size_time_mark, context.resources.getDimension(R.dimen.default_size_time_mark)) / scaleRatio

    val companyName = attrArr.getString(R.styleable.ChatView_company_name) ?: context.getString(R.string.chat_name_company)
    val showCompanyName = attrArr.getBoolean(R.styleable.ChatView_show_company_name, false)
    val showInternetConnectionState = attrArr.getBoolean(R.styleable.ChatView_show_internet_connection_state, true)
    val showUpperLimiter = attrArr.getBoolean(R.styleable.ChatView_show_upper_limiter, true)

    val drawableBackgroundSignInButton: Drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.background_sign_in_auth_form)!!).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(this, colorMain)
        } else {
            this.mutate().setColorFilter(colorMain, PorterDuff.Mode.SRC_IN)
        }
    }

    companion object {
        @Volatile private var INSTANCE: ChatAttr? = null

        fun getInstance(attrArr: TypedArray? = null, context: Context? = null): ChatAttr =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatAttr(attrArr!!, context!!).also { INSTANCE = it }
            }
        fun createInstance(attrArr: TypedArray, context: Context) {
            getInstance(attrArr, context)
        }
    }

}
