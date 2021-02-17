package com.crafttalk.chat.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.crafttalk.chat.data.api.rest.PersonApi
import com.crafttalk.chat.data.helper.network.toData
import com.crafttalk.chat.data.local.db.dao.PersonDao
import com.crafttalk.chat.data.local.db.entity.Person
import com.crafttalk.chat.domain.repository.IPersonRepository
import javax.inject.Inject

class PersonRepository
@Inject constructor(
    private val dao: PersonDao,
    private val personApi: PersonApi
) : IPersonRepository {
    override fun getPersonPreview(personId: String, visitorToken: String): String? {
        val previewFromDB = dao.getPersonPreview(personId)
        return if (previewFromDB == null) {
            val previewFromNetwork = personApi.getPersonPreview(
                personId = personId,
                visitorToken = visitorToken
            ).toData().picture
            return previewFromNetwork?.apply {
                try {
                    dao.addPersonPreview(Person(personId, this))
                } catch (ex: SQLiteConstraintException) {}
            }
        } else {
            previewFromDB
        }
    }
}