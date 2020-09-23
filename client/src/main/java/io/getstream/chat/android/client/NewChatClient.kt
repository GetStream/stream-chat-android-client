package io.getstream.chat.android.client

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.controllers.ChannelController
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.GuestUser
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatObservable
import java.io.File
import java.util.Date

interface NewChatClient {

    fun setUser(user: User, token: String, listener: InitConnectionListener? = null)

    fun setUser(user: User, tokenProvider: TokenProvider, listener: InitConnectionListener? = null)

    fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message>

    fun getStorageClient(): StorageClient

    //Code is simplified.
}
