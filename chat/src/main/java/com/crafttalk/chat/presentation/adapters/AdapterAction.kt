package com.crafttalk.chat.presentation.adapters

import android.view.ViewGroup
import com.crafttalk.chat.presentation.base.BaseAdapter
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.inflate
import com.crafttalk.chat.presentation.holders.HolderAction
import com.crafttalk.chat.presentation.model.ActionModel

class AdapterAction(
    private val scaleRatio: Float,
    private val selectAction: (actionId: String) -> Unit
) : BaseAdapter<ActionModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<out ActionModel> {
        return HolderAction(parent.inflate(viewType), scaleRatio) { actionId ->
            selectAction(actionId)
        }
    }

}
