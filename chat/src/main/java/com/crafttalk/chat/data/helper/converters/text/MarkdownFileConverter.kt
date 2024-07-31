package com.crafttalk.chat.data.helper.converters.text

import android.webkit.MimeTypeMap
import com.crafttalk.chat.utils.ChatParams


data class MarkdownFileConverter(
    val string: String){

     val arrayOfURILinks = arrayListOf<String>()
     val arrayOfURLLinks = arrayListOf<String>()
     val arrayOfNames = arrayListOf<String>()
     val arrayOfMimeType = arrayListOf<String>()
     var count:Int = 0

    fun convert(){
        val regex = "<a[^>]*href=\"(.*?)\">(.*?)</a>".toRegex() // Define regular expression
        for (match in regex.findAll(string)) {
            val attributeValue = match.groups[1]?.value // Capture group 1 (gref value)
            val text = match.groups[2]?.value // Capture group 2 (text inside tag)
            val correctLink = "${ChatParams.urlChatScheme}://${ChatParams.urlChatHost}${attributeValue}"
            //val correctLink = attributeValue
            val extension = MimeTypeMap.getFileExtensionFromUrl(correctLink)
            arrayOfURLLinks.add(correctLink)
            arrayOfMimeType.add(extension)
            arrayOfURILinks.add(attributeValue!!)
            arrayOfNames.add(text!!)
        }
        count = arrayOfURILinks.size
    }
}