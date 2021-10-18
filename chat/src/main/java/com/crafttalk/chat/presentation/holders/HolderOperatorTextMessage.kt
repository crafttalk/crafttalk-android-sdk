package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
    private val selectAction: (messageId: String, actionId: String) -> Unit
) : BaseViewHolder<TextMessageItem>(view) {
    private val contentContainer: View? = view.findViewById(R.id.content_container)

    private val message: TextView? = view.findViewById(R.id.server_message)
    private val listActions: RecyclerView? = view.findViewById(R.id.actions_list)
    private val authorName: TextView? = view.findViewById(R.id.author_name)
    private val authorPreview: ImageView? = view.findViewById(R.id.author_preview)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    override fun bindTo(item: TextMessageItem) {
        date?.setDate(item)
        // set content
        authorName?.setAuthor(item)
        authorPreview?.setAuthorIcon(item.authorPreview)
        time?.setTime(item)
        status?.setStatusMessage(item)
        message?.setMessageText(
            textMessage = item.message,
            maxWidthTextMessage = ChatAttr.getInstance().widthItemOperatorTextMessage,
            colorTextMessage = ChatAttr.getInstance().colorTextOperatorMessage,
            colorTextLinkMessage = ChatAttr.getInstance().colorTextLinkOperatorMessage,
            sizeTextMessage = ChatAttr.getInstance().sizeTextOperatorMessage,
            resFontFamilyMessage = ChatAttr.getInstance().resFontFamilyOperatorMessage,
            isClickableLink = true,
            isSelectableText = true,
            bindContinue = {
                listActions?.apply {
                    if (item.actions == null) {
                        visibility = View.GONE
                    } else {
                        adapter = AdapterAction(item.id, item.hasSelectedAction, selectAction).apply {
                            this.data = item.actions
                        }
                        visibility = View.VISIBLE
                    }
                }
            }
        )
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgOperatorMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))
        }
    }
}