package com.crafttalk.chat.di.modules

import android.content.SharedPreferences
import com.crafttalk.chat.data.local.pref.checkVisitorInPref
import com.crafttalk.chat.data.local.pref.getVisitorFromPref
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatAttr
import dagger.Module
import dagger.Provides
import java.lang.Exception
import javax.inject.Singleton

@Module
class VisitorModule
constructor(private val visitor: Visitor?) {

    @Provides
    @Singleton
    fun provideVisitor(sharedPreferences: SharedPreferences) : Visitor? {
        return when (ChatAttr.getInstance().authType) {
            AuthType.AUTH_WITH_FORM_WITHOUT_HASH -> {
                if (checkVisitorInPref(sharedPreferences)) {
                    getVisitorFromPref(sharedPreferences)
                } else {
                    null
                }
            }
            AuthType.AUTH_WITHOUT_FORM_WITHOUT_HASH -> {
                if (visitor == null) throw Exception("Visitor must not be null!")
                visitor
            }
            AuthType.AUTH_WITHOUT_FORM_WITH_HASH -> {
                if (visitor == null) throw Exception("Visitor must not be null!")
                if (visitor.hash == null) throw Exception("Visitor's hash must not be null!")
                visitor
            }
        }
    }

}