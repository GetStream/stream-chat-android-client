package io.getstream.chat.android.core.poc.library

import android.text.TextUtils
import android.util.Log
import io.getstream.chat.android.core.poc.common.extensions.safeLet
import io.getstream.chat.android.core.poc.library.TokenProvider.TokenProviderListener
import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.rest.UpdateChannelRequest
import io.getstream.chat.android.core.poc.library.socket.ChatObservable
import io.getstream.chat.android.core.poc.library.socket.ChatSocketConnectionImpl
import io.getstream.chat.android.core.poc.library.socket.ConnectionData
import java.util.UUID.randomUUID

class StreamChatClient(
    val apiKey: String,
    private val apiOptions: ApiClientOptions
) {

    var clientID = ""
    private lateinit var api: ChatApiImpl
    private var anonymousConnection = false
    private val state = ClientState()
    private var tokenProvider: CachedTokenProvider? = null
    private var cacheUserToken: String? = null
    private var fetchingToken = false
    private var isAnonymous = false

    private val TAG = StreamChatClient::class.java.canonicalName

    private val socket = ChatSocketConnectionImpl(apiKey, apiOptions.wssURL)

    fun setUser(
        user: User,
        provider: TokenProvider,
        callback: (Result<ConnectionData>) -> Unit
    ) {
        anonymousConnection = false

        if (state.user != null) {
            Log.e(
                TAG,
                "setUser was called but a user is already set; this is probably an integration mistake"
            )
            return
        }

        Log.d(this.javaClass.canonicalName, "setting user: " + user.id)
        state.user = user

        initTokenProvider(provider)

        api = ChatApiImpl.provideChatApi(apiKey, apiOptions, tokenProvider, isAnonymous)

        tokenProvider?.let {
            socket.connect(user, it).enqueue { connection ->

                if (connection.isSuccess) {
                    safeLet(
                        connection.data()?.connectionId,
                        connection.data()?.user?.id
                    ) { connectionId, userId ->
                        api.connectionId = connectionId
                        api.userId = userId
                    }
                }

                callback.invoke(connection)
            }
        }
    }

    /**
     * Setup an anonymous session
     */
    fun setAnonymousUser(callback: (Result<ConnectionData>) -> Unit) {
        if (state.user != null) {
            Log.w(TAG, "setAnonymousUser was called but a user is already set")
            return
        }

        anonymousConnection = true
        api = ChatApiImpl.provideChatApi(apiKey, apiOptions, null, true)

        val uuid = randomUUID().toString()

        state.user = User(uuid)
        connect(anonymousConnection, callback)
    }

    /**
     * Setup a temporary guest user
     *
     * @param user Data about this user. IE {name: "john"}
     */
    fun setGuestUser(user: User, callback: (Result<ConnectionData>) -> Unit) {
        //TODO Implement guest user
        /*if (state.user != null) {
            Log.w(TAG, "setGuestUser was called but a user is already set;")
            return
        }

        anonymousConnection = false
        initTokenProvider(provider)

        api = ChatApiImpl.provideChatApi(apiKey, apiOptions, tokenProvider, anonymousConnection)

        api.setGuestUser(apiKey = apiKey, userId = user.id, userName = user.name)
        setUser(user, provider, callback)

        connect(anonymousConnection, callback)*/
    }

    fun events(): ChatObservable {
        return socket.events()
    }

    private fun connect(
        anonymousConnection: Boolean = false,
        callback: (Result<ConnectionData>) -> Unit
    ) {
        Log.i(this.javaClass.canonicalName, "client.connect was called")

        if (anonymousConnection) {
            socket.connect().enqueue { connection ->
                if (connection.isSuccess) {
                    safeLet(
                        connection.data()?.connectionId,
                        connection.data()?.user?.id
                    ) { connectionId, userId ->
                        api.connectionId = connectionId
                        api.userId = userId
                    }
                }

                callback.invoke(connection)
            }
        } else {
            safeLet(state.user, tokenProvider) { user, provider ->
                socket.connect(user, provider).enqueue { connection ->

                    if (connection.isSuccess) {
                        safeLet(
                            connection.data()?.connectionId,
                            connection.data()?.user?.id
                        ) { connectionId, userId ->
                            api.connectionId = connectionId
                            api.userId = userId
                        }
                    }

                    callback.invoke(connection)
                }
            }
        }
    }

    fun getState(): ClientState {
        return state
    }

    fun fromCurrentUser(entity: UserEntity): Boolean {
        val otherUserId = entity.getUserId() ?: return false
        return if (getUser() == null) false else TextUtils.equals(getUserId(), otherUserId)
    }

    fun getUserId() = state.user?.id ?: ""

    fun getUser() = state.user

    fun disconnect() {
        if (state.user == null) {
            //log
        } else {
            //log
        }

        socket.disconnect()

        // unset token facilities
        tokenProvider = null
        fetchingToken = false
        cacheUserToken = ""

        //builtinHandler.dispatchUserDisconnected()
        //for (handler in subRegistry.getSubscribers()) {
        //    handler.dispatchUserDisconnected()
        //}

        // clear local state
        state.reset()
        //activeChannelMap.clear()
    }

    fun showChannel(channelType: String, channelId: String): ChatCall<Unit> {
        return api.showChannel(channelType, channelId)
    }

    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): ChatCall<Unit> {
        return api.hideChannel(channelType, channelId, clearHistory)
    }

    fun stopWatching(channelType: String, channelId: String): ChatCall<Unit> {
        return api.stopWatching(channelType, channelId)
    }

    fun queryChannels(
        request: QueryChannelsRequest
    ): ChatCall<List<Channel>> {
        return api.queryChannels(request).map { response ->
            response.getChannels()
        }
    }

    fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message,
        channelExtraData: Map<String, Any> = emptyMap()
    ): ChatCall<Channel> {
        val request = UpdateChannelRequest(channelExtraData, updateMessage)
        return api.updateChannel(channelType, channelId, request).map { response ->
            response.channel
        }
    }

    fun rejectInvite(channelType: String, channelId: String): ChatCall<Channel> {
        return api.rejectInvite(channelType, channelId)
    }

    fun acceptInvite(channelType: String, channelId: String, message: String): ChatCall<Channel> {
        return api.acceptInvite(channelType, channelId, message)
    }

    fun markAllRead(): ChatCall<Event> {
        return api.markAllRead().map {
            it.event
        }
    }

    fun reconnectWebSocket() {
        if (getUser() == null) {
            return
        }

//        if (webSocketService != null) {
//            return
//        }
        //connectionRecovered()

        //connect(anonymousConnection)
    }

    private fun initTokenProvider(provider: TokenProvider) {
        val listeners = mutableListOf<TokenProviderListener>()

        this.tokenProvider = object : CachedTokenProvider {
            override fun getToken(listener: TokenProviderListener) {
                cacheUserToken?.let { token ->
                    listener.onSuccess(token)
                    return
                }

                if (fetchingToken) {
                    listeners.add(listener)
                    return
                } else {
                    // token is not in cache and there are no in-flight requests, go fetch it
                    Log.d(TAG, "Go get a new token")
                    fetchingToken = true
                }

                provider.getToken(object : TokenProviderListener {
                    override fun onSuccess(token: String) {
                        fetchingToken = false
                        listener.onSuccess(token)
                        listeners.forEach { listener ->
                            listener.onSuccess(token)
                        }
                        listeners.clear()
                    }
                })
            }

            override fun tokenExpired() {
                cacheUserToken = null
            }

        }
    }

    private fun onError(errMsg: String, errCode: Int) {
        /*val subs: List<ClientConnectionCallback> =
            connectSubRegistry.getSubscribers()
            connectSubRegistry.clear()
        for (waiter in subs) {
            waiter.onError(errMsg, errCode)
        }*/
    }
}