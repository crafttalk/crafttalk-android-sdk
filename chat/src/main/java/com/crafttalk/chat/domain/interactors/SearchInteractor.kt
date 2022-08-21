package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject

fun String.countContains(pattern: String): Int {
    if (!contains(pattern)) return 0
    var count = 0
    var indexStart: Int
    indexStart = this.indexOf(pattern)
    if (indexStart != -1) {
        count += 1
    }
    while (indexStart != -1) {
        indexStart = this.indexOf(pattern, indexStart + 1)
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
    private var searchItems: MutableList<SearchItem> = mutableListOf()
    private var allMessageLoaded = false

    fun cancelSearch() {
        searchText = ""
        indexCurrentSearchItem = 0
        indexLastLoadSearchItem = -1
        searchItems.clear()
        allMessageLoaded = false
    }

    fun getAllSearchedItems(): List<SearchItem> {
        return searchItems
    }

    suspend fun preloadMessages(searchText: String): SearchItem? {
        val uuid = visitorInteractor.getVisitor()?.uuid ?: return null
        if (this.searchText == searchText) return searchItems.getOrNull(indexCurrentSearchItem)
        this.searchText = searchText
        indexCurrentSearchItem = 0
        indexLastLoadSearchItem = -1
        searchItems = mutableListOf()
        val searchResult = messageRepository.searchTimestampsMessages(uuid, searchText)?.messages

        var searchAllCount = 0
        searchResult?.forEach {
            searchAllCount += it.message?.countContains(searchText) ?: 0
        }

        if (searchResult.isNullOrEmpty()) {
            return null
        } else {
            var messagePosition: Int? = 0
            var currentSearchPosition = 0
            searchItems = mutableListOf()
            searchResult.forEach { networkMessage ->
                messagePosition = if (messagePosition != null) {
                    indexLastLoadSearchItem++
                    val messageId = if (networkMessage.isReply) networkMessage.id else networkMessage.idFromChannel
                    if (messageId == null) {
                        null
                    } else {
                        messageRepository.getPositionByTimestamp(
                            id = messageId,
                            timestamp = networkMessage.timestamp
                        )
                    }
                } else {
                    null
                }
                val countMatchInMsg = networkMessage.message?.countContains(searchText) ?: 1
                for(i in 1..countMatchInMsg) {
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
            }
        }

        val firstItem = searchItems.firstOrNull()
        if (firstItem != null && firstItem.scrollPosition == null) {
            additionalLoadingMessages()
        }
        return firstItem
    }

    private suspend fun uploadMessages(visitor: Visitor, startTime: Long) {
        val firstMessageTime = messageRepository.getTimeFirstMessage() ?: return

        val messages = messageRepository.uploadMessages(
            uuid = visitor.uuid,
            startTime = startTime,
            endTime = firstMessageTime,
            updateReadPoint = { false },
            syncMessagesAcrossDevices = {},
            returnedEmptyPool = { allMessageLoaded = true },
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
            returnedEmptyPool = { allMessageLoaded = true },
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
        if (indexCurrentSearchItem != indexLastLoadSearchItem) return
        if (allMessageLoaded) return

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
        return searchItems[indexCurrentSearchItem]
    }

    fun onSearchBottomClick(): SearchItem? {
        if (indexCurrentSearchItem == 0) {
            return null
        }
        indexCurrentSearchItem--
        return searchItems[indexCurrentSearchItem]
    }

    private suspend fun fillMessagePosition() {
        run loop@{
            searchItems.forEachIndexed { index, item ->
                if (item.scrollPosition != null) return@forEachIndexed

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

    suspend fun updateMessagePosition(insertedMessages: List<MessageEntity>) {
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

                val offset = insertedMessages.count { it.timestamp > item.timestamp }

                searchItems[index] = item.apply {
                    scrollPosition = messagePosition + offset
                }
            }
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