package com.crafttalk.chat.data.repository

import android.content.SharedPreferences
import com.crafttalk.chat.data.local.pref.checkVisitorInPref
import com.crafttalk.chat.data.local.pref.getVisitorFromPref
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IVisitorRepository
import javax.inject.Inject

class VisitorRepository
@Inject constructor(
    private val pref: SharedPreferences
) : IVisitorRepository {

    private var visitor: Visitor? = null

    override fun getVisitorFromClient(): Visitor? = visitor

    override fun getVisitorFromSharedPreferences(): Visitor? {
        return if (checkVisitorInPref(pref)) {
            getVisitorFromPref(pref)
        } else {
            null
        }
    }

    override fun setVisitorFromClient(visitor: Visitor?) {
        this.visitor = visitor
    }

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