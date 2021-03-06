package com.crafttalk.chat.presentation.custom_views.custom_snackbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.custom_views.custom_snackbar.binding.findSuitableParent
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
        fun make(view: View, typeFailUpload: TypeFailUpload? = null, title: String? = null, description: String? = null): WarningSnackbar {
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )

            val customView = LayoutInflater.from(view.context).inflate(
                R.layout.layout_warning_snackbar,
                parent,
                false
            ) as WarningSnackbarView

            when (typeFailUpload) {
                TypeFailUpload.LARGE -> customView.bind(R.string.warning_snackbar_large_title, R.string.warning_snackbar_large_description)
                TypeFailUpload.NOT_SUPPORT_TYPE -> customView.bind(R.string.warning_snackbar_not_support_type_title, R.string.warning_snackbar_not_support_type_description)
                TypeFailUpload.DEFAULT -> customView.bind(R.string.warning_snackbar_not_support_type_title, R.string.warning_snackbar_not_support_type_description)
                else -> customView.bind(title, description)
            }

            return WarningSnackbar(
                parent,
                customView
            )
        }
    }

}