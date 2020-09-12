package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatApiImpl
import io.getstream.chat.android.client.api.RetrofitApi
import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.api.models.MessageRequest
import io.getstream.chat.android.client.api.models.MessageResponse
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.UuidGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class MessageIdGenerationTests {

    val userId = "user-id"
    val apiKey = "api-key"
    val connectionId = "connection-id"
    val messageId = "message-id"
    val channelType = "channel-type"
    val channelId = "channel-id"
    val randomUuid = "random-uuid"
    val messageText = "message-text"

    lateinit var uuidGenerator: UuidGenerator
    lateinit var retroApi: RetrofitApi
    lateinit var api: ChatApi

    @Before
    fun before() {
        retroApi = mock(RetrofitApi::class.java)
        uuidGenerator = mock(UuidGenerator::class.java)
        api = ChatApiImpl(
            apiKey,
            retroApi,
            mock(RetrofitCdnApi::class.java),
            mock(ChatParser::class.java),
            uuidGenerator
        )
        api.setConnection(userId, connectionId)
    }

    @Test
    fun emptyMessageId() {

        val message = Message()
        message.text = messageText

        `when`(uuidGenerator.generate()).thenReturn(randomUuid)
        `when`(
            retroApi.sendMessage(
                channelType,
                channelId,
                apiKey,
                userId,
                connectionId,
                MessageRequest(message)
            )
        ).thenReturn(RetroSuccess(MessageResponse(message)))

        api.sendMessage(channelType, channelId, message)

        assertThat(message.id).isEqualTo("$userId-$randomUuid")
    }

    @Test
    fun filledMessageId() {

        val message = Message()
        val preGeneratedId = "pre-generated-id"
        message.text = messageText
        message.id = preGeneratedId

        `when`(
            retroApi.sendMessage(
                channelType,
                channelId,
                apiKey,
                userId,
                connectionId,
                MessageRequest(message)
            )
        ).thenReturn(RetroSuccess(MessageResponse(message)))

        api.sendMessage(channelType, channelId, message)

        assertThat(message.id).isEqualTo(preGeneratedId)
    }
}
