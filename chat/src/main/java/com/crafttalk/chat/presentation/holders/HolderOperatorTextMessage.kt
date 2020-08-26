package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.adapters.AdapterAction
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageDefault
import com.crafttalk.chat.presentation.model.TextMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorTextMessage(
    view: View,
    private val scaleRatio: Float,
    private val selectAction: (actionId: String) -> Unit
) : BaseViewHolder<TextMessageItem>(view) {
    private val message: TextView = view.findViewById(R.id.server_message)
    private val listActions: RecyclerView = view.findViewById(R.id.actions_list)
    private val time: TextView = view.findViewById(R.id.time)

    override fun bindTo(item: TextMessageItem) {
        time.setTimeMessageDefault(item, scaleRatio)
        // set content
        message.movementMethod = LinkMovementMethod.getInstance()
        message.text = item.message
        Log.d("TEST", "actions - ${item.actions}")
        item.actions?.let {
            listActions.adapter = AdapterAction(
                scaleRatio,
                selectAction
            ).apply {
                this.data = it
            }
        }
        // set color
        message.setTextColor(ChatAttr.mapAttr["color_text_server_message"] as Int)
        // set bg
        message.setBackgroundColor(0)
        message.setBackgroundResource(R.drawable.background_item_simple_server_message)
        // set bg color
        ViewCompat.setBackgroundTintList(message, ColorStateList.valueOf(ChatAttr.mapAttr["color_bg_server_message"] as Int))
    }
}