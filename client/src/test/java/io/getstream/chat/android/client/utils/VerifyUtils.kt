package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatNetworkError
import org.assertj.core.api.Assertions

fun <T> verifyError(result: Result<T>, statusCode: Int) {
    Assertions.assertThat(result.isSuccess).isFalse()
    Assertions.assertThat(result.error()).isInstanceOf(ChatNetworkError::class.java)

    val error = result.error() as ChatNetworkError
    Assertions.assertThat(error.statusCode).isEqualTo(statusCode)
}

fun <T> verifySuccess(result: Result<T>, equalsTo: T) {
    Assertions.assertThat(result.isSuccess).isTrue()
    Assertions.assertThat(result.data()).isEqualTo(equalsTo)
}
