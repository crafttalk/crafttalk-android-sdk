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
import com.crafttalk.chat.utils.ChatParams
import com.crafttalk.chat.utils.ConstantsUtils.TAG_SPAN_CONVERTER
import kotlin.Exception

fun String.convertToSpannableString(authorIsUser: Boolean, spanStructureList: List<Tag>, context: Context): SpannableString {
    var tempString = this
    var bias = 0
    Log.d(TAG_SPAN_CONVERTER,"start add span to message -> $this")
    spanStructureList.forEach {
        try {
            when (it){
                is OrderedListTag -> {
                    bias = 0
                    var positionOfElementInList = 1
                for (element in spanStructureList){
                    if (it.pointStart < element.pointStart && it.pointEnd >= element.pointEnd ){
                        if (element.name == "li") {
                            var firstPartOfString =
                                tempString.subSequence(0, element.pointStart + bias)
                            var lastPartOfString =
                                tempString.subSequence(element.pointStart + bias, tempString.length)
                            tempString =
                                "$firstPartOfString$positionOfElementInList) $lastPartOfString"
                            positionOfElementInList++
                            bias += 3
                            }
                        else {
                                element.pointStart = element.pointStart + bias
                                element.pointEnd = element.pointEnd + bias
                            }
                        }
                    }
                    for (i in spanStructureList){
                        if (it.pointEnd <= i.pointStart){
                            i.pointEnd += bias
                            i.pointStart += bias
                        }
                    }
                }
            }
        }
        catch (e:Exception){
            Log.e ("",e.toString())
        }
    }

    val result = SpannableString(tempString)
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
                                Log.d("CTALK_LOG_CONVERTER", "Fail onClick ${ ChatParams.urlChatScheme}://${ChatParams.urlChatHost}${it.url}};")
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
                }
                is HostListTag -> {
                    result.setSpan(
                        LeadingMarginSpan.Standard(10),
                        it.pointStart,
                        it.pointEnd + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    for (element in spanStructureList) {
                        if (element.name == "li") {
                            if (it.pointStart < element.pointStart && it.pointEnd >= element.pointEnd) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    result.setSpan(
                                        BulletSpan(
                                            2,
                                            ChatAttr.getInstance().colorTextOperatorMessage,
                                            6
                                        ),
                                        element.pointStart,
                                        element.pointEnd + 1,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                } else {
                                    result.setSpan(
                                        BulletSpan(),
                                        element.pointStart,
                                        element.pointEnd + 1,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                            }
                        }
                    }

                }
                is OrderedListTag -> {
                    for (element in spanStructureList){
                        if (it.pointStart < element.pointStart && it.pointEnd >= element.pointEnd ){

                        }
                    }

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
            Log.e("CTALK_ERROR_INCONVERTER", "msg: ${this}, authorIsUser: ${authorIsUser}; spanStructureList: ${spanStructureList};")
        }
    }
    Log.d(TAG_SPAN_CONVERTER,"finish add span to message -> $result")
    return result
}


