package com.crafttalk.chat.presentation.helper.mappers

import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import com.crafttalk.chat.domain.interactors.SearchItem
import com.crafttalk.chat.presentation.model.*
import com.crafttalk.chat.utils.ChatAttr

fun messageSearchMapper(
    messageModel: MessageModel,
    searchText: String,
    currentSearchItem: SearchItem,
    allSearchedItems: List<SearchItem>
): MessageModel {
    if (allSearchedItems.find { it.id == messageModel.id } == null) return messageModel
    when (messageModel) {
        is TextMessageItem -> selectText(messageModel.id, messageModel.message, searchText, currentSearchItem)
        is InfoMessageItem -> selectText(messageModel.id, messageModel.message, searchText, currentSearchItem)
        is WidgetMessageItem -> messageModel.message?.let { selectText(messageModel.id, it, searchText, currentSearchItem) }
        is UnionMessageItem -> selectText(messageModel.id, messageModel.message, searchText, currentSearchItem)
        is FileMessageItem -> selectText(messageModel.id, messageModel.document.name, searchText, currentSearchItem)
        else -> {}
    }
    return messageModel
}

private fun selectText(
    messageId: String,
    spannableString: SpannableString,
    searchText: String,
    currentSearchItem: SearchItem
): SpannableString {
    var indexStart = spannableString.toString().indexOf(searchText, ignoreCase = true)
    var numberCoincidences = 1
    if (indexStart != -1) {
        spannableString.setSpan(
            BackgroundColorSpan(
                if (
                    numberCoincidences == currentSearchItem.positionMatchInMsg &&
                    (currentSearchItem.id != null && messageId == currentSearchItem.id)
                ) ChatAttr.getInstance().colorCurrentSelectSearchText
                else ChatAttr.getInstance().colorSelectSearchText
            ),
            indexStart,
            indexStart + searchText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    while (indexStart != -1) {
        indexStart = spannableString.toString().indexOf(searchText, indexStart + 1, ignoreCase = true)
        numberCoincidences++

        if (indexStart != -1) {
            spannableString.setSpan(
                BackgroundColorSpan(
                    if (
                        numberCoincidences == currentSearchItem.positionMatchInMsg &&
                        (currentSearchItem.id != null && messageId == currentSearchItem.id)
                    ) ChatAttr.getInstance().colorCurrentSelectSearchText
                    else ChatAttr.getInstance().colorSelectSearchText
                ),
                indexStart,
                indexStart + searchText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    return spannableString
}