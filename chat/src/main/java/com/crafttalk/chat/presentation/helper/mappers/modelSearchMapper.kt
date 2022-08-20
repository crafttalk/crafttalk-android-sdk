package com.crafttalk.chat.presentation.helper.mappers

import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import com.crafttalk.chat.domain.interactors.SearchItem
import com.crafttalk.chat.presentation.model.*
import com.crafttalk.chat.utils.ChatAttr
import kotlin.math.abs

fun messageSearchMapper(messageModel: MessageModel, searchText: String?, currentSearchItem: SearchItem?): MessageModel {
    currentSearchItem ?: return messageModel
    searchText ?: return messageModel
    when (messageModel) {
        is TextMessageItem -> selectText(messageModel.id, messageModel.timestamp, messageModel.message, searchText, currentSearchItem)
        is InfoMessageItem -> selectText(messageModel.id, messageModel.timestamp, messageModel.message, searchText, currentSearchItem)
        is WidgetMessageItem -> messageModel.message?.let { selectText(messageModel.id, messageModel.timestamp, it, searchText, currentSearchItem) }
        is UnionMessageItem -> selectText(messageModel.id, messageModel.timestamp, messageModel.message, searchText, currentSearchItem)
        else -> {}
    }
    return messageModel
}

private fun selectText(
    messageId: String,
    messageTimestamp: Long,
    spannableString: SpannableString,
    searchText: String,
    currentSearchItem: SearchItem
): SpannableString {
    var indexStart = spannableString.toString().indexOf(searchText)
    var numberCoincidences = 1
    if (indexStart != -1) {
        spannableString.setSpan(
            BackgroundColorSpan(
                if (
                    numberCoincidences == currentSearchItem.positionMatchInMsg &&
                    ((currentSearchItem.id != null && messageId == currentSearchItem.id) ||
                            (abs(messageTimestamp - currentSearchItem.timestamp) <= 50))
                ) ChatAttr.getInstance().colorCurrentSelectSearchText
                else ChatAttr.getInstance().colorSelectSearchText
            ),
            indexStart,
            indexStart + searchText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    while (indexStart != -1) {
        indexStart = spannableString.toString().indexOf(searchText, indexStart + 1)
        numberCoincidences++

        if (indexStart != -1) {
            spannableString.setSpan(
                BackgroundColorSpan(
                    if (
                        numberCoincidences == currentSearchItem.positionMatchInMsg &&
                        ((currentSearchItem.id != null && messageId == currentSearchItem.id) ||
                                (abs(messageTimestamp - currentSearchItem.timestamp) <= 50))
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