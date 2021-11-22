package com.crafttalk.chat.presentation.custom_views.custom_snackbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.custom_views.custom_snackbar.binding.findSuitableParent
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.TypeFailUpload
import com.google.android.material.snackbar.BaseTransientBottomBar

class WarningSnackbar(
    parent: ViewGroup,
    content: WarningSnackbarView
) : BaseTransientBottomBar<WarningSnackbar>(parent, content, content) {

    init {
        getView().setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        getView().setPadding(0, 0, 0, 0)
    }

    companion object {
        fun make(
            view: View,
            parentViewGroup: ViewGroup? = null,
            typeFailUpload: TypeFailUpload? = null,
            title: String? = null,
            description: String? = null,
            @DrawableRes iconRes: Int = R.drawable.com_crafttalk_chat_ic_warning,
            @ColorRes textColor: Int = ChatAttr.getInstance().colorFailDownloadFileWarning,
            @ColorRes backgroundColor: Int = ChatAttr.getInstance().backgroundFailDownloadFileWarning
        ): WarningSnackbar? {
            val parent = parentViewGroup ?: view.findSuitableParent() ?: return null

            val customView = LayoutInflater.from(view.context).inflate(
                R.layout.com_crafttalk_chat_layout_warning_snackbar,
                parent,
                false
            ) as WarningSnackbarView

            when (typeFailUpload) {
                TypeFailUpload.LARGE -> customView.bind(
                    R.string.com_crafttalk_chat_warning_snackbar_large_title,
                    R.string.com_crafttalk_chat_warning_snackbar_large_description,
                    iconRes,
                    textColor,
                    backgroundColor
                )
                TypeFailUpload.NOT_SUPPORT_TYPE, TypeFailUpload.DEFAULT -> customView.bind(
                    R.string.com_crafttalk_chat_warning_snackbar_not_support_type_title,
                    R.string.com_crafttalk_chat_warning_snackbar_not_support_type_description,
                    iconRes,
                    textColor,
                    backgroundColor
                )
                TypeFailUpload.TIMEOUT -> customView.bind(
                    R.string.com_crafttalk_chat_warning_snackbar_not_support_type_title,
                    null,
                    iconRes,
                    textColor,
                    backgroundColor
                )
                else -> customView.bind(
                    title,
                    description,
                    iconRes,
                    textColor,
                    backgroundColor
                )
            }

            return WarningSnackbar(
                parent,
                customView
            )
        }
    }

}