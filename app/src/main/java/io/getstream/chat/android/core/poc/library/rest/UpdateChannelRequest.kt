package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.Expose
import io.getstream.chat.android.core.poc.library.Message


class UpdateChannelRequest(data: Map<String, Any>, updateMessage: Message) {

    
    var data: Map<String, Any>

    
    var message: Message

    init {
        val mutableMap = data.toMutableMap()
        mutableMap.remove("members")
        this.data = mutableMap
        this.message = updateMessage
    }
}