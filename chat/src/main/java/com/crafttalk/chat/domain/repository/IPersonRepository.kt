package com.crafttalk.chat.domain.repository

interface IPersonRepository {
    fun updatePersonName(personId: String?, currentPersonName: String?)
    fun getPersonPreview(personId: String, visitorToken: String): String?
}