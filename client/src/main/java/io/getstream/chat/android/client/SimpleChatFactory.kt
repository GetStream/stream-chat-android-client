package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.ChatApi

class SimpleChatFactory {
    fun create() =
        NewChatClientImpl(api = ChatApi(), userInitiallizer = UserInitializer(), storageClient = null)
}
