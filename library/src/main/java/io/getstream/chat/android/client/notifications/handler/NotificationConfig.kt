package io.getstream.chat.android.client.notifications.handler

import io.getstream.chat.android.client.R

data class NotificationConfig(
    val notificationChannelId: Int = R.string.stream_chat_notification_channel_id,
    val notificationChannelName: Int = R.string.stream_chat_notification_channel_name,
    val smallIcon: Int = R.drawable.stream_ic_notification,
    val firebaseMessageIdKey: String = "stream-chat-message-id",
    val firebaseChannelIdKey: String = "stream-chat-channel-id",
    val firebaseChannelTypeKey: String = "stream-chat-channel-type",
    val errorCaseNotificationTitle: Int = R.string.stream_chat_notification_title,
    val errorCaseNotificationContent: Int = R.string.stream_chat_notification_content
)