package com.crafttalk.chat.presentation.holders

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.model.ActionItem
import com.crafttalk.chat.utils.ChatAttr

class HolderAction(
    view: View,
    private val clickHandler: (actionId: String) -> Unit
) : BaseViewHolder<ActionItem>(view) {
    private val actionText: TextView? = view.findViewById(R.id.action_text)

    override fun bindTo(item: ActionItem) {
        actionText?.apply {
            setTextIsSelectable(true)
            // set content
            text = item.actionText
            tag = item.id
            // set color
            setTextColor(ChatAttr.getInstance().colorTextOperatorAction)
            // set dimension
            setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextOperatorAction)
            // set font
            ChatAttr.getInstance().resFontFamilyOperatorAction?.let {
                typeface = ResourcesCompat.getFont(context, it)
            }
        }
        // set bg
        itemView.setBackgroundResource(item.backgroundRes)
        itemView.setOnClickListener{
            clickHandler(item.id)
        }
    }
}
