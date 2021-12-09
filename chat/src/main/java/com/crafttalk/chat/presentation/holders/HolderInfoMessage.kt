package com.crafttalk.chat.presentation.holders

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.setMessageText
import com.crafttalk.chat.presentation.model.InfoMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderInfoMessage(
    view: View
) : BaseViewHolder<InfoMessageItem>(view) {

    private val message: TextView? = view.findViewById(R.id.info_message)

    @SuppressLint("StringFormatInvalid")
    override fun bindTo(item: InfoMessageItem) {
        // set content
        message?.setMessageText(
            textMessage = item.message,
            colorTextMessage = ChatAttr.getInstance().colorTextInfo,
            sizeTextMessage = ChatAttr.getInstance().sizeTextOperatorMessage
        )
    }
}