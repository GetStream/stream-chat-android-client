package io.getstream.chat.android.client.api

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatClientImpl
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.ImmediateTokenProvider
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import io.getstream.chat.android.client.utils.observable.JustObservable
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

internal class ClientConnectionTests {

    val userId = "test-id"
    val connectionId = "connection-id"
    val user = User().apply { id = userId }
    val token = "token"
    val context = mock(Context::class.java)

    val config = ChatClientConfig(
        "api-key",
        "http://hello.com".toHttpUrlOrNull()!!,
        "http://cdn.http".toHttpUrlOrNull()!!,
        "socket.url",
        1000,
        1000,
        ChatLogger.Config(ChatLogLevel.NOTHING, null),
        ChatNotificationConfig(context)

    ).apply {
        tokenManager.setTokenProvider(ImmediateTokenProvider(token))
    }

    val connectedEvent = ConnectedEvent().apply {
        me = this@ClientConnectionTests.user
        connectionId = this@ClientConnectionTests.connectionId
    }

    lateinit var api: ChatApi
    lateinit var socket: ChatSocket
    lateinit var retrofitApi: RetrofitApi
    lateinit var retrofitCdnApi: RetrofitCdnApi
    lateinit var client: ChatClient
    lateinit var logger: ChatLogger
    lateinit var notificationsManager: ChatNotifications

    @Before
    fun before() {
        socket = mock(ChatSocket::class.java)
        retrofitApi = mock(RetrofitApi::class.java)
        retrofitCdnApi = mock(RetrofitCdnApi::class.java)
        logger = mock(ChatLogger::class.java)
        notificationsManager = mock(ChatNotifications::class.java)
        api = ChatApiImpl(
            config.apiKey,
            retrofitApi,
            retrofitCdnApi,
            ChatParserImpl(),
            UuidGeneratorImpl()
        )
    }

    @Test
    fun successConnection() {

        `when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(
            config,
            api,
            socket,
            notificationsManager
        )
        client.setUser(user, token)

        verify(socket, times(1)).connect(user)
    }

    @Test
    fun connectAndDisconnect() {
        `when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(
            config,
            api,
            socket,
            notificationsManager
        )
        client.setUser(user, token)

        client.disconnect()

        verify(socket, times(1)).disconnect()
    }


}
