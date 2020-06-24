package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import okhttp3.HttpUrl


internal class ChatClientConfig(
    val apiKey: String,
    var httpUrl: HttpUrl,
    var cdnHttpUrl: HttpUrl,
    var wssUrl: String,
    var baseTimeout: Long,
    var cdnTimeout: Long,
    val loggerConfig: ChatLogger.Config,
    val notificationsConfig: ChatNotificationConfig? = null,
    val clientVersion: String = ""
) {
    val tokenManager: TokenManager = TokenManagerImpl()
    var isAnonymous: Boolean = false
}