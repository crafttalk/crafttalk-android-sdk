package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IVisitorRepository
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class VisitorInteractorTest {
    private val previousAuthMode = ChatParams.authMode
    private val interactor = VisitorInteractor(InMemoryVisitors())
    private val clientVisitor = visitor("client")
    private val savedVisitor = visitor("saved")

    @After fun restoreAuthMode() {
        ChatParams.authMode = previousAuthMode
    }

    @Test fun formUsesSavedVisitorAndClearsOnlyTheSavedIdentity() {
        interactor.setVisitor(clientVisitor)
        interactor.saveVisitor(savedVisitor)
        ChatParams.authMode = AuthType.AUTH_WITH_FORM

        assertEquals(savedVisitor.uuid, interactor.getVisitor()?.uuid)
        interactor.clearDataVisitor()
        assertNull(interactor.getVisitor())

        ChatParams.authMode = AuthType.AUTH_WITHOUT_FORM
        assertEquals(clientVisitor.uuid, interactor.getVisitor()?.uuid)
    }

    @Test fun withoutFormUsesClientVisitorAndRequiresANewIdentityAfterClearing() {
        interactor.saveVisitor(savedVisitor)
        interactor.setVisitor(clientVisitor)
        ChatParams.authMode = AuthType.AUTH_WITHOUT_FORM

        assertEquals(clientVisitor.uuid, interactor.getVisitor()?.uuid)
        interactor.clearDataVisitor()
        val error = assertThrows(Exception::class.java) { interactor.getVisitor() }
        assertEquals("Visitor must not be null!", error.message)
        val nextVisitor = visitor("next-client")
        interactor.setVisitor(nextVisitor)
        assertEquals(nextVisitor.uuid, interactor.getVisitor()?.uuid)

        ChatParams.authMode = AuthType.AUTH_WITH_FORM
        assertEquals(savedVisitor.uuid, interactor.getVisitor()?.uuid)
    }
}

internal fun visitor(uuid: String) = Visitor(uuid, "token-$uuid", "", "", null, null, null, null)

internal class InMemoryVisitors : IVisitorRepository {
    private var client: Visitor? = null
    override val visitorFlow = MutableStateFlow<Visitor?>(null)

    override fun getVisitorFromClient() = client
    override fun setVisitorFromClient(visitor: Visitor?) { client = visitor }
    override fun getVisitorFromDataStore() = visitorFlow.value
    override fun saveVisitor(visitor: Visitor) { visitorFlow.value = visitor }
    override fun deleteVisitor() { visitorFlow.value = null }
}
