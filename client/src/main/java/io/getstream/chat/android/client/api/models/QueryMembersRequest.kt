package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.FilterObject

data class QueryMembersRequest(
    @SerializedName("type")
    val channelType: String,
    @SerializedName("id")
    val channelId: String,
    @IgnoreSerialisation
    var filter: FilterObject,
    val offset: Int,
    val limit: Int,
    @IgnoreSerialisation
    var querySort: QuerySort = QuerySort(),
    val members: List<Member> = emptyList()
) {
    val sort = querySort.data
    val filter_conditions: Map<String, Any> = filter.toMap()
}
