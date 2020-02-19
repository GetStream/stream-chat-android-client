package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.token.CachedTokenProvider
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.utils.observable.ChatObservableImpl

class ChatSocketImpl(
    val apiKey: String,
    val wssUrl: String,
    val cachedTokenProvider: CachedTokenProvider,
    val chatParser: ChatParser,
    val logger: ChatLogger?
) : ChatSocket {

    private val service = ChatSocketServiceImpl(chatParser, logger)

    override fun connectAnonymously() {
        connect(null, null)
    }

    override fun connect(user: User) {
        cachedTokenProvider.getToken(object : TokenProvider.TokenProviderListener {
            override fun onSuccess(token: String) {
                connect(user, token)
            }
        })
    }

    override fun events(): ChatObservableImpl {
        return ChatObservableImpl(
            service
        )
    }

    override fun disconnect() {
        service.disconnect()
    }

    override fun addListener(listener: SocketListener) {
        service.addListener(listener)
    }

    override fun removeListener(listener: SocketListener) {
        service.removeListener(listener)
    }

    private fun connect(user: User?, userToken: String?) {
        service.connect(wssUrl, apiKey, user, userToken)
    }

}