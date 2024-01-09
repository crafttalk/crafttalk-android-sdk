package com.crafttalk.chat.presentation.helper.converters

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.*
import android.util.Log
import android.view.View
import com.crafttalk.chat.domain.entity.tags.*
import com.crafttalk.chat.utils.ChatAttr
import java.lang.Exception

fun String.convertToSpannableString(authorIsUser: Boolean, spanStructureList: List<Tag>, context: Context): SpannableString {
    val result = SpannableString(this)
    spanStructureList.forEach {
        try {
            when (it) {
                is StrikeTag -> result.setSpan(
                    StrikethroughSpan(),
                    it.pointStart,
                    it.pointEnd + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                is StrongTag, is BTag -> result.setSpan(
                    StyleSpan(Typeface.BOLD),
                    it.pointStart,
                    it.pointEnd + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                is ItalicTag, is EmTag -> result.setSpan(
                    StyleSpan(Typeface.ITALIC),
                    it.pointStart,
                    it.pointEnd + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                is UrlTag -> {
                    result.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(it.url.replaceFirstChar { it.lowercase() })
                            )
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            try {
                                context.startActivity(intent)
                            } catch (ex: Exception) {
                                Log.d("CTALK_LOG_CONVERTER", "Fail onClick ${it.url};")
                            }
                        }
                    }, it.pointStart, it.pointEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    result.setSpan(
                        ForegroundColorSpan(
                            if (authorIsUser) ChatAttr.getInstance().colorTextLinkUserMessage
                            else ChatAttr.getInstance().colorTextLinkOperatorMessage
                        ),
                        it.pointStart,
                        it.pointEnd + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
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
                        result.setSpan(
                            BulletSpan(
                                10,
                                ChatAttr.getInstance().colorTextOperatorMessage,
                                6
                            ), it.pointStart, it.pointEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        result.setSpan(
                            BulletSpan(),
                            it.pointStart,
                            it.pointEnd + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                is HostListTag -> {
                    result.setSpan(
                        LeadingMarginSpan.Standard(80),
                        it.pointStart,
                        it.pointEnd + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                is PhoneTag -> {
                    result.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.phone}"))
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        }
                    }, it.pointStart, it.pointEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    result.setSpan(
                        ForegroundColorSpan(
                            if (authorIsUser) ChatAttr.getInstance().colorTextPhoneUserMessage
                            else ChatAttr.getInstance().colorTextPhoneOperatorMessage
                        ),
                        it.pointStart,
                        it.pointEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        } catch(ex: IndexOutOfBoundsException) {
            Log.e("CTALK_ERROR_IN_CONVERTER", "msg: ${this}, authorIsUser: ${authorIsUser}; spanStructureList: ${spanStructureList};")
        }
    }
    return result
}