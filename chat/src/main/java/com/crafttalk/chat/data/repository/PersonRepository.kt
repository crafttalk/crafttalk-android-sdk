package com.crafttalk.chat.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.crafttalk.chat.data.api.rest.PersonApi
import com.crafttalk.chat.data.helper.network.toData
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.dao.PersonDao
import com.crafttalk.chat.data.local.db.entity.PersonEntity
import com.crafttalk.chat.domain.repository.IPersonRepository
import com.crafttalk.chat.utils.ChatParams
import com.crafttalk.chat.utils.OperatorNameMode
import com.crafttalk.chat.utils.OperatorPreviewMode
import java.lang.Exception
import javax.inject.Inject

class PersonRepository
@Inject constructor(
    private val personDao: PersonDao,
    private val messagesDao: MessagesDao,
    private val personApi: PersonApi
) : IPersonRepository {

    override suspend fun updatePersonName(personId: String?, currentPersonName: String?) {
        personId ?: return
        currentPersonName ?: return
        if (personId.isEmpty()) return
        when (ChatParams.operatorNameMode) {
            OperatorNameMode.ACTUAL -> {
                messagesDao.updatePersonName(personId, currentPersonName)
            }
            else -> {
                Log.e("CTALK_FAIL_REQUEST","Undefined behavior detected, please investigate the issue")
            }
        }
    }

    override suspend fun getPersonPreview(personId: String, visitorToken: String): String? {
        if (personId.isEmpty()) return null
        return try {
            when (ChatParams.operatorPreviewMode) {
                OperatorPreviewMode.CACHE -> {
                    personDao.getPersonPreview(personId)
                        ?: personApi.getPersonPreview(
                            personId = personId,
                            visitorToken = visitorToken
                        ).toData()?.picture?.apply {
                            try {
                                personDao.addPersonPreview(PersonEntity(personId, this))
                                messagesDao.updatePersonPreview(personId, this)
                            } catch (ex: SQLiteConstraintException) {}
                        }
                }
                OperatorPreviewMode.ALWAYS_REQUEST -> {
                    personApi.getPersonPreview(
                        personId = personId,
                        visitorToken = visitorToken
                    ).toData()?.picture.apply {
                        messagesDao.updatePersonPreview(personId, this)
                    }
                }
                else -> null
            }
        } catch (ex: Exception) {
            Log.e("CTALK_FAIL_REQUEST", "getPersonPreview fail: ${ex.message}")
            null
        }
    }

}