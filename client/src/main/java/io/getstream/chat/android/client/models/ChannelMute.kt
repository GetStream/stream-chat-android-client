package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ChannelMute(
    val user: User,
    val channel: Channel,
    @SerializedName("created_at")
    val createdAt: Date
)
