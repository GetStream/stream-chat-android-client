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
import java.util.logging.Level
import java.util.logging.Logger


class DatesTests {

    val parser = ChatParserImpl()
    val jsonMessageA = loadResource("/message-a.json")
    val jsonMessageB = loadResource("/message-b.json")
    val expectedTimeA = 1591787071000L
    val expectedTimeB = 1591786800000L

    init {
        Logger.getLogger(MockWebServer::class.java.name).level = Level.WARNING
    }


    /**
     * Verifies that [io.getstream.chat.android.client.parser.DateAdapter] parses valid time
     */
    @Test
    fun multithreadedDateParsing() {

        userParser(expectedTimeA)

        val threadA = Thread {
            var n = 200
            while (n > 0) {
                userParser(expectedTimeA)
                Thread.sleep((10 * Math.random()).toLong())
                n--
            }
        }

        val threadB = Thread {
            var n = 200
            while (n > 0) {
                useGson(expectedTimeA)
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
    fun multiThreadstressTestApi() {
        val server = MockWebServer()

        val messageResponseBodyA = "{\"message\":$jsonMessageA}"
        val messageResponseBodyB = "{\"message\":$jsonMessageB}"

        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {

                return if (request.path!!.contains("channels")) {
                    MockResponse().setBody(messageResponseBodyB)
                } else {
                    MockResponse().setBody(messageResponseBodyA)
                }
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

        genThreads({
            assert(
                api.getMessage("x").execute().data(), expectedTimeA
            )
            assert(
                api.sendMessage("type", "id", Message("x")).execute().data(),
                expectedTimeB
            )
        }, 5)
    }

    private fun genThreads(task: () -> Unit, threadsCount: Int = 10) {
        var count = threadsCount
        val threads = mutableListOf<Thread>()
        while (count > 0) {
            val thread = genThread(task, count)
            threads.add(thread)
            count--
        }

        threads.forEach {
            it.join()
        }
    }

    private fun genThread(task: () -> Unit, id: Int): Thread {
        val result = Thread {
            var n = 500
            while (n > 0) {
                task()
                Thread.sleep((10 * Math.random()).toLong())
                n--
            }
        }
        result.name = "test-t: $id"
        result.start()
        return result
    }

    private fun useGson(expectedTime: Long) {
        val message = parser.gson.fromJson(jsonMessageA, Message::class.java)
        assert(message, expectedTime)
    }

    private fun userParser(expectedTime: Long) {
        val message = parser.fromJson(jsonMessageA, Message::class.java)
        assert(message, expectedTime)
    }

    private fun assert(message: Message, expectedTime: Long) {
        val createdAt = message.createdAt
        val time = createdAt!!.time
        assertThat(time).isEqualTo(expectedTime)
    }
}