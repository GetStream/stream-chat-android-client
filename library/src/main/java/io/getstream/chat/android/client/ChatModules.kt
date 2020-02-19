package io.getstream.chat.android.client

import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.client.api.*
import io.getstream.chat.android.client.api.models.RetrofitApi
import io.getstream.chat.android.client.api.models.RetrofitCdnApi
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.ChatNotificationsImpl
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

open class ChatModules(val config: ChatClientConfig) {

    private val defaultParser by lazy { ChatParserImpl() }
    private val defaultLogger by lazy { ChatLogger.Builder().level(config.logLevel).build() }
    private val defaultNotifications by lazy {
        buildNotification(
            config.notificationsConfig,
            api()
        )
    }
    private val defaultApi by lazy { buildApi(config, parser()) }
    private val defaultSocket by lazy { buildSocket(config, parser(), logger()) }

    //region Modules

    open fun api(): ChatApi {
        return defaultApi
    }

    open fun socket(): ChatSocket {
        return defaultSocket
    }

    open fun parser(): ChatParser {
        return defaultParser
    }

    open fun logger(): ChatLogger {
        return defaultLogger
    }

    open fun notifications(): ChatNotifications {
        return defaultNotifications
    }

    //endregion

    private fun buildNotification(
        config: ChatNotificationConfig,
        api: ChatApi
    ): ChatNotifications {
        return ChatNotificationsImpl(config, api, config.context)
    }

    private fun buildRetrofit(
        endpoint: String,
        connectTimeout: Long,
        writeTimeout: Long,
        readTimeout: Long,
        config: ChatClientConfig,
        parser: ChatParser
    ): Retrofit {

        val clientBuilder = OkHttpClient.Builder()
            .followRedirects(false)
            // timeouts
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            // interceptors
            .addInterceptor(HeadersInterceptor(config))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(TokenAuthInterceptor(config, parser))
            .addNetworkInterceptor(StethoInterceptor())

        val builder = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(clientBuilder.build())

        return parser.configRetrofit(builder).build()
    }

    private fun buildSocket(
        chatConfig: ChatClientConfig,
        parser: ChatParser,
        logger: ChatLogger
    ): ChatSocket {
        return ChatSocketImpl(
            chatConfig.apiKey,
            chatConfig.wssUrl,
            chatConfig.tokenProvider,
            parser,
            logger
        )
    }

    private fun buildApi(
        chatConfig: ChatClientConfig,
        parser: ChatParser
    ): ChatApi {
        return ChatApiImpl(
            buildRetrofitApi(),
            buildRetrofitCdnApi(),
            chatConfig,
            parser
        )
    }

    private fun buildRetrofitApi(): RetrofitApi {
        return buildRetrofit(
            config.httpUrl,
            config.baseTimeout,
            config.baseTimeout,
            config.baseTimeout,
            config,
            parser()
        ).create(RetrofitApi::class.java)
    }

    private fun buildRetrofitCdnApi(): RetrofitCdnApi {
        return buildRetrofit(
            config.cdnHttpUrl,
            config.cdnTimeout,
            config.cdnTimeout,
            config.cdnTimeout,
            config,
            parser()
        ).create(RetrofitCdnApi::class.java)
    }
}