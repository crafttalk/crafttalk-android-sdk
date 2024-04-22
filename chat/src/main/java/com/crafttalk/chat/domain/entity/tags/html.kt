package com.crafttalk.chat.domain.entity.tags

sealed class Tag(
    val name: String,
    @Transient
    open var pointStart: Int,
    @Transient
    open var pointEnd: Int
)
data class StrikeTag(
    override var pointStart: Int,
    override var pointEnd: Int
): Tag("strike", pointStart, pointEnd)
data class StrongTag(
    override var pointStart: Int,
    override var pointEnd: Int
): Tag("strong", pointStart, pointEnd)
data class BTag(
    override var pointStart: Int,
    override var pointEnd: Int
): Tag("b", pointStart, pointEnd)
data class ItalicTag(
    override var pointStart: Int,
    override var pointEnd: Int
): Tag("i", pointStart, pointEnd)
data class EmTag(
    override var pointStart: Int,
    override var pointEnd: Int
): Tag("em", pointStart, pointEnd)
data class UrlTag(
    override var pointStart: Int,
    override var pointEnd: Int,
    val url: String
): Tag("a", pointStart, pointEnd)
data class ImageTag(
    override var pointStart: Int,
    override var pointEnd: Int,
    val url: String,
    val width: Int,
    val height: Int
): Tag("img", pointStart, pointEnd)

data class OrderedListTag(
    override var pointStart: Int,
    override var pointEnd: Int,
    val countNesting: Int
): Tag("ol", pointStart, pointEnd)

data class HostListTag(
    override var pointStart: Int,
    override var pointEnd: Int,
    val countNesting: Int
): Tag("ul", pointStart, pointEnd)
data class ItemListTag(
    override var pointStart: Int,
    override var pointEnd: Int,
    val countNesting: Int
): Tag("li", pointStart, pointEnd)
data class PhoneTag(
    override var pointStart: Int,
    override var pointEnd: Int,
    val phone: String
): Tag("phone", pointStart, pointEnd)

class AttrTag(
    val attrName: String,
    val value: String
)

class AttrTagInfo(
    val endIndex: Int,
    val attrTag: AttrTag
)