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
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.adapters.AdapterAction
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.TextMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorTextMessage(
    view: View,
    private val selectAction: (actionId: String) -> Unit
) : BaseViewHolder<TextMessageItem>(view) {
    private val contentContainer: ViewGroup? = view.findViewById(R.id.content_container)

    private val message: TextView? = view.findViewById(R.id.server_message)
    private val listActions: RecyclerView? = view.findViewById(R.id.actions_list)
    private val author: TextView? = view.findViewById(R.id.author)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    override fun bindTo(item: TextMessageItem) {
        date?.setDate(item)
        // set content
        author?.setAuthor(item, true)
        time?.setTime(item)
        status?.setStatusMessage(item)
        message?.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = item.message
            listActions?.apply {
                if (item.actions == null) {
                    visibility = View.GONE
                } else {
                    adapter = AdapterAction(selectAction).apply {
                        this.data = item.actions
                    }
                    visibility = View.VISIBLE
                }
            }
            // set color
            setTextColor(ChatAttr.getInstance().colorTextOperatorMessage)
            // set dimension
            setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextOperatorMessage)
            // set font
            ChatAttr.getInstance().resFontFamilyOperatorMessage?.let {
                typeface = ResourcesCompat.getFont(context, it)
            }
        }
        // set bg
        contentContainer?.apply {
            setBackgroundResource(R.drawable.background_item_simple_server_message)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))
        }
    }
}