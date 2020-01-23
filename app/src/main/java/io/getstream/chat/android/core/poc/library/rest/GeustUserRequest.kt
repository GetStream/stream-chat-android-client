package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class GuestUserRequest(val id: String, val name: String?) {

    @Expose
    @SerializedName("user")
    var user: GuestUserBody? = null

    init {
        this.user = GuestUserBody(id, name)
    }

    data class GuestUserBody constructor(
        @SerializedName("id") val id: String,
        @SerializedName("name") var name: String?
    )
}
