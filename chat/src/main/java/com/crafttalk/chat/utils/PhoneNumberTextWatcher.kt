package com.crafttalk.chat.utils

import android.text.Editable
import android.text.TextWatcher

class PhoneNumberTextWatcher(
    private val mask: String,
    private val countryCode: String
) : TextWatcher {

    private var isUpdating = false
    private val fullPrefix = "$countryCode "

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating || s == null) return
        val digits = s.toString().filter { it.isDigit() }

        var result = ""
        var digitIndex = 0

        for (char in mask) {
            if (char == '#') {
                if (digitIndex < digits.length) {
                    result += digits[digitIndex]
                    digitIndex++
                } else break
            } else {
                result += char
            }
        }

        val formatted = fullPrefix + result

        isUpdating = true
        s.replace(0, s.length, formatted, 0, formatted.length)
        isUpdating = false
    }
}