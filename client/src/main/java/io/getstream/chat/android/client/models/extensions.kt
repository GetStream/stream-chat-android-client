package io.getstream.chat.android.client.models

internal const val EXTRA_IMAGE = "image"
internal const val EXTRA_NAME = "name"

fun Message.getTranslation(language: String): String {
    return i18n.get("${language}_text", "")
}

val Message.originalLanguage
    get() = i18n.get("language", "")

fun Channel.getUnreadMessagesCount(forUserId: String = ""): Int {

    return if (forUserId.isEmpty()) {
        read
            .sumBy { it.unreadMessages }
    } else {
        read
            .filter { it.user.id == forUserId }
            .sumBy { it.unreadMessages }
    }
}

var User.image: String
    get() = getExternalField(this, EXTRA_IMAGE)
    set(value) {
        extraData[EXTRA_IMAGE] = value
    }

var User.name: String
    get() = getExternalField(this, EXTRA_NAME)
    set(value) {
        extraData[EXTRA_NAME] = value
    }

var Channel.image: String
    get() = getExternalField(this, EXTRA_IMAGE)
    set(value) {
        extraData[EXTRA_IMAGE] = value
    }

var Channel.name: String
    get() = getExternalField(this, EXTRA_NAME)
    set(value) {
        extraData[EXTRA_NAME] = value
    }

internal fun getExternalField(obj: CustomObject, key: String): String {

    val value = obj.extraData[key]
    val emptyResult = ""

    return if (value == null) {
        emptyResult
    } else {
        if (value is String) {
            value
        } else {
            emptyResult
        }
    }
}

internal fun <A, B> Map<A, B>.get(key: A, default: B): B {
    return get(key) ?: default
}
