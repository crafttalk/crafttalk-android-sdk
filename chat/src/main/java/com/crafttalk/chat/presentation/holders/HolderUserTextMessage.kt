package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.setAuthor
import com.crafttalk.chat.presentation.helper.extensions.setDate
import com.crafttalk.chat.presentation.helper.extensions.setStatusMessage
import com.crafttalk.chat.presentation.helper.extensions.setTime
import com.crafttalk.chat.presentation.model.TextMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderUserTextMessage(
    view: View
) : BaseViewHolder<TextMessageItem>(view) {
    private val contentContainer: ViewGroup? = view.findViewById(R.id.content_container)

    private val message: TextView? = view.findViewById(R.id.user_message)
    private val author: TextView? = view.findViewById(R.id.author)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    override fun bindTo(item: TextMessageItem) {
        date?.setDate(item)
        // set content
        author?.setAuthor(item)
        time?.setTime(item)
        status?.setStatusMessage(item)
        message?.apply {
            // set behavior
            setTextIsSelectable(true)
            movementMethod = LinkMovementMethod.getInstance()
            // set width item
            ChatAttr.getInstance().widthItemUserTextMessage?.let {
                maxWidth = it
            }
            // set content
            text = item.message
            // set color
            setTextColor(ChatAttr.getInstance().colorTextUserMessage)
            // set dimension
            setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextUserMessage)
            // set font
            ChatAttr.getInstance().resFontFamilyUserMessage?.let {
                typeface = ResourcesCompat.getFont(context, it)
            }
        }
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgUserMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundUserMessage))
        }
    }
}