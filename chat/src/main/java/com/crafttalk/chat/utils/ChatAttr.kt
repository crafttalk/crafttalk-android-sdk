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
        attrArr.getString(R.styleable.ChatView_fileProviderAuthorities)?.let { ChatParams.fileProviderAuthorities = it }
    }

    val colorBackgroundUserMessage = attrArr.getColor(R.styleable.ChatView_color_bg_user_message, ContextCompat.getColor(context, R.color.default_color_bg_user_message))
    val colorBackgroundOperatorMessage = attrArr.getColor(R.styleable.ChatView_color_bg_server_message, ContextCompat.getColor(context, R.color.default_color_bg_server_message))
    val colorBackgroundOperatorAction = attrArr.getColor(R.styleable.ChatView_color_bg_server_action, ContextCompat.getColor(context, R.color.default_color_bg_server_action))
    val colorBordersOperatorAction = attrArr.getColor(R.styleable.ChatView_color_borders_server_action, ContextCompat.getColor(context, R.color.default_color_borders_server_action))

    val colorMain = attrArr.getColor(R.styleable.ChatView_color_main, ContextCompat.getColor(context, R.color.default_color_main))
    val colorTextInternetConnectionWarning = attrArr.getColor(R.styleable.ChatView_color_text_warning, ContextCompat.getColor(context, R.color.default_color_text_warning))
    val colorTextInfo = attrArr.getColor(R.styleable.ChatView_color_company, ContextCompat.getColor(context, R.color.default_color_info))
    val colorTextUserMessage = attrArr.getColor(R.styleable.ChatView_color_text_user_message, ContextCompat.getColor(context, R.color.default_color_text_user_message))
    val colorTextOperatorMessage = attrArr.getColor(R.styleable.ChatView_color_text_server_message, ContextCompat.getColor(context, R.color.default_color_text_server_message))
    val colorTextOperatorAction = attrArr.getColor(R.styleable.ChatView_color_text_server_action, ContextCompat.getColor(context, R.color.default_color_text_server_action))
    val colorTextTimeMark = attrArr.getColor(R.styleable.ChatView_color_time_mark, ContextCompat.getColor(context, R.color.default_color_time_mark))
    val colorTextDateGrouping = attrArr.getColor(R.styleable.ChatView_color_text_date_grouping, ContextCompat.getColor(context, R.color.default_color_info))
    val colorTextLink = attrArr.getColor(R.styleable.ChatView_color_text_link, ContextCompat.getColor(context, R.color.default_color_text_link))

    val progressIndeterminateDrawable = attrArr.getDrawable(R.styleable.ChatView_progressIndeterminateDrawable)

    val sizeTextInternetConnectionWarning = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_warning, context.resources.getDimensionPixelSize(R.dimen.default_size_warning)).toFloat()
    val sizeTextInfoText = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_info, context.resources.getDimensionPixelSize(R.dimen.default_size_info)).toFloat()
    val sizeTextUserMessage = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_user_message, context.resources.getDimensionPixelSize(R.dimen.default_size_user_message)).toFloat()
    val sizeTextOperatorMessage = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_server_message, context.resources.getDimensionPixelSize(R.dimen.default_size_server_message)).toFloat()
    val sizeTextOperatorAction = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_server_action, context.resources.getDimensionPixelSize(R.dimen.default_size_server_action)).toFloat()
    val sizeTextTimeMark = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_time_mark, context.resources.getDimensionPixelSize(R.dimen.default_size_time_mark)).toFloat()
    val sizeTextDateGrouping = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_text_date_grouping, context.resources.getDimensionPixelSize(R.dimen.default_size_info)).toFloat()

    val marginStartMediaFile = attrArr.getDimension(R.styleable.ChatView_margin_start_media_file, context.resources.getDimension(R.dimen.default_margin_start_media_file)).toInt()
    val marginEndMediaFile = attrArr.getDimension(R.styleable.ChatView_margin_end_media_file, context.resources.getDimension(R.dimen.default_margin_end_media_file)).toInt()
    val marginTopMediaFile = attrArr.getDimension(R.styleable.ChatView_margin_top_media_file, context.resources.getDimension(R.dimen.default_margin_top_media_file)).toInt()
    val marginBottomMediaFile = attrArr.getDimension(R.styleable.ChatView_margin_bottom_media_file, context.resources.getDimension(R.dimen.default_margin_bottom_media_file)).toInt()

    val companyName = attrArr.getString(R.styleable.ChatView_company_name) ?: context.getString(R.string.chat_name_company)
    val showCompanyName = attrArr.getBoolean(R.styleable.ChatView_show_company_name, false)
    val showInternetConnectionState = attrArr.getBoolean(R.styleable.ChatView_show_internet_connection_state, true)
    val showUpperLimiter = attrArr.getBoolean(R.styleable.ChatView_show_upper_limiter, true)
    val showStartingProgress = attrArr.getBoolean(R.styleable.ChatView_show_starting_progress, true)

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