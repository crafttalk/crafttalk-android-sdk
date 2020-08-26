package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.TextView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.model.ActionItem
import com.crafttalk.chat.utils.ChatAttr

class HolderAction(
    view: View,
    private val scaleRatio: Float,
    private val clickHandler: (actionId: String) -> Unit
) : BaseViewHolder<ActionItem>(view) {
    private val actionText: TextView = view.findViewById(R.id.action_text)

    override fun bindTo(item: ActionItem) {
        // set content
        actionText.text = item.actionText
        actionText.tag = item.id
        // set color
        actionText.setTextColor(ChatAttr.mapAttr["color_text_server_action"] as Int)
        // set dimension
        actionText.textSize = (ChatAttr.mapAttr["size_server_action"] as Float) / scaleRatio
        // set bg
        itemView.setBackgroundResource(item.backgroundRes)
        itemView.setOnClickListener{
            clickHandler(item.id)
        }
    }
}
