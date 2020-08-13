package io.getstream.chat.android.client.notifications

import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent

interface ChatNotifications {

    fun onSetUser()

    fun setFirebaseToken(firebaseToken: String)

    fun onFirebaseMessage(message: RemoteMessage)

    fun onChatEvent(event: ChatEvent)

    fun onFirebaseMessageHandled(event: NewMessageEvent)

    fun onFirebaseMessageHandlingFallback()
}
