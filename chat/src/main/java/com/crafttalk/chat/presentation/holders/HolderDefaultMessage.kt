package com.crafttalk.chat.presentation.holders

import android.view.View
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.model.TextMessageItem

class HolderDefaultMessage(
    view: View
) : BaseViewHolder<TextMessageItem>(view) {
    override fun bindTo(item: TextMessageItem) {}
}