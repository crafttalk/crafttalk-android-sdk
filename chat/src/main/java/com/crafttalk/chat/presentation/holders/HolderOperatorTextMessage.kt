package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.adapters.AdapterAction
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.setDate
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageDefault
import com.crafttalk.chat.presentation.model.TextMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorTextMessage(
    view: View,
    private val selectAction: (actionId: String) -> Unit
) : BaseViewHolder<TextMessageItem>(view) {
    private val message: TextView = view.findViewById(R.id.server_message)
    private val listActions: RecyclerView = view.findViewById(R.id.actions_list)
    private val time: TextView = view.findViewById(R.id.time)
    private val date: TextView = view.findViewById(R.id.date)

    override fun bindTo(item: TextMessageItem) {
        date.setDate(item)
        time.setTimeMessageDefault(item, true)
        // set content
        message.movementMethod = LinkMovementMethod.getInstance()
        message.text = item.message
        if (item.actions == null) {
            listActions.visibility = View.GONE
        } else {
            listActions.adapter = AdapterAction(selectAction).apply {
                this.data = item.actions
            }
            listActions.visibility = View.VISIBLE
        }
        // set color
        message.setTextColor(ChatAttr.getInstance().colorTextOperatorMessage)
        // set dimension
        message.setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextOperatorMessage)
        // set bg color
        ViewCompat.setBackgroundTintList(message, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))
    }
}