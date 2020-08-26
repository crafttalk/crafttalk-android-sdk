package com.crafttalk.chat.di.modules

import android.content.SharedPreferences
import com.crafttalk.chat.data.local.pref.checkVisitorInPref
import com.crafttalk.chat.data.local.pref.getVisitorFromPref
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.utils.ChatAttr
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class VisitorModule
constructor(private val visitor: Visitor?) {

    @Provides
    @Singleton
    fun provideVisitor(sharedPreferences: SharedPreferences) : Visitor? {
        return if (!(ChatAttr.mapAttr["auth_with_form"] as Boolean) && visitor != null) {
            visitor
        }
        else {
            if (checkVisitorInPref(sharedPreferences)) {
                getVisitorFromPref(sharedPreferences)
            }
            null
        }
    }

}