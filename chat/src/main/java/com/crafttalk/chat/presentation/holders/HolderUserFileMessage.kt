package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.setFileIcon
import com.crafttalk.chat.presentation.helper.extensions.setTimeMessageWithCheck
import com.crafttalk.chat.presentation.model.FileMessageItem

class HolderUserFileMessage(
    val view: View,
    private val clickHandler: (fileUrl: String) -> Unit
) : BaseViewHolder<FileMessageItem>(view), View.OnClickListener {
    private val fileIcon: ImageView = view.findViewById(R.id.user_file)
    private val time: TextView = view.findViewById(R.id.time)
    private var fileUrl: String? = null

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        fileUrl?.let{
            clickHandler(it)
        }
    }

    override fun bindTo(item: FileMessageItem) {
        fileUrl = item.document.url
        fileIcon.setFileIcon()
        time.setTimeMessageWithCheck(item)
    }

}