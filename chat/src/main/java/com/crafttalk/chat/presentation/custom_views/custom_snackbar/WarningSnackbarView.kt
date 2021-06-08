package com.crafttalk.chat.presentation.custom_views.custom_snackbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.crafttalk.chat.R
import com.google.android.material.snackbar.ContentViewCallback
import kotlinx.android.synthetic.main.com_crafttalk_chat_view_warning_snackbar.view.*

class WarningSnackbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), ContentViewCallback {

    init {
        View.inflate(context, R.layout.com_crafttalk_chat_view_warning_snackbar, this)
        clipToPadding =  false
    }

    @SuppressLint("ResourceAsColor")
    fun bind(
        title: String?,
        description: String?,
        @DrawableRes iconRes: Int,
        @ColorRes textColor: Int,
        @ColorRes backgroundColor: Int
    ) {
        setVisibilityTextViewByContent(warning_snackbar_title, title)
        setVisibilityTextViewByContent(warning_snackbar_description, description)

        warning_snackbar_title.setTextColor(textColor)
        icon_warning.setImageResource(iconRes)
        warning_snackbar_container.setBackgroundColor(backgroundColor)
    }

    fun bind(
        @StringRes title: Int?,
        @StringRes description: Int?,
        @DrawableRes iconRes: Int,
        @ColorRes textColor: Int,
        @ColorRes backgroundColor: Int
    ) {
        bind(title?.let { context.getString(it) }, description?.let { context.getString(it) }, iconRes, textColor, backgroundColor)
    }

    private fun setVisibilityTextViewByContent(view: TextView, content: String?) {
        if (content == null) {
            view.visibility = View.GONE
        } else {
            view.text = content
            view.visibility = View.VISIBLE
        }
    }

    override fun animateContentIn(delay: Int, duration: Int) {
        warning_snackbar_title.alpha = 0f
        warning_snackbar_title.animate().alpha(1f).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()

        warning_snackbar_description.alpha = 0f
        warning_snackbar_description.animate().alpha(1f).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()
    }

    override fun animateContentOut(delay: Int, duration: Int) {
        warning_snackbar_title.alpha = 1f
        warning_snackbar_title.animate().alpha(0f).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()

        warning_snackbar_description.alpha = 1f
        warning_snackbar_description.animate().alpha(0f).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()
    }

}