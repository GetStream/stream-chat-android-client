package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.socket.okhttp.OkHttpSocketFactory
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.utils.observable.ChatObservable
import io.getstream.chat.android.client.utils.observable.ChatObservableImpl

internal class ChatSocketImpl(
    private val apiKey: String,
    private val wssUrl: String,
    tokenManager: TokenManager,
    parser: ChatParser
) : ChatSocket {

    private val eventsParser = EventsParser(parser)

    private val service = ChatSocketServiceImpl(
        eventsParser,
        tokenManager,
        OkHttpSocketFactory(eventsParser, parser, tokenManager)
    )

    override fun connectAnonymously() {
        service.connect(wssUrl, apiKey, null)
    }

    override fun connect(user: User) {
        service.connect(wssUrl, apiKey, user)
    }

    override fun events(): ChatObservable {
        return ChatObservableImpl(service)
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
}
