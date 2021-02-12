package com.crafttalk.chat.domain.repository

interface IPersonRepository {
    fun getPersonPreview(personId: String, visitorToken: String) : String?
}