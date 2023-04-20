package com.crafttalk.chat.data.repository

import android.content.SharedPreferences
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IVisitorRepository
import javax.inject.Inject

class VisitorRepository
@Inject constructor(
    private val pref: SharedPreferences
) : IVisitorRepository {

    private var visitor: Visitor? = null

    override fun getVisitorFromClient(): Visitor? = visitor

    override fun setVisitorFromClient(visitor: Visitor?) {
        this.visitor = visitor
    }

    override fun getVisitorFromSharedPreferences(): Visitor? {
        return if (pref.getBoolean(FIELD_IS_VISITOR, false)) {
            val json = pref.getString(FIELD_VISITOR, "")
            Visitor.getVisitorFromJson(json)
        } else {
            null
        }
    }

    override fun saveVisitor(visitor: Visitor) {
        val prefEditor = pref.edit()
        prefEditor.putString(FIELD_VISITOR, visitor.getJsonObject().toString())
        prefEditor.putBoolean(FIELD_IS_VISITOR, true)
        prefEditor.apply()
    }

    override fun deleteVisitor() {
        val prefEditor = pref.edit()
        prefEditor.remove(FIELD_VISITOR)
        prefEditor.putBoolean(FIELD_IS_VISITOR, false)
        prefEditor.apply()
    }

    companion object {
        private const val FIELD_VISITOR = "visitor"
        private const val FIELD_IS_VISITOR = "isVisitor"
    }

}