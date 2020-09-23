package io.getstream.chat.android.client

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message

/*
* Although this interface is defined inside the LLC client, the implementation lives inside the LiveData client.
* This way a user can change between use the LiveData library, or to provide its own storage accordingly to its needs.
* The LLC just defines the contract for its functionalities, but no implementation, this should be provided some where else.
*/
interface StorageClient {

    fun storeChannelState(channel: Channel)

    //Just a method to illustrate a concept
    fun storeLastMessage(channelType: String, channelId: String, message: Message)

    //Here comes a some methods related to the behaviour of the storage.
}
