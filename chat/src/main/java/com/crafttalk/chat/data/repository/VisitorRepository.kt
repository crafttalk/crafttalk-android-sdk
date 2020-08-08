package com.crafttalk.chat.data.repository

import android.content.SharedPreferences
import com.crafttalk.chat.data.remote.socket_service.SocketApi
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IVisitorRepository

class VisitorRepository constructor(
    private val pref: SharedPreferences,
    private val socketApi: SocketApi
) : IVisitorRepository {
    override fun logIn(visitor: Visitor, successAuth: () -> Unit, failAuth: (ex: Throwable) -> Unit) {
        socketApi.setVisitor(visitor, successAuth, failAuth)
    }

    override fun logOut() {
        TODO("Not yet implemented")
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