package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.CustomObject
import io.getstream.chat.android.client.parser.adapters.CustomObjectGsonAdapter
import io.getstream.chat.android.client.parser.adapters.QuerySortAdapter
import io.getstream.chat.android.client.utils.FilterObject
import java.util.*


class TypeAdapterFactory : com.google.gson.TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {

        if (type.rawType.interfaces.contains(CustomObject::class.java)) {
            return CustomObjectGsonAdapter(gson, type.rawType) as TypeAdapter<T>
        }

        return when (type.rawType) {
            ChatEvent::class.java -> {
                EventAdapter(
                    gson,
                    gson.getDelegateAdapter(this, type) as TypeAdapter<ChatEvent>
                ) as TypeAdapter<T>
            }
            Date::class.java -> {
                DateAdapter() as TypeAdapter<T>
            }
            FilterObject::class.java -> {
                FilterObjectAdapter(gson) as TypeAdapter<T>
            }
            QuerySortAdapter::class.java -> {
                QuerySortAdapter(gson) as TypeAdapter<T>
            }
            else -> {
                null
            }
        }
    }


}


