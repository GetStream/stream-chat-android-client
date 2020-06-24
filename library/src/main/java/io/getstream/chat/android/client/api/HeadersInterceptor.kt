package io.getstream.chat.android.client.api

import okhttp3.Interceptor
import okhttp3.Response

internal class HeadersInterceptor(val config: ChatClientConfig, val clientVersion: String) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authType = if (config.isAnonymous) "anonymous" else "jwt"
        val request = chain.request()
            .newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("stream-auth-type", authType)
            .addHeader("Accept-Encoding", "application/gzip")
            .addHeader("X-STREAM-CLIENT", clientVersion)
            .addHeader("Cache-Control", "no-cache")
            .build()
        return chain.proceed(request)
    }
}