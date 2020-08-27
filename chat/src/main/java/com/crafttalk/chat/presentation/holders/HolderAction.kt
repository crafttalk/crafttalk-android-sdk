package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.TextView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.model.ActionItem
import com.crafttalk.chat.utils.ChatAttr

class HolderAction(
    view: View,
    private val clickHandler: (actionId: String) -> Unit
) : BaseViewHolder<ActionItem>(view) {
    private val actionText: TextView = view.findViewById(R.id.action_text)

    override fun bindTo(item: ActionItem) {
        // set content
        actionText.text = item.actionText
        actionText.tag = item.id
        // set color
        actionText.setTextColor(ChatAttr.getInstance().colorTextOperatorAction)
        // set dimension
        actionText.textSize = ChatAttr.getInstance().sizeTextOperatorAction
        // set bg
        itemView.setBackgroundResource(item.backgroundRes)
        itemView.setOnClickListener{
            clickHandler(item.id)
        }
    }
}
