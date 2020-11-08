package com.crafttalk.chat.data.repository

import android.content.SharedPreferences
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.ICacheRepository
import javax.inject.Inject

class CacheRepository
@Inject constructor(
    private val pref: SharedPreferences
): ICacheRepository {

    override fun saveVisitor(visitor: Visitor) {
        val prefEditor = pref.edit()
        prefEditor.putString("Visitor", visitor.getJsonObject().toString())
        prefEditor.putBoolean("isVisitor", true)
        prefEditor.apply()
    }

    override fun deleteVisitor(visitor: Visitor) {
        val prefEditor = pref.edit()
        prefEditor.remove("Visitor")
        prefEditor.putBoolean("isVisitor", false)
        prefEditor.apply()
    }

}