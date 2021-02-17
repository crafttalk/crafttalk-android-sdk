package com.crafttalk.chat.presentation.custom_views.custom_snackbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.crafttalk.chat.R
import com.google.android.material.snackbar.ContentViewCallback
import kotlinx.android.synthetic.main.view_warning_snackbar.view.*

class WarningSnackbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), ContentViewCallback {

    init {
        View.inflate(context, R.layout.view_warning_snackbar, this)
        clipToPadding =  false
    }

    fun bind(title: String?, description: String?) {
        setVisibilityByContent(warning_snackbar_title, title)
        setVisibilityByContent(warning_snackbar_description, description)
    }

    fun bind(@StringRes title: Int?, @StringRes description: Int?) {
        bind(title?.let { context.getString(it) }, description?.let { context.getString(it) })
    }

    private fun setVisibilityByContent(view: TextView, content: String?) {
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