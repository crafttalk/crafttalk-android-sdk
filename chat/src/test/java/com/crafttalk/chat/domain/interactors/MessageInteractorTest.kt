package com.crafttalk.chat.domain.interactors

import android.content.Context
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.message.NetworkMessage
import com.crafttalk.chat.domain.repository.IConditionRepository
import com.crafttalk.chat.domain.repository.IMessageRepository
import com.crafttalk.chat.domain.repository.IPersonRepository
import com.crafttalk.chat.domain.transfer.TransferFileInfo
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.chat.utils.ChatParams
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

class MessageInteractorTest {
    private val previousAuthMode = ChatParams.authMode
    private val visitors = VisitorInteractor(InMemoryVisitors())
    private val messages = HistoryMessages()
    private val condition = HistoryCondition()
    private val interactor = MessageInteractor(messages, condition, visitors, PersonInteractor(unusedRepository<IPersonRepository>()))
    private val loadedEvents = mutableListOf<Boolean>()
    private val unreadPositions = mutableListOf<Int>()
    private var completedPages = 0

    @Before fun signIn() {
        ChatParams.authMode = AuthType.AUTH_WITH_FORM
        visitors.saveVisitor(visitor("reader"))
    }

    @After fun restoreAuthMode() {
        ChatParams.authMode = previousAuthMode
    }

    @Test fun unreadCountPassesTheReadBoundariesAndIgnoresOnlyInfoMessages() {
        val ignored = listOf(MessageType.INFO_MESSAGE.valueType)
        val repository = object : IMessageRepository by unusedRepository<IMessageRepository>() {
            override fun getCountUnreadMessages(currentReadMessageTime: Long, ignoredMessageTypes: List<Int>): Int {
                assertEquals(100L, currentReadMessageTime)
                assertEquals(ignored, ignoredMessageTypes)
                return 7
            }

            override fun getCountUnreadMessagesRange(
                currentReadMessageTime: Long, timestampLastMessage: Long, ignoredMessageTypes: List<Int>
            ): Int {
                assertEquals(100L, currentReadMessageTime)
                assertEquals(120L, timestampLastMessage)
                assertEquals(ignored, ignoredMessageTypes)
                return 2
            }
        }
        val reader = MessageInteractor(repository, condition, visitors, PersonInteractor(unusedRepository<IPersonRepository>()))
        assertEquals(7, reader.getCountUnreadMessages(100, null))
        assertEquals(2, reader.getCountUnreadMessages(100, 120))
    }

    @Test fun initialSyncRequestsLatestPageAndReportsMoreHistory() = runBlocking {
        condition.allHistoryLoaded = true
        messages.page = listOf(message("latest", 200L))
        messages.unreadCount = 3

        sync()

        assertEquals(listOf(HistoryRequest("reader", null, 0L)), messages.requests)
        assertEquals(listOf(false), loadedEvents)
        assertFalse(condition.allHistoryLoaded)
        assertEquals(listOf(3), unreadPositions)
        assertEquals(messages.page, messages.messagesForPersonNames)
        assertEquals(1, messages.merges)
    }

    @Test fun initialSyncRecordsTheEndOfHistoryReportedByTheRepository() = runBlocking {
        messages.endOfHistory = true
        sync()

        assertEquals(listOf(true), loadedEvents)
        assertTrue(condition.allHistoryLoaded)
    }

    @Test fun initialSyncKeepsHistoryOpenWhenThereAreNoNewLocalRows() = runBlocking {
        condition.allHistoryLoaded = true
        messages.page = emptyList()
        messages.endOfHistory = false

        sync()

        assertEquals(listOf(false), loadedEvents)
        assertFalse(condition.allHistoryLoaded)
    }

    @Test fun repeatSyncStartsAfterLastTimestampAndPreservesHistoryFlag() = runBlocking {
        condition.hasMessages = true
        condition.allHistoryLoaded = true
        messages.lastTime = 200L
        messages.page = listOf(message("new", 201L))
        messages.unreadCount = 3

        sync()

        assertEquals(listOf(HistoryRequest("reader", 201L, 0L)), messages.requests)
        assertEquals(listOf(2), unreadPositions)
        assertEquals(emptyList<Boolean>(), loadedEvents)
        assertTrue(condition.allHistoryLoaded)
        assertEquals(messages.page, messages.messagesForPersonNames)
        assertEquals(1, messages.merges)
    }

    @Test fun repeatSyncWithNoUnreadMessagesKeepsPositionAtZero() = runBlocking {
        condition.hasMessages = true
        messages.lastTime = 200L
        messages.unreadCount = 0
        messages.page = listOf(message("read", 201L))

        sync()

        assertEquals(listOf(0), unreadPositions)
    }

    @Test fun olderPagesUseFirstLocalTimestampAndStopWhenRepositoryReportsTheEnd() = runBlocking {
        condition.hasMessages = true
        messages.firstTime = 100L
        messages.page = listOf(message("older", 50L))
        loadOlder()

        messages.firstTime = 50L
        messages.page = emptyList()
        messages.endOfHistory = true
        loadOlder()
        loadOlder()

        assertEquals(listOf(HistoryRequest("reader", null, 100L), HistoryRequest("reader", null, 50L)), messages.requests)
        assertEquals(listOf(false, true), loadedEvents)
        assertTrue(condition.allHistoryLoaded)
        assertEquals(2, completedPages)
    }

    @Test fun olderPageContainingOnlyKnownMessagesAllowsAnotherRequest() = runBlocking {
        condition.hasMessages = true
        messages.firstTime = 100L
        messages.page = emptyList()
        messages.endOfHistory = false

        loadOlder()
        assertFalse(condition.allHistoryLoaded)

        messages.endOfHistory = true
        loadOlder()

        val request = HistoryRequest("reader", null, 100L)
        assertEquals(listOf(request, request), messages.requests)
        assertEquals(listOf(false, true), loadedEvents)
    }

    @Test fun readMarksReachTheCallerDuringSyncAndAreIgnoredWhenLoadingOlderPages() = runBlocking {
        val updates = mutableListOf<List<Pair<String, Long>>>()
        val firstMarks = listOf("first" to 100L)
        val newMarks = listOf("new" to 201L)

        messages.page = listOf(message("first", 100L))
        messages.readMarks = firstMarks
        sync(updates::add)
        condition.hasMessages = true
        messages.lastTime = 200L
        messages.page = listOf(message("new", 201L))
        messages.readMarks = newMarks
        sync(updates::add)

        messages.firstTime = 100L
        messages.page = listOf(message("older", 50L))
        messages.readMarks = listOf("older" to 50L)
        loadOlder()

        assertEquals(listOf(firstMarks, newMarks), updates)
        assertEquals(listOf(true, true, false), messages.readPointResults)
    }

    @Test fun forcedLoadChecksForOlderMessagesEvenAfterReachingTheEnd() = runBlocking {
        condition.hasMessages = true
        condition.allHistoryLoaded = true
        messages.firstTime = 50L
        messages.page = listOf(message("older", 25L))

        loadOlder(force = true)

        assertEquals(listOf(HistoryRequest("reader", null, 50L)), messages.requests)
        assertEquals(listOf(false), loadedEvents)
        assertFalse(condition.allHistoryLoaded)
        assertEquals(1, completedPages)
    }

    @Test fun forcedLoadWithNoLocalMessagesCompletesWithoutFetchingHistory() = runBlocking {
        loadOlder(force = true)

        assertTrue(messages.requests.isEmpty())
        assertTrue(loadedEvents.isEmpty())
        assertEquals(1, completedPages)
    }

    @Test fun signedOutVisitorCannotSyncOrLoadHistory() = runBlocking {
        visitors.clearDataVisitor()
        condition.hasMessages = true
        messages.firstTime = 100L
        messages.lastTime = 200L

        sync()
        loadOlder(force = true)

        assertTrue(messages.requests.isEmpty())
        assertEquals(0, messages.merges)
        assertEquals(0, completedPages)
    }

    private suspend fun sync(updateReadPoint: (List<Pair<String, Long>>) -> Boolean = { false }) = interactor.syncMessages(
        updateReadPoint = updateReadPoint,
        syncMessagesAcrossDevices = { unreadPositions.add(it) },
        eventStateHistoryLoaded = { loadedEvents.add(it) },
        updateSearchMessagePosition = {}
    )

    private suspend fun loadOlder(force: Boolean = false) = interactor.uploadHistoryMessages(
        eventStateHistoryLoaded = { loadedEvents.add(it) },
        uploadHistoryComplete = { completedPages++ },
        updateSearchMessagePosition = {},
        executeAnyway = force
    )

    private fun message(id: String, timestamp: Long) = MessageEntity(
        uuid = "reader", id = id, isReply = true, messageType = MessageType.MESSAGE.valueType, timestamp = timestamp
    )
}

private data class HistoryRequest(val uuid: String, val startTime: Long?, val endTime: Long)

private class HistoryCondition : IConditionRepository by unusedRepository<IConditionRepository>() {
    var hasMessages = false
    var allHistoryLoaded = false
    override suspend fun getStatusExistenceMessages() = hasMessages
    override fun getFlagAllHistoryLoaded() = allHistoryLoaded
    override fun saveFlagAllHistoryLoaded(isAllHistoryLoaded: Boolean) { allHistoryLoaded = isAllHistoryLoaded }
}

private class HistoryMessages : IMessageRepository by unusedRepository<IMessageRepository>() {
    var firstTime: Long? = null
    var lastTime: Long? = null
    var page = emptyList<MessageEntity>()
    var endOfHistory = false
    var readMarks = emptyList<Pair<String, Long>>()
    val readPointResults = mutableListOf<Boolean>()
    var unreadCount: Int? = null
    var messagesForPersonNames = emptyList<MessageEntity>()
    var merges = 0
    val requests = mutableListOf<HistoryRequest>()

    override suspend fun getTimeFirstMessage() = firstTime
    override suspend fun getTimeLastMessage() = lastTime
    override suspend fun uploadMessages(
        uuid: String,
        startTime: Long?,
        endTime: Long,
        updateReadPoint: (List<Pair<String, Long>>) -> Boolean,
        syncMessagesAcrossDevices: (Int) -> Unit,
        allMessageLoaded: () -> Unit,
        notAllMessageLoaded: () -> Unit,
        getPersonPreview: suspend (String) -> String?,
        getFileInfo: suspend (Context, NetworkMessage) -> TransferFileInfo?,
        updateSearchMessagePosition: suspend (List<MessageEntity>) -> Unit
    ): List<MessageEntity> {
        requests.add(HistoryRequest(uuid, startTime, endTime))
        if (endOfHistory) allMessageLoaded() else notAllMessageLoaded()
        if (readMarks.isNotEmpty()) readPointResults.add(updateReadPoint(readMarks))
        unreadCount?.let(syncMessagesAcrossDevices)
        return page
    }

    override suspend fun updatePersonNames(messages: List<MessageEntity>, updatePersonName: suspend (String?, String?) -> Unit) {
        messagesForPersonNames = messages
    }

    override suspend fun mergeNewMessages() { merges++ }
}

// Fail immediately if the interactor unexpectedly uses another repository operation.
private inline fun <reified T : Any> unusedRepository(): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
        throw AssertionError("Unexpected repository call: ${method.name}")
    } as T
