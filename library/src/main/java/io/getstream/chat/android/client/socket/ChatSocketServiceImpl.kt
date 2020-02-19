package io.getstream.chat.android.client.socket

import android.os.Message
import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

class ChatSocketServiceImpl(
    val chatParser: ChatParser,
    val logger: ChatLogger?
) : ChatSocketService {

    private var wsEndpoint: String = ""
    private var apiKey: String = ""
    private var userToken: String? = ""
    private var user: User? = null
    private val eventsParser: EventsParser = EventsParser(this, chatParser, logger)
    private var httpClient = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val listeners = mutableListOf<SocketListener>()

    private var wsId = 0
    private val eventHandler = EventHandler(this)
    private val healthMonitor = HealthMonitor(this, logger)

    internal var state: State = State.Disconnected

    override fun removeListener(listener: SocketListener) {
        listeners.remove(listener)
        logger?.logD(this, "SocketListener removed. Total listeners: ${listeners.size}")
    }

    override fun addListener(listener: SocketListener) {
        listeners.add(listener)
        logger?.logD(this, "SocketListener added. Total listeners: ${listeners.size}")
    }

    override fun connect(
        wsEndpoint: String,
        apiKey: String,
        user: User?,
        userToken: String?
    ) {
        logger?.logD(this, "Socket connecting...")
        if (state is State.Connecting || state is State.Connected) {
            disconnect()
        }

        this.wsEndpoint = wsEndpoint
        this.apiKey = apiKey
        this.user = user
        this.userToken = userToken
        wsId = 0
        healthMonitor.reset()

        setupWS()
    }

    override fun disconnect() {
        updateState(State.Disconnected)
        clearWebSocket()
        logger?.logD(this, "Socket disconnected.")
    }

    fun setLastEventDate(date: Date) {
        healthMonitor.lastEventDate = date
    }

    fun onSocketError(error: ChatError) {
        logger?.logE(this, "Socket error: ${error.message}")

        if (state is State.Connected || state is State.Connecting) {
            updateState(State.Error(error))
            updateState(State.Disconnected)
            clearWebSocket()
            healthMonitor.onError()
        }
    }

    fun onRemoteEvent(event: ChatEvent) {
        listeners.forEach { it.onEvent(event) }
    }

    fun onConnectionResolved(event: ConnectedEvent) {
        updateState(State.Connected(event))
        startMonitor()
    }

    fun onEvent(event: ChatEvent) {
        val eventMsg = Message()
        eventMsg.obj = event
        eventHandler.sendMessage(eventMsg)
    }

    internal fun sendEvent(event: ChatEvent) {
        webSocket?.send(chatParser.toJson(event))
    }

    internal fun setupWS() {

        updateState(State.Connecting)

        wsId++
        val url = getWsUrl()
        logger?.logI(this, "Created WS Url: $url")

        val request: Request = Request.Builder().url(url).build()

        httpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()

        webSocket = httpClient.newWebSocket(request, eventsParser)
        //webSocket = StethoWebSocketsFactory(httpClient).newWebSocket(request, eventsParser)       //FIXME Does we need this?
        logger?.logD(this, "Socket created")
    }

    private fun startMonitor() {
        healthMonitor.start()
    }

    private fun clearWebSocket() {
        logger?.logD(this, "Socked clearing")
        healthMonitor.reset()
        webSocket?.cancel()
        webSocket?.close(1000, "bye")
        webSocket = null
    }

    private fun updateState(state: State) {

        this.state = state

        when (state) {
            is State.Error -> {
                eventHandler.post {
                    listeners.forEach { it.onError(state.error) }
                }
            }
            is State.Connecting -> {
                eventHandler.post {
                    listeners.forEach { it.onConnecting() }
                }
            }
            is State.Connected -> {
                eventHandler.post {
                    listeners.forEach { it.onConnected(state.event) }
                }
            }
            is State.Disconnected -> {
                eventHandler.post {
                    listeners.forEach { it.onDisconnected() }
                }
            }
        }
    }

    private fun getWsUrl(): String {
        var json = buildUserDetailJson(user)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val baseWsUrl: String =
                wsEndpoint + "connect?json=" + json + "&api_key=" + apiKey
            if (user == null) {
                "$baseWsUrl&stream-auth-type=anonymous"
            } else {
                "$baseWsUrl&authorization=$userToken&stream-auth-type=jwt"
            }
        } catch (throwable: Throwable) {
            throw UnsupportedEncodingException("Unable to encode user details json: $json")
        }
    }

    private fun buildUserDetailJson(user: User?): String {
        val data = mutableMapOf<String, Any>()
        user?.let {
            data["user_details"] = user
            data["user_id"] = user.id
        }
        data["server_determines_connection_id"] = true
        return chatParser.toJson(data)
    }

    sealed class State {
        object Disconnected : State()
        object Connecting : State()
        class Connected(val event: ConnectedEvent) : State()
        class Error(val error: ChatError) : State()
    }

}