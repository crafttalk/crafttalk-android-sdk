package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.setDate
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageWithCheck
import com.crafttalk.chat.presentation.model.TextMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderUserTextMessage(
    view: View
) : BaseViewHolder<TextMessageItem>(view) {
    private val message: TextView = view.findViewById(R.id.user_message)
    private val time: TextView = view.findViewById(R.id.time)
    private val date: TextView = view.findViewById(R.id.date)

    override fun bindTo(item: TextMessageItem) {
        date.setDate(item)
        time.setTimeMessageWithCheck(item)
        // set content
        message.movementMethod = LinkMovementMethod.getInstance()
        message.text = item.message
        // set color
        message.setTextColor(ChatAttr.getInstance().colorTextUserMessage)
        // set dimension
        message.setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextUserMessage)
        // set bg
        ViewCompat.setBackgroundTintList(message, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundUserMessage))
    }
}