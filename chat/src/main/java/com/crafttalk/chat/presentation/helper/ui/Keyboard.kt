package com.crafttalk.chat.presentation.helper.ui

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun hideSoftKeyboard(view: View?) {
    if (view != null) {
        val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}