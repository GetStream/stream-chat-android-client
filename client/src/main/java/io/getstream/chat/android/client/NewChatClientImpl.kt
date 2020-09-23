package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.token.TokenProvider

private const val NO_STORAGE_MESSAGE = "No storage was provided. If you want to have storage, provide a storageClient!"

/*
* That's the only implementation of Chat. Is shouldn't contain any business logic, this class should follow the Delegation patter.
* The only responsibility of this class is to pass all data to the correct object to handle a specific request, nothing more.
*
* This class only defines a contract with the classes inside it, but the actual implementation lives outside of it.
*
* If some implementation is not available for this client, than no delegation is done, this way new behaviour can be created
* by passing different implementations which conforms to the Open Close Principle. This avoid duplication of code and it uses
* composition over inheritance.
*/

class NewChatClientImpl(
    private val api: ChatApi,
    private val userInitiallizer: UserInitializer,
    private val storageClient: StorageClient?
) : NewChatClient {

    override fun setUser(user: User, token: String, listener: InitConnectionListener?) {
        userInitiallizer.init(user, token, listener)
    }

    override fun setUser(user: User, tokenProvider: TokenProvider, listener: InitConnectionListener?) {
        userInitiallizer.init(user, tokenProvider, listener)
    }

    override fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message> {
        storageClient?.storeLastMessage(channelType, channelId, message)
        return api.sendMessage(channelType, channelId, message)
    }

    //If client would like to use storage, all the logic lives inside the StorageClient.
    override fun getStorageClient(): StorageClient = storageClient ?: throw IllegalStateException(NO_STORAGE_MESSAGE)
}
