package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.testing.loadResource
import io.getstream.chat.android.client.utils.ImmediateTokenProvider
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class DatesTests {

    val parser = ChatParserImpl()
    val jsonMessage = loadResource("/message.json")
    val expectedTime = 1591787071000L

    /**
     * Verifies that [io.getstream.chat.android.client.parser.DateAdapter] parses valid time
     *
     */
    @Test
    fun multithreadedDateParsing() {

        userParser()

        val threadA = Thread {
            var n = 200
            while (n > 0) {
                userParser()
                Thread.sleep((10 * Math.random()).toLong())
                n--
            }
        }

        val threadB = Thread {
            var n = 200
            while (n > 0) {
                useGson()
                Thread.sleep((10 * Math.random()).toLong())
                n--
            }
        }

        threadA.start()
        threadB.start()

        threadA.join()
        threadB.join()
    }

    @Test
    fun zzz() {

        val server = MockWebServer()

        val body = "{\"message\":$jsonMessage}"

        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setBody(body)
            }
        }

        val baseUrl = server.url("/")
        val config = ChatClientConfig(
            "",
            baseUrl,
            baseUrl,
            "wss://hello.com",
            10000,
            10000,
            ChatLogger.Config(ChatLogLevel.NOTHING, null)
        )

        config.tokenManager.setTokenProvider(ImmediateTokenProvider("tok"))

        val api = ChatModules(config).api()

        api.setConnection("user-id", "connection-id")

        val threadA = Thread {
            var n = 200
            while (n > 0) {
                val result = api.getMessage("x").execute()
                assert(result.data())
                Thread.sleep((10 * Math.random()).toLong())
                n--
            }
        }

        val threadB = Thread {
            var n = 200
            while (n > 0) {
                val result = api.getMessage("x").execute()
                assert(result.data())
                Thread.sleep((10 * Math.random()).toLong())
                n--
            }
        }

        threadA.start()
        threadB.start()

        threadA.join()
        threadB.join()
    }

    private fun useGson() {
        val message = parser.gson.fromJson(jsonMessage, Message::class.java)
        assert(message)
    }

    private fun userParser() {
        val message = parser.fromJson(jsonMessage, Message::class.java)
        assert(message)
    }

    private fun assert(message: Message) {
        val createdAt = message.createdAt
        val time = createdAt!!.time
        assertThat(time).isEqualTo(expectedTime)
    }
}