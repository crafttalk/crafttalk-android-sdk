package com.crafttalk.chat.data.local.db

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.crafttalk.chat.data.local.db.database.ChatDatabase
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.message.MessageType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class MessagesDaoTest {
    private val db = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext, ChatDatabase::class.java
    ).build()
    private val dao = db.messageDao()

    @After fun closeDatabase() = db.close()

    @Test fun unreadCountsExcludeInfoAndUseAnExclusiveStartAndInclusiveEnd() {
        dao.insertMessages(listOf(
            message("at-read-mark", 100),
            message("unread", 110),
            message("info", 115, type = MessageType.INFO_MESSAGE),
            message("at-upper-bound", 120),
            message("after-upper-bound", 130),
            message("latest-info", 140, type = MessageType.INFO_MESSAGE)
        ))

        val ignored = listOf(MessageType.INFO_MESSAGE.valueType)
        assertEquals(3, dao.getCountUnreadMessages(100, ignored))
        assertEquals(0, dao.getCountUnreadMessages(130, ignored))
        assertEquals(2, dao.getCountUnreadMessagesRange(100, 120, ignored))
        assertEquals(0, dao.getCountUnreadMessagesRange(110, 115, ignored))
        assertEquals(1, dao.getCountUnreadMessagesRange(115, 120, ignored))
        assertEquals(0, dao.getCountUnreadMessagesRange(120, 120, ignored))
    }

    @Test fun historyAndPagedHistorySortByTimestampThenArrivalTime() = runBlocking {
        dao.insertMessages(listOf(
            message("older", 90, 999),
            message("unknown-arrival", 100),
            message("early-arrival", 100, 3),
            message("newer", 110, 1),
            message("late-arrival", 100, 8)
        ))
        val expected = listOf("newer", "late-arrival", "early-arrival", "unknown-arrival", "older")

        assertEquals(90L, dao.getFirstTime())
        assertEquals(110L, dao.getLastTime())
        assertEquals(expected, dao.getAllMessages().map { it.id })
        val source = dao.getMessages().asPagingSourceFactory().invoke()
        val page = source.load(PagingSource.LoadParams.Refresh(null, 10, false))
                as PagingSource.LoadResult.Page
        assertEquals(expected, page.data.map { it.id })
    }

    private fun message(id: String, timestamp: Long, arrival: Long? = null, type: MessageType = MessageType.MESSAGE) =
        MessageEntity(uuid = "visitor", id = id, isReply = true, timestamp = timestamp,
            arrivalTime = arrival, messageType = type.valueType)
}
