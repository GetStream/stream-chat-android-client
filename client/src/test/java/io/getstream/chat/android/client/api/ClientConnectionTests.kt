package io.getstream.chat.android.client.api

import android.content.Context
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatClientImpl
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.Date

internal class ClientConnectionTests {

    private val userId = "test-id"
    private val connectionId = "connection-id"
    private val user = User().apply { id = userId }
    private val token = "token"
    private val context = mock<Context>()

    private val config = ChatClientConfig(
        "api-key",
        "hello.http",
        "cdn.http",
        "socket.url",
        1000,
        1000,
        false,
        ChatLogger.Config(ChatLogLevel.NOTHING, null),
        ChatNotificationHandler(context),
        FakeTokenManager(token)
    )

    private val connectedEvent = ConnectedEvent(
        EventType.HEALTH_CHECK,
        Date(),
        user,
        connectionId
    )
    private val disconnectedEvent = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())

    private lateinit var api: ChatApi
    private lateinit var socket: ChatSocket
    private lateinit var retrofitApi: RetrofitApi
    private lateinit var retrofitCdnApi: RetrofitCdnApi
    private lateinit var client: ChatClient
    private lateinit var logger: ChatLogger
    private lateinit var notificationsManager: ChatNotifications
    private lateinit var initConnectionListener: InitConnectionListener
    private lateinit var socketListener: SocketListener

    @Before
    fun before() {
        socket = mock()
        retrofitApi = mock()
        retrofitCdnApi = mock()
        logger = mock()
        notificationsManager = mock()
        initConnectionListener = mock()
        api = ChatApiImpl(
            config.apiKey,
            retrofitApi,
            retrofitCdnApi,
            ChatParserImpl(),
            UuidGeneratorImpl()
        )

        whenever(socket.addListener(anyOrNull())) doAnswer { invocationOnMock ->
            socketListener = invocationOnMock.getArgument(0)
            socketListener.onEvent(disconnectedEvent)
        }

        client = ChatClientImpl(
            config,
            api,
            socket,
            notificationsManager
        )
    }

    @Test
    fun successConnection() {
        client.setUser(user, token)

        verify(socket, times(1)).connect(user)
    }

    @Test
    fun `Should not connect and report error when user is already set`() {
        socketListener.onEvent(connectedEvent)

        client.setUser(user, token, initConnectionListener)

        verify(socket, never()).connect(user)

        val error = ChatError("User cannot be set until previous one is disconnected.")
        verify(initConnectionListener).onError(argThat { message == error.message })
    }

    @Test
    fun connectAndDisconnect() {
        client.setUser(user, token)

        client.disconnect()

        verify(socket, times(1)).disconnect()
    }
}
