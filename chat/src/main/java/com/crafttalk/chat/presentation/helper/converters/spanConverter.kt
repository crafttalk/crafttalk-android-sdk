package com.crafttalk.chat.presentation.helper.converters

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import androidx.core.content.ContextCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.tags.*

fun String.convertToSpannableString(spanStructureList: List<Tag>, context: Context): SpannableString {
    val result = SpannableString(this)
    spanStructureList.forEach {
        when (it) {
            is StrongTag -> result.setSpan(StyleSpan(Typeface.BOLD), it.pointStart, it.pointEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            is ItalicTag -> result.setSpan(StyleSpan(Typeface.ITALIC), it.pointStart, it.pointEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            is UrlTag -> result.setSpan(URLSpan(it.url), it.pointStart, it.pointEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            is ImageTag -> {
                // load bitmap use it.url
//                ...
//                result.setSpan(
//                    ImageSpan(context, bitmap, DynamicDrawableSpan.ALIGN_BASELINE),
//                    it.pointStart,
//                    it.pointEnd + 1,
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                )
            }
            is ItemListTag -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    result.setSpan(BulletSpan(10, ContextCompat.getColor(context, R.color.default_color_text_user_message), 6), it.pointStart, it.pointEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    result.setSpan(BulletSpan(), it.pointStart, it.pointEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            is HostListTag -> {
                result.setSpan(LeadingMarginSpan.Standard(80), it.pointStart, it.pointEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

        }
    }
    return result
}


