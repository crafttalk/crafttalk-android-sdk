package com.crafttalk.chat.presentation.adapters

import android.view.ViewGroup
import com.crafttalk.chat.presentation.base.BaseAdapter
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.inflate
import com.crafttalk.chat.presentation.holders.HolderAction
import com.crafttalk.chat.presentation.model.ActionModel

class AdapterAction(
    private val messageId: String,
    private val hasSelectedAction: Boolean,
    private val selectAction: (messageId: String, actionId: String) -> Unit
) : BaseAdapter<ActionModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<out ActionModel> {
        return HolderAction(parent.inflate(viewType), hasSelectedAction) { actionId ->
            selectAction(messageId, actionId)
        }
    }

}
