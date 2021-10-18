package com.crafttalk.chat.presentation.holders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.TransferMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderTransferMessage(
    view: View
) : BaseViewHolder<TransferMessageItem>(view) {
    private val contentContainer: View? = view.findViewById(R.id.content_container)

    private val message: TextView? = view.findViewById(R.id.server_message)
    private val authorName: TextView? = view.findViewById(R.id.author_name)
    private val authorPreview: ImageView? = view.findViewById(R.id.author_preview)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    @SuppressLint("StringFormatInvalid")
    override fun bindTo(item: TransferMessageItem) {
        date?.setDate(item)
        // set content
        authorName?.setAuthor(item)
        authorPreview?.setAuthorIcon(item.authorPreview)
        time?.setTime(item)
        status?.setStatusMessage(item)
        message?.setMessageText(
            textMessageRes = R.string.com_crafttalk_chat_message_join,
            textMessageResArgs = listOf(item.authorName),
            maxWidthTextMessage = ChatAttr.getInstance().widthItemOperatorTextMessage,
            colorTextMessage = ChatAttr.getInstance().colorTextOperatorMessage,
            sizeTextMessage = ChatAttr.getInstance().sizeTextOperatorMessage,
            resFontFamilyMessage = ChatAttr.getInstance().resFontFamilyOperatorMessage,
            isSelectableText = true
        )
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgOperatorMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))
        }
    }
}