package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.call.ChatCall

class ChatSocketConnectionImpl(
    private val apiKey: String,
    private val wssUrl: String
) {

    private val service = StreamWebSocketService()

    fun connect(): ChatCall<ConnectionData> {
        val result = ConnectionCall()

        val callback: (ConnectionData, Throwable?) -> Unit = { c, t ->
            result.deliverResult(c, t)
        }

        connect(null, null, callback)

        return result
    }

    fun connect(user: User, tokenProvider: CachedTokenProvider): ChatCall<ConnectionData> {

        val result = ConnectionCall()

        val callback: (ConnectionData, Throwable?) -> Unit = { c, t ->
            result.deliverResult(c, t)
        }

        tokenProvider.getToken(object : TokenProvider.TokenProviderListener {
            override fun onSuccess(token: String) {
                connect(user, token, callback)
            }
        })

        return result
    }

    fun events(): ChatObservable {
        return ChatObservable(service)
    }

    fun disconnect() {
        service.disconnect()
    }

    private fun connect(
        user: User?,
        userToken: String?,
        listener: (ConnectionData, Throwable?) -> Unit
    ) {
        service.connect(wssUrl, apiKey, user, userToken, listener)
    }

}