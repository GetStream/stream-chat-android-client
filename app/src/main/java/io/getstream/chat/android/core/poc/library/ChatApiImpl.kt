package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.api.QueryChannelsResponse
import io.getstream.chat.android.core.poc.library.api.RetrofitClient
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.rest.ChannelQueryRequest
import io.getstream.chat.android.core.poc.library.rest.ChannelResponse
import io.getstream.chat.android.core.poc.library.rest.EventResponse
import io.getstream.chat.android.core.poc.library.rest.UpdateChannelRequest

class ChatApiImpl constructor(
    private val apiKey: String,
    private val retrofitApi: RetrofitApi
) {

    companion object {
        fun provideChatApi(
            apiKey: String,
            apiClientOptions: ApiClientOptions,
            tokenProvider: CachedTokenProvider?,
            isAnonymous: Boolean
        ): ChatApiImpl {

            val client = RetrofitClient.getClient(
                apiClientOptions,
                tokenProvider,
                isAnonymous
            ).create(
                RetrofitApi::class.java
            )

            return ChatApiImpl(apiKey, client)
        }
    }

    var userId: String = ""
    var connectionId: String = ""

    private val callMapper = RetrofitCallMapper()

    fun queryChannels(query: QueryChannelsRequest): ChatCall<QueryChannelsResponse> {
        return callMapper.map(
            retrofitApi.queryChannels(
                apiKey,
                userId,
                connectionId,
                query
            )
        )
    }

    fun stopWatching(
        channelType: String,
        channelId: String
    ): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.stopWatching(channelType, channelId, apiKey, connectionId, emptyMap())
        ).map {
            Unit
        }
    }

    fun queryChannel(
        query: ChannelQueryRequest,
        channelType: String,
        channelId: String = ""
    ): ChatCall<ChannelState> {

        if (channelId.isEmpty()) {
            return callMapper.map(
                retrofitApi.queryChannel(
                    channelType,
                    apiKey,
                    userId,
                    connectionId,
                    query
                )
            )
        } else {
            return callMapper.map(
                retrofitApi.queryChannel(
                    channelType,
                    channelId,
                    apiKey,
                    userId,
                    connectionId,
                    query
                )
            )
        }
    }

    fun updateChannel(
        channelType: String,
        channelId: String,
        request: UpdateChannelRequest
    ): ChatCall<ChannelResponse> {
        return callMapper.map(
            retrofitApi.updateChannel(
                channelType,
                channelId,
                apiKey,
                connectionId,
                request
            )
        )
    }

    fun acceptInvite() {

    }

    fun deleteChannel(channelType: String, channelId: String): ChatCall<ChannelResponse> {
        return callMapper.map(
            retrofitApi.deleteChannel(channelType, channelId, apiKey, connectionId)
        )
    }

    fun markAllRead(): ChatCall<EventResponse> {
        return callMapper.map(
            retrofitApi.markAllRead(
                apiKey,
                userId,
                connectionId
            )
        )
    }

}