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
import com.crafttalk.chat.databinding.ComCrafttalkChatViewHostBinding
import com.crafttalk.chat.databinding.ComCrafttalkChatViewWarningSnackbarBinding
import com.google.android.material.snackbar.ContentViewCallback
//import kotlinx.android.synthetic.main.com_crafttalk_chat_view_warning_snackbar.view.*

class WarningSnackbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), ContentViewCallback {
    private var _binding: ComCrafttalkChatViewWarningSnackbarBinding? = null
    private val binding get() = _binding!!
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

        setVisibilityTextViewByContent(binding.warningSnackbarTitle, title)
        setVisibilityTextViewByContent(binding.warningSnackbarDescription, description)
        binding.warningSnackbarTitle.setTextColor(textColor)
        binding.iconWarning.setImageResource(iconRes)
        binding.warningSnackbarContainer.setBackgroundColor(backgroundColor)
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
        binding.warningSnackbarTitle.alpha = 0f
        binding.warningSnackbarTitle.animate().alpha(1f).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()

        binding.warningSnackbarDescription.alpha = 0f
        binding.warningSnackbarDescription.animate().alpha(1f).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()
    }

    override fun animateContentOut(delay: Int, duration: Int) {
        binding.warningSnackbarTitle.alpha = 1f
        binding.warningSnackbarTitle.animate().alpha(0f).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()

        binding.warningSnackbarDescription.alpha = 1f
        binding.warningSnackbarDescription.animate().alpha(0f).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()
    }

}