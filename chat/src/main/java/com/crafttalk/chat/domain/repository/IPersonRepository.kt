package com.crafttalk.chat.domain.repository

interface IPersonRepository {
    suspend fun updatePersonName(personId: String?, currentPersonName: String?)
    suspend fun getPersonPreview(personId: String, visitorToken: String): String?
}