package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.presentation.adapters.AdapterAction
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.UnionMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorUnionMessage(
    view: View,
    private val selectAction: (actionId: String) -> Unit,
    private val updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    private val clickGifHandler: (gifUrl: String) -> Unit,
    private val clickImageHandler: (imageUrl: String) -> Unit,
    private val clickDocumentHandler: (fileUrl: String) -> Unit
) : BaseViewHolder<UnionMessageItem>(view), View.OnClickListener {
    private val message: TextView = view.findViewById(R.id.server_message)
    private val listActions: RecyclerView = view.findViewById(R.id.actions_list)
    private val time: TextView = view.findViewById(R.id.time)
    private val fileIcon: ImageView = view.findViewById(R.id.server_file)
    private val media: ImageView = view.findViewById(R.id.server_media)

    private var fileUrl: String? = null
    private var fileType: TypeFile? = null

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        fileUrl?.let{ url ->
            when (fileType) {
                TypeFile.FILE -> {
                    clickDocumentHandler(url)
                }
                TypeFile.IMAGE -> {
                    clickImageHandler(url)
                }
                TypeFile.GIF -> {
                    clickGifHandler(url)
                }
                else -> {}
            }
        }
    }

    override fun bindTo(item: UnionMessageItem) {
        time.setTimeMessageDefault(item, true)
        // set content
        message.movementMethod = LinkMovementMethod.getInstance()
        message.text = item.message
        item.actions?.let {
            listActions.adapter = AdapterAction(
                selectAction
            ).apply {
                this.data = it
            }
        }
        // set color
        message.setTextColor(ChatAttr.getInstance().colorTextOperatorMessage)
        // set dimension
        message.textSize = ChatAttr.getInstance().sizeTextOperatorMessage
        // set bg
        message.setBackgroundColor(0)
        message.setBackgroundResource(R.drawable.background_item_simple_server_message)
        // set bg color
        ViewCompat.setBackgroundTintList(message, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))

        when (fileType) {
            TypeFile.FILE -> {
                media.visibility = View.GONE
                fileIcon.visibility = View.VISIBLE
                fileIcon.setFileIcon()
            }
            TypeFile.IMAGE -> {
                fileIcon.visibility = View.GONE
                media.settingMediaFile(item.file, fileUrl)
                media.loadMediaFile(item.idKey, item.file, updateData)
            }
            TypeFile.GIF -> {
                fileIcon.visibility = View.GONE
                media.settingMediaFile(item.file, fileUrl)
                media.loadMediaFile(item.idKey, item.file, updateData, true)
            }
        }
        fileUrl = item.file.url
        fileType = item.file.type
    }

}