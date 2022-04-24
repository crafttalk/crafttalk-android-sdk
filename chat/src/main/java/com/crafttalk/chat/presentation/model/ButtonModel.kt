package com.crafttalk.chat.presentation.model

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.message.NetworkButtonOperation
import com.crafttalk.chat.presentation.base.BaseItem

data class ButtonsListItem(
    val buttons: List<ButtonItem>
) : BaseItem() {
    override fun getLayout() = R.layout.com_crafttalk_chat_item_buttons

    override fun <T : BaseItem> isSame(item: T): Boolean {
        return item is ButtonsListItem && item.buttons.hashCode() == hashCode()
    }
}

data class ButtonItem(
    val id: String,
    val text: String,
    val action: String,
    val typeOperation: NetworkButtonOperation,
    val isSelected: Boolean,
    val imageUrl: String?,
    val imageEmoji: String?,
    @ColorInt val textColor: Int,
    @DrawableRes val backgroundRes: Int,
    val width: Int,
    val marginTop: Int,
    val marginBottom: Int,
    val marginStart: Int,
    val marginEnd: Int
)