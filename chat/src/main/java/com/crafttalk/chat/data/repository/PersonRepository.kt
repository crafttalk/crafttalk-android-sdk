package com.crafttalk.chat.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.crafttalk.chat.data.api.rest.PersonApi
import com.crafttalk.chat.data.helper.network.toData
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.dao.PersonDao
import com.crafttalk.chat.data.local.db.entity.PersonEntity
import com.crafttalk.chat.domain.repository.IPersonRepository
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.OperatorPreviewMode
import java.lang.Exception
import javax.inject.Inject

class PersonRepository
@Inject constructor(
    private val personDao: PersonDao,
    private val messagesDao: MessagesDao,
    private val personApi: PersonApi
) : IPersonRepository {
    override fun getPersonPreview(personId: String, visitorToken: String): String? {
        try {
            when (ChatAttr.getInstance().operatorPreviewMode) {
                OperatorPreviewMode.CACHE_ONLY_LINK -> {
                    return personDao.getPersonPreview(personId)
                        ?: personApi.getPersonPreview(
                            personId = personId,
                            visitorToken = visitorToken
                        ).toData().picture?.apply {
                            try {
                                personDao.addPersonPreview(PersonEntity(personId, this))
                                messagesDao.updatePersonPreview(personId, this)
                            } catch (ex: SQLiteConstraintException) {}
                        }
                }
                OperatorPreviewMode.ALWAYS_REQUEST -> {
                    return personApi.getPersonPreview(
                        personId = personId,
                        visitorToken = visitorToken
                    ).toData().picture.apply {
                        messagesDao.updatePersonPreview(personId, this)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("FAIL_REQUEST", "getPersonPreview fail: ${ex.message}")
            return null
        }
    }
}