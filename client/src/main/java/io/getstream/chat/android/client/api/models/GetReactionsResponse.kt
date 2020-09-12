package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Reaction

data class GetReactionsResponse(val reactions: List<Reaction> = emptyList())
