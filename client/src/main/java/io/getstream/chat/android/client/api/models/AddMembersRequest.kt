package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

data class AddMembersRequest(
    @SerializedName("add_members")
    val members: List<String>
)
