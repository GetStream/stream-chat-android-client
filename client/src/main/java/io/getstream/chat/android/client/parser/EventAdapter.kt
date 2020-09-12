package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.events.ChannelCreatedEvent
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelMuteEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUnmuteEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChannelsMuteEvent
import io.getstream.chat.android.client.events.ChannelsUnmuteEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserMutedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUnmutedEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.UsersMutedEvent
import io.getstream.chat.android.client.events.UsersUnmutedEvent
import io.getstream.chat.android.client.models.EventType
import java.util.Date

internal class EventAdapter(
    private val gson: Gson,
    private val chatEventAdapter: TypeAdapter<ChatEvent>
) : TypeAdapter<ChatEvent>() {

    override fun write(out: JsonWriter, value: ChatEvent?) {
        chatEventAdapter.write(out, value)
    }

    override fun read(reader: JsonReader): ChatEvent {

        /**
         * A workaround
         *
         * JsonReader can be read only once
         * But it's required to read type of event before parsing actual event
         * Hence:
         * 1. read as [HashMap]
         * 2. get event.type
         * 3. convert [HashMap] to [String]
         * 4. parse actual event from [String]
         */

        val mapAdapter = gson.getAdapter(HashMap::class.java)

        val mapData = mapAdapter.read(reader) as HashMap<*, *>
        val type = mapData["type"] as String
        val data = gson.toJson(mapData)

        return when (type) {

            EventType.HEALTH_CHECK -> {

                /**
                 * [HealthEvent] and [ConnectedEvent] have the same type [EventType.HEALTH_CHECK]
                 */

                if (mapData.containsKey("me")) {
                    gson.fromJson(data, ConnectedEvent::class.java)
                } else {
                    gson.fromJson(data, HealthEvent::class.java)
                }
            }

            //region Messages

            EventType.MESSAGE_NEW -> {
                gson.fromJson(data, NewMessageEvent::class.java)
            }
            EventType.MESSAGE_DELETED -> {
                gson.fromJson(data, MessageDeletedEvent::class.java)
            }
            EventType.MESSAGE_UPDATED -> {
                gson.fromJson(data, MessageUpdatedEvent::class.java)
            }
            EventType.MESSAGE_READ -> {
                gson.fromJson(data, MessageReadEvent::class.java)
            }

            //region Typing

            EventType.TYPING_START -> {
                gson.fromJson(data, TypingStartEvent::class.java)
            }
            EventType.TYPING_STOP -> {
                gson.fromJson(data, TypingStopEvent::class.java)
            }

            //region Reactions

            EventType.REACTION_NEW -> {
                gson.fromJson(data, ReactionNewEvent::class.java)
            }
            EventType.REACTION_UPDATED -> {
                gson.fromJson(data, ReactionUpdateEvent::class.java)
            }
            EventType.REACTION_DELETED -> {
                gson.fromJson(data, ReactionDeletedEvent::class.java)
            }

            //region Members

            EventType.MEMBER_ADDED -> {
                gson.fromJson(data, MemberAddedEvent::class.java)
            }
            EventType.MEMBER_REMOVED -> {
                gson.fromJson(data, MemberRemovedEvent::class.java)
            }
            EventType.MEMBER_UPDATED -> {
                gson.fromJson(data, MemberUpdatedEvent::class.java)
            }

            //region Channels

            EventType.CHANNEL_CREATED -> {
                gson.fromJson(data, ChannelCreatedEvent::class.java)
            }
            EventType.CHANNEL_UPDATED -> {
                gson.fromJson(data, ChannelUpdatedEvent::class.java)
            }
            EventType.CHANNEL_HIDDEN -> {
                gson.fromJson(data, ChannelHiddenEvent::class.java)
            }
            EventType.CHANNEL_MUTED -> {
                if (mapData.containsKey("mute")) {
                    gson.fromJson(data, ChannelMuteEvent::class.java)
                } else {
                    gson.fromJson(data, ChannelsMuteEvent::class.java)
                }
            }
            EventType.CHANNEL_UNMUTED -> {
                if (mapData.containsKey("mute")) {
                    gson.fromJson(data, ChannelUnmuteEvent::class.java)
                } else {
                    gson.fromJson(data, ChannelsUnmuteEvent::class.java)
                }
            }
            EventType.CHANNEL_DELETED -> {
                gson.fromJson(data, ChannelDeletedEvent::class.java)
            }

            EventType.CHANNEL_VISIBLE -> {
                gson.fromJson(data, ChannelVisibleEvent::class.java)
            }

            EventType.CHANNEL_TRUNCATED -> {
                gson.fromJson(data, ChannelTruncatedEvent::class.java)
            }

            //region Watching

            EventType.USER_WATCHING_START -> {
                gson.fromJson(data, UserStartWatchingEvent::class.java)
            }
            EventType.USER_WATCHING_STOP -> {
                gson.fromJson(data, UserStopWatchingEvent::class.java)
            }

            //region Notifications

            EventType.NOTIFICATION_ADDED_TO_CHANNEL -> {
                gson.fromJson(data, NotificationAddedToChannelEvent::class.java)
            }

            EventType.NOTIFICATION_MARK_READ -> {
                gson.fromJson(data, NotificationMarkReadEvent::class.java)
            }

            EventType.NOTIFICATION_MESSAGE_NEW -> {
                gson.fromJson(data, NotificationMessageNewEvent::class.java)
            }

            EventType.NOTIFICATION_INVITED -> {
                gson.fromJson(data, NotificationInvitedEvent::class.java)
            }

            EventType.NOTIFICATION_INVITE_ACCEPTED -> {
                gson.fromJson(data, NotificationInviteAcceptedEvent::class.java)
            }

            EventType.NOTIFICATION_REMOVED_FROM_CHANNEL -> {
                gson.fromJson(data, NotificationRemovedFromChannelEvent::class.java)
            }

            EventType.NOTIFICATION_MUTES_UPDATED -> {
                gson.fromJson(data, NotificationMutesUpdatedEvent::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED -> {
                gson.fromJson(data, NotificationChannelMutesUpdatedEvent::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_DELETED -> {
                gson.fromJson(data, NotificationChannelDeletedEvent::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_TRUNCATED -> {
                gson.fromJson(data, NotificationChannelTruncatedEvent::class.java)
            }

            EventType.USER_PRESENCE_CHANGED -> {
                gson.fromJson(data, UserPresenceChangedEvent::class.java)
            }

            EventType.USER_UPDATED -> {
                gson.fromJson(data, UserUpdatedEvent::class.java)
            }

            EventType.USER_DELETED -> {
                gson.fromJson(data, UserDeletedEvent::class.java)
            }

            EventType.USER_MUTED -> {
                if (mapData.containsKey("target_user")) {
                    gson.fromJson(data, UserMutedEvent::class.java)
                } else {
                    gson.fromJson(data, UsersMutedEvent::class.java)
                }
            }

            EventType.USER_UNMUTED -> {
                if (mapData.containsKey("target_user")) {
                    gson.fromJson(data, UserUnmutedEvent::class.java)
                } else {
                    gson.fromJson(data, UsersUnmutedEvent::class.java)
                }
            }

            EventType.USER_BANNED -> {
                if (mapData.containsKey("cid")) {
                    gson.fromJson(data, ChannelUserBannedEvent::class.java)
                } else {
                    gson.fromJson(data, GlobalUserBannedEvent::class.java)
                }
            }

            EventType.USER_UNBANNED -> {
                if (mapData.containsKey("cid")) {
                    gson.fromJson(data, ChannelUserUnbannedEvent::class.java)
                } else {
                    gson.fromJson(data, GlobalUserUnbannedEvent::class.java)
                }
            }
            else -> {
                UnknownEvent(mapData["type"]?.toString() ?: EventType.UNKNOWN, Date(), mapData)
            }
        }
    }
}
