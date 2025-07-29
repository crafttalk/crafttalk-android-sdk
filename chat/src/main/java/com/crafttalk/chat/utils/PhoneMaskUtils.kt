package com.crafttalk.chat.utils

import android.content.Context
import android.telephony.TelephonyManager
import java.util.*

object PhoneMaskUtils {

    fun getPhoneMaskByCountryCode(countryCode: String): String {
        return when (countryCode) {
            "+7" -> "(###) ###-##-##"
            "+373" -> "(##) ###-###"
            "+381" -> "(##) ####-##-##"
            "+375" -> "(##) ###-##-##"
            "+994" -> "(##) ###-##-##"
            "+374" -> "(##) ###-###"
            "+998" -> "(##) ###-##-##"
            "+995" -> "(###) ##-##-##"
            else -> "(###) ###-##-##" // дефолтная маска
        }
    }


    fun getCountryCodeBySim(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val iso = tm.simCountryIso.uppercase(Locale.ROOT)
        return when (iso) {
            "RU" -> "+7"
            "MD" -> "+373"
            "RS" -> "+381"
            "BY" -> "+375"
            "AZ" -> "+994"
            "AM" -> "+374"
            "UZ" -> "+998"
            "GE" -> "+995"
            else -> "+7"
        }
    }
}