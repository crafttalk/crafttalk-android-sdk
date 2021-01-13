package com.crafttalk.chat.di.modules.chat

import android.content.SharedPreferences
import com.crafttalk.chat.data.local.pref.checkVisitorInPref
import com.crafttalk.chat.data.local.pref.getVisitorFromPref
import com.crafttalk.chat.di.ChatScope
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams.authType
import dagger.Module
import dagger.Provides
import java.lang.Exception

@Module
class VisitorModule
constructor(private val visitor: Visitor?) {

    @Provides
    @ChatScope
    fun provideVisitor(sharedPreferences: SharedPreferences) : Visitor? {
        return when (authType) {
            AuthType.AUTH_WITH_FORM -> {
                if (checkVisitorInPref(sharedPreferences)) {
                    getVisitorFromPref(sharedPreferences)
                } else {
                    null
                }
            }
            AuthType.AUTH_WITHOUT_FORM -> {
                if (visitor == null) throw Exception("Visitor must not be null!")
                visitor
            }
            else -> throw Exception("Not found type auth!")
        }
    }

}