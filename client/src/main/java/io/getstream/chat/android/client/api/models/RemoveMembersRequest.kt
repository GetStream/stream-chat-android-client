package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

data class RemoveMembersRequest(
    @SerializedName("remove_members")
    val members: List<String>
)
