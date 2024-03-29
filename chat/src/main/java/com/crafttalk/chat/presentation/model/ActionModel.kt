package com.crafttalk.chat.presentation.model

import androidx.annotation.DrawableRes
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseItem

sealed class ActionModel(
    open val id: String,
    open val actionText: String,
    open val isSelected: Boolean
) : BaseItem()

data class ActionItem(
    override val id: String,
    override val actionText: String,
    override val isSelected: Boolean,
    @DrawableRes val backgroundRes: Int
) : ActionModel(id, actionText, isSelected) {
    override fun getLayout() = R.layout.com_crafttalk_chat_item_action

    override fun <T : BaseItem> isSame(item: T): Boolean {
        return item is ActionItem && item.id == id
    }
}