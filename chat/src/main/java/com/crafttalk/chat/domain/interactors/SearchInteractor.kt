package com.crafttalk.chat.domain.interactors

import android.util.Log
import com.crafttalk.chat.data.helper.converters.text.convertTextToNormalString
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IMessageRepository
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

fun String.countContains(pattern: String): Int {
//    Log.d("SEARCH_LOG", "countContains str: $this, pattern: $pattern;")
    if (!contains(pattern, ignoreCase = true)) return 0
    var count = 0
    var indexStart: Int
    indexStart = this.indexOf(pattern, ignoreCase = true)
    if (indexStart != -1) {
        count += 1
    }
    while (indexStart != -1) {
        indexStart = this.indexOf(pattern, indexStart + 1, ignoreCase = true)
        if (indexStart != -1) {
            count += 1
        }
    }
    return count
}

class SearchInteractor
@Inject constructor(
    private val visitorInteractor: VisitorInteractor,
    private val personInteractor: PersonInteractor,
    private val messageRepository: IMessageRepository
) {

    private var searchText: String = ""
    private var indexCurrentSearchItem: Int = 0
    private var indexLastLoadSearchItem: Int = -1
    private var searchItems: CopyOnWriteArrayList<SearchItem> = CopyOnWriteArrayList()
    private var allMessageLoaded = false
    private var canUpdatePositions = true
    private var wantUpdatePositions = false

    fun cancelSearch() {
        searchText = ""
        indexCurrentSearchItem = 0
        indexLastLoadSearchItem = -1
        searchItems.clear()
        allMessageLoaded = false
    }

    fun getAllSearchedItems(): List<SearchItem> {
        return searchItems.toList()
    }

    suspend fun preloadMessages(searchText: String, searchStart: () -> Unit): SearchItem? {
        val uuid = visitorInteractor.getVisitor()?.uuid ?: return null
        searchStart()
        if (this.searchText == searchText) return searchItems.getOrNull(indexCurrentSearchItem)
        this.searchText = searchText
        indexCurrentSearchItem = 0
        indexLastLoadSearchItem = -1
        canUpdatePositions = false
        searchItems.clear()
        val searchResult = messageRepository.searchTimestampsMessages(uuid, searchText)?.messages

        Log.d("CTALK_SEARCH_LOG", "search count: ${searchResult?.size};")
        yield()

        var searchAllCount = 0
        searchResult?.forEach {
            yield()
            searchAllCount += when {
//                it.isFile -> it.attachmentName?.convertTextToNormalString(arrayListOf())?.countContains(searchText).apply { Log.d("SEARCH_LOG", "countContains 1 res: $this") } ?: 0
                it.isText -> it.message?.convertTextToNormalString(arrayListOf())?.countContains(searchText).apply { Log.d("SEARCH_LOG", "countContains 2 res: $this") } ?: 0
                else -> 0
            }
        }

        Log.d("CTALK_SEARCH_LOG", "searchAllCount: ${searchAllCount};")

        if (searchResult.isNullOrEmpty() || searchAllCount == 0) {
            return null
        } else {
            var messagePosition: Int? = 0
            var currentSearchPosition = 0
            searchResult.forEach { networkMessage ->
                yield()
                messagePosition = if (messagePosition != null) {
                    val messageId = if (networkMessage.isReply) networkMessage.id else networkMessage.idFromChannel
                    if (messageId == null) {
                        null
                    } else {
                        messageRepository.getPositionByTimestamp(
                            id = messageId,
                            timestamp = networkMessage.timestamp
                        ).apply {
                            if (this != null) {
                                indexLastLoadSearchItem++
                            }
                        }
                    }
                } else {
                    null
                }
                val countMatchInMsg = when {
//                    networkMessage.isFile -> networkMessage.attachmentName?.convertTextToNormalString(arrayListOf())?.countContains(searchText).apply { Log.d("SEARCH_LOG", "countContains 3 res: $this") } ?: 0
                    networkMessage.isText -> networkMessage.message?.convertTextToNormalString(arrayListOf())?.countContains(searchText).apply { Log.d("SEARCH_LOG", "countContains 4 res: $this") } ?: 0
                    else -> 0
                }
                Log.d("CTALK_SEARCH_LOG", "countMatchInMsg: $countMatchInMsg; messagePosition: $messagePosition; networkMessage: $networkMessage;")
                for(i in 1..countMatchInMsg) {
                    yield()
                    currentSearchPosition++
                    searchItems.add(
                        SearchItem(
                            id = if (networkMessage.isReply) networkMessage.id else networkMessage.idFromChannel,
                            timestamp = networkMessage.timestamp,
                            searchPosition = currentSearchPosition,
                            positionMatchInMsg = i,
                            allCount = searchAllCount,
                            scrollPosition = messagePosition,
                            isLast = (currentSearchPosition == 1) || (currentSearchPosition == searchAllCount),
                        )
                    )
                }
                canUpdatePositions = true
                if (wantUpdatePositions) {
                    updateMessagePosition(null)
                }
            }
        }

        searchItems.forEach {
            Log.d("CTALK_SEARCH_LOG", "searchItems item: ${it};")
        }

        val firstItem = searchItems.firstOrNull()
        val secondItem = searchItems.getOrNull(1)
        if ((firstItem != null && firstItem.scrollPosition == null) ||
            (secondItem != null && secondItem.scrollPosition == null)) {
            additionalLoadingMessages()
        }
        return firstItem.apply {
            Log.d("CTALK_SEARCH_LOG", "searchItems first: ${this};")
        }
    }

    private suspend fun uploadMessages(visitor: Visitor, startTime: Long) {
        val firstMessageTime = messageRepository.getTimeFirstMessage() ?: return

        val messages = messageRepository.uploadMessages(
            uuid = visitor.uuid,
            startTime = startTime,
            endTime = firstMessageTime,
            updateReadPoint = { false },
            syncMessagesAcrossDevices = {},
            allMessageLoaded = { allMessageLoaded = true },
            notAllMessageLoaded = { allMessageLoaded = false },
            getPersonPreview = { personId ->
                personInteractor.getPersonPreview(personId, visitor.token)
            },
            getFileInfo = messageRepository::getFileInfo,
            updateSearchMessagePosition = ::updateMessagePosition
        )
        messageRepository.updatePersonNames(messages, personInteractor::updatePersonName)

        if (allMessageLoaded) return

        val postMessages = messageRepository.uploadMessages(
            uuid = visitor.uuid,
            startTime = null,
            endTime = startTime,
            updateReadPoint = { false },
            syncMessagesAcrossDevices = {},
            allMessageLoaded = { allMessageLoaded = true },
            notAllMessageLoaded = { allMessageLoaded = false },
            getPersonPreview = { personId ->
                personInteractor.getPersonPreview(personId, visitor.token)
            },
            getFileInfo = messageRepository::getFileInfo,
            updateSearchMessagePosition = ::updateMessagePosition
        )
        messageRepository.updatePersonNames(postMessages, personInteractor::updatePersonName)
    }

    private suspend fun additionalLoadingMessages() {
        val visitor = visitorInteractor.getVisitor() ?: return

        if (indexLastLoadSearchItem == searchItems.size - 1) return
        if (indexCurrentSearchItem < indexLastLoadSearchItem) return
        if (allMessageLoaded) return

        yield()
        if (indexCurrentSearchItem == 0) {
            val startTime = searchItems.getOrNull(1)?.timestamp ?: searchItems.firstOrNull()?.timestamp
            startTime?.let { time ->
                uploadMessages(
                    visitor,
                    time
                )
            }
        } else {
            uploadMessages(
                visitor,
                searchItems[indexLastLoadSearchItem + 1].timestamp
            )
        }
        fillMessagePosition()
    }

    suspend fun onSearchTopClick(): SearchItem? {
        if (indexCurrentSearchItem == searchItems.size - 1) {
            return null
        }
        indexCurrentSearchItem++
        fillMessagePosition()
        additionalLoadingMessages()
        return searchItems.getOrNull(indexCurrentSearchItem)
    }

    fun onSearchBottomClick(): SearchItem? {
        if (indexCurrentSearchItem == 0) {
            return null
        }
        indexCurrentSearchItem--
        return searchItems.getOrNull(indexCurrentSearchItem)
    }

    private suspend fun fillMessagePosition() {
        run loop@{
            searchItems.forEachIndexed { index, item ->
                yield()
                if (item.scrollPosition != null) {
                    if (index > indexLastLoadSearchItem) {
                        indexLastLoadSearchItem = index
                    }
                    return@forEachIndexed
                }
                val messagePosition = if (item.id == null) {
                    return@loop
                } else {
                    messageRepository.getPositionByTimestamp(
                        id = item.id,
                        timestamp = item.timestamp
                    ) ?: return@loop
                }
                searchItems[index] = item.copy(scrollPosition = messagePosition)
                if (index > indexLastLoadSearchItem) {
                    indexLastLoadSearchItem = index
                }
            }
        }
    }

    suspend fun updateMessagePosition(insertedMessages: List<MessageEntity>?) {
        if (!canUpdatePositions) {
            wantUpdatePositions = true
            return
        }
        run loop@{
            searchItems.forEachIndexed { index, item ->
                val messagePosition = if (item.id == null) {
                    return@loop
                } else {
                    messageRepository.getPositionByTimestamp(
                        id = item.id,
                        timestamp = item.timestamp
                    ) ?: return@loop
                }
                val offset = insertedMessages?.count { it.timestamp > item.timestamp } ?: 0
                searchItems[index] = item.apply {
                    scrollPosition = messagePosition + offset
                }
            }
            wantUpdatePositions = false
        }
    }
}

data class SearchItem(
    val id: String?,
    val timestamp: Long,
    val searchPosition: Int,
    val positionMatchInMsg: Int,
    val allCount: Int,
    var scrollPosition: Int?,
    val isLast: Boolean
)