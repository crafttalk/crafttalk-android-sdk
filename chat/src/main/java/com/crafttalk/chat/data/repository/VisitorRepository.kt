package com.crafttalk.chat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IVisitorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class VisitorRepository
@Inject constructor(
    private val pref: DataStore<Preferences>
) : IVisitorRepository {

    private var visitor: Visitor? = null

    override fun getVisitorFromClient(): Visitor? = visitor

    override fun setVisitorFromClient(visitor: Visitor?) {
        this.visitor = visitor
    }


    // Flow для наблюдения за Visitor
    override val visitorFlow: Flow<Visitor?> = pref.data.map { preferences ->
        if (preferences[FIELD_IS_VISITOR] == true) {
            preferences[FIELD_VISITOR]?.let { Visitor.getVisitorFromJson(it) }
        } else {
            null
        }
    }

    // Получить Visitor один раз (suspend)
    override fun getVisitorFromDataStore(): Visitor? = runBlocking {
        val preferences = pref.data.first()
        return@runBlocking if (preferences[FIELD_IS_VISITOR] == true) {
            preferences[FIELD_VISITOR]?.let { Visitor.getVisitorFromJson(it) }
        } else {
            null
        }
    }

    override fun saveVisitor(visitor: Visitor): Unit = runBlocking {
        pref.edit { preferences ->
            preferences[FIELD_VISITOR] = visitor.getJsonObject().toString()
            preferences[FIELD_IS_VISITOR] = true
        }
    }

    override fun deleteVisitor(): Unit = runBlocking {
        pref.edit { preferences ->
            preferences.remove(FIELD_VISITOR)
            preferences[FIELD_IS_VISITOR] = false
        }
    }

    companion object {
        private val FIELD_VISITOR = stringPreferencesKey( "visitor")
        private val FIELD_IS_VISITOR = booleanPreferencesKey( "isVisitor")
    }

}