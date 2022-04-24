package com.crafttalk.chat.presentation.adapters

import android.view.ViewGroup
import com.crafttalk.chat.presentation.base.BaseAdapter
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.inflate
import com.crafttalk.chat.presentation.holders.HolderButtons
import com.crafttalk.chat.presentation.model.ButtonsListItem

class AdapterButton(
    private val messageId: String,
    private val hasSelectedButton: Boolean,
    private val selectButton: (messageId: String, actionId: String, buttonId: String) -> Unit
) : BaseAdapter<ButtonsListItem>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ButtonsListItem> {
        return HolderButtons(parent.inflate(viewType), hasSelectedButton) { actionId, buttonId ->
            selectButton(messageId, actionId, buttonId)
        }
    }

}
