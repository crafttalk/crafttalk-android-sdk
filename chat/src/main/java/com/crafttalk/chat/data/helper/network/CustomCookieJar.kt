package com.crafttalk.chat.data.helper.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CustomCookieJar : CookieJar {
    private val cache: MutableSet<WrappedCookie> = HashSet()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        for (cookie in cookies) {
            cache.add(WrappedCookie(cookie))
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies: MutableList<Cookie> = ArrayList()
        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val cookie: Cookie = iterator.next().cookie
            if (isCookieExpired(cookie)) {
                iterator.remove()
            } else if (cookie.matches(url)) {
                cookies.add(cookie)
            }
        }
        return cookies
    }

    private fun isCookieExpired(cookie: Cookie): Boolean {
        return cookie.expiresAt < System.currentTimeMillis()
    }
}

private class WrappedCookie(val cookie: Cookie) {

    override fun equals(other: Any?): Boolean {
        if (other !is WrappedCookie) return false
        return (other.cookie.name == cookie.name
                && other.cookie.domain == cookie.domain
                && other.cookie.path == cookie.path
                && other.cookie.secure == cookie.secure
                && other.cookie.hostOnly == cookie.hostOnly)
    }

    override fun hashCode(): Int {
        var hash = 17
        hash = 31 * hash + cookie.name.hashCode()
        hash = 31 * hash + cookie.domain.hashCode()
        hash = 31 * hash + cookie.path.hashCode()
        hash = 31 * hash + if (cookie.secure) 0 else 1
        hash = 31 * hash + if (cookie.hostOnly) 0 else 1
        return hash
    }

}