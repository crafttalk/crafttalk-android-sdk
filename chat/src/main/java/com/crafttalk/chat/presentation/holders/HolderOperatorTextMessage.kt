package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.adapters.AdapterAction
import com.crafttalk.chat.presentation.adapters.AdapterButton
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.helper.ui.bindRepliedMessage
import com.crafttalk.chat.presentation.model.TextMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorTextMessage(
    view: View,
    private val selectReplyMessage: (messageId: String) -> Unit,
    private val updateData: (id: String, height: Int, width: Int) -> Unit,
    private val selectAction: (messageId: String, actionId: String) -> Unit,
    private val selectButton: (messageId: String, actionId: String, buttonId: String) -> Unit
) : BaseViewHolder<TextMessageItem>(view), View.OnClickListener {
    private val contentContainer: View? = view.findViewById(R.id.content_container)

    private val message: TextView? = view.findViewById(R.id.server_message)
    private val repliedMessageContainer: ViewGroup? = view.findViewById(R.id.replied_message_container)
    private val repliedBarrier: View? = view.findViewById(R.id.replied_barrier)
    private val repliedMessage: TextView? = view.findViewById(R.id.replied_message)
    private val repliedFileInfo: ViewGroup? = view.findViewById(R.id.replied_file_info)
    private val repliedFileIcon: ImageView? = view.findViewById(R.id.file_icon)
    private val repliedProgressDownload: ProgressBar? = view.findViewById(R.id.progress_download)
    private val repliedFileName: TextView? = view.findViewById(R.id.file_name)
    private val repliedFileSize: TextView? = view.findViewById(R.id.file_size)
    private val repliedMediaFile: ImageView? = view.findViewById(R.id.replied_media_file)
    private val repliedMediaFileWarning: ViewGroup? = view.findViewById(R.id.replied_media_file_warning)
    private val listActions: RecyclerView? = view.findViewById(R.id.actions_list)
    private val listButtons: RecyclerView? = view.findViewById(R.id.buttons_list)
    private val authorName: TextView? = view.findViewById(R.id.author_name)
    private val authorPreview: ImageView? = view.findViewById(R.id.author_preview)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    private var replyMessageId: String? = null

    init {
        repliedMessageContainer?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.replied_message_container -> replyMessageId?.run(selectReplyMessage)
        }
    }

    override fun bindTo(item: TextMessageItem) {
        replyMessageId = item.repliedMessage?.id

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
                listButtons?.apply {
                    if (item.buttons == null) {
                        visibility = View.GONE
                    } else {
                        adapter = AdapterButton(item.id, item.hasSelectedButton, selectButton).apply {
                            this.data = item.buttons
                        }
                        visibility = View.VISIBLE
                    }
                }
            }
        )
        bindRepliedMessage(
            itemId = item.id,
            itemRepliedMessage = item.repliedMessage,
            isUserMessage = false,
            repliedMessageContainer = repliedMessageContainer,
            repliedBarrier = repliedBarrier,
            repliedMessage = repliedMessage,
            repliedFileInfo = repliedFileInfo,
            repliedFileIcon = repliedFileIcon,
            repliedProgressDownload = repliedProgressDownload,
            repliedFileName = repliedFileName,
            repliedFileSize = repliedFileSize,
            repliedMediaFile = repliedMediaFile,
            repliedMediaFileWarning = repliedMediaFileWarning,
            updateData = updateData
        )
        // set bg
        contentContainer?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgOperatorMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))
        }
    }
}