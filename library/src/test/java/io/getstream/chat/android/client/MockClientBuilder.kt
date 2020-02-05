package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.ChatConfig
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.gson.JsonParserImpl
import io.getstream.chat.android.client.logger.StreamLogger
import io.getstream.chat.android.client.observable.JustObservable
import io.getstream.chat.android.client.poc.utils.SuccessTokenProvider
import io.getstream.chat.android.client.socket.ChatSocket
import org.mockito.Mockito

/**
 * Used for integrations tests.
 * Initialises mock internals of [ChatClientImpl]
 */
class MockClientBuilder {

    val userId = "test-id"
    val connectionId = "connection-id"
    val apiKey = "api-key"
    val channelType = "channel-type"
    val channelId = "channel-id"
    val token = "token"
    val serverErrorCode = 500
    val user = User(userId)
    val connectedEvent = ConnectedEvent().apply {
        me = this@MockClientBuilder.user
        connectionId = this@MockClientBuilder.connectionId
    }

    lateinit var api: ChatApi
    lateinit var socket: ChatSocket
    lateinit var retrofitApi: RetrofitApi

    private lateinit var client: ChatClient

    fun build(): ChatClient {

        val config = ChatConfig.Builder()
            .apiKey(apiKey)
            .token(token)
            .build()
        val logger = Mockito.mock(StreamLogger::class.java)
        socket = Mockito.mock(ChatSocket::class.java)
        retrofitApi = Mockito.mock(RetrofitApi::class.java)
        api = ChatApiImpl(config, retrofitApi, JsonParserImpl(), null)

        Mockito.`when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(api, socket, config, logger)
        client.setUser(user)

        return client
    }
}