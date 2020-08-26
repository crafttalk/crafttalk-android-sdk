package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageWithCheck
import com.crafttalk.chat.presentation.model.TextMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderUserTextMessage(
    view: View,
    private val scaleRatio: Float
) : BaseViewHolder<TextMessageItem>(view) {
    private val message: TextView = view.findViewById(R.id.user_message)
    private val time: TextView = view.findViewById(R.id.time)

    override fun bindTo(item: TextMessageItem) {
        time.setTimeMessageWithCheck(item, scaleRatio)
        // set content
        message.text = item.message
        // set color
        message.setTextColor(ChatAttr.mapAttr["color_text_user_message"] as Int)
        // set dimension
        message.textSize = (ChatAttr.mapAttr["size_user_message"] as Float) / scaleRatio
        // set bg
        ViewCompat.setBackgroundTintList(message, ColorStateList.valueOf(ChatAttr.mapAttr["color_bg_user_message"] as Int))
    }
}