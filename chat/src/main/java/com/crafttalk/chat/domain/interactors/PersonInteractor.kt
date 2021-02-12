package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.repository.IPersonRepository
import javax.inject.Inject

class PersonInteractor
@Inject constructor(
    private val personRepository: IPersonRepository
) {
    fun getPersonPreview(personId: String, visitorToken: String) : String? = personRepository.getPersonPreview(personId, visitorToken)
}