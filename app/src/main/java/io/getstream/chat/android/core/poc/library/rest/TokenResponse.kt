package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.User


data class TokenResponse(
    @SerializedName("user")
    var user: User,

    @SerializedName("access_token")
    val accessToken: String? = null
)