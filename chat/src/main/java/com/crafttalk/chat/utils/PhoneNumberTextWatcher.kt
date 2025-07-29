package com.crafttalk.chat.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class PhoneNumberTextWatcher(
    private val editText: EditText,
    private val mask: String,
    private val countryCode: String
) : TextWatcher {

    private var isUpdating = false
    private val fullPrefix = "$countryCode "

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating || s == null) return

        val currentText = s.toString()

        if (!currentText.startsWith(fullPrefix)) {
            isUpdating = true
            editText.setText(fullPrefix)
            editText.setSelection(fullPrefix.length)
            isUpdating = false
            return
        }

        val input = currentText.removePrefix(fullPrefix)
        val digits = input.filter { it.isDigit() }

        var formatted = ""
        var digitIndex = 0
        for (char in mask) {
            if (char == '#') {
                if (digitIndex < digits.length) {
                    formatted += digits[digitIndex++]
                } else break
            } else {
                if (digitIndex < digits.length) {
                    formatted += char
                } else break
            }
        }

        isUpdating = true
        val finalText = fullPrefix + formatted
        editText.setText(finalText)
        editText.setSelection(finalText.length)
        isUpdating = false
    }
}