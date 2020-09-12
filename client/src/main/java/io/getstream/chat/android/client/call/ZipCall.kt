package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

internal object ZipCall {

    fun <A, B, C> zip(callA: Call<A>, callB: Call<B>, callC: Call<C>): Call<Triple<A, B, C>> {
        return object : ChatCallImpl<Triple<A, B, C>>() {
            override fun execute(): Result<Triple<A, B, C>> {
                val resultA = callA.execute()

                if (!resultA.isSuccess) return Result(null, resultA.error())
                val resultB = callB.execute()
                if (!resultB.isSuccess) return Result(null, resultB.error())
                val resultC = callC.execute()
                if (!resultC.isSuccess) return Result(null, resultC.error())

                return Result(Triple(resultA.data(), resultB.data(), resultC.data()), null)
            }

            override fun enqueue(callback: (Result<Triple<A, B, C>>) -> Unit) {
                callA.enqueue { resultA ->

                    if (!resultA.isSuccess) {
                        callback(Result(null, resultA.error()))
                    } else {
                        callB.enqueue { resultB ->
                            if (!resultB.isSuccess) {
                                callback(Result(null, resultB.error()))
                            } else {

                                callC.enqueue { resultC ->
                                    if (!resultC.isSuccess) {
                                        callback(Result(null, resultC.error()))
                                    } else {

                                        val dataA = resultA.data()
                                        val dataB = resultB.data()
                                        val dataC = resultC.data()

                                        callback(Result(Triple(dataA, dataB, dataC), null))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun <A, B> zip(callA: Call<A>, callB: Call<B>): Call<Pair<A, B>> {
        return object : ChatCallImpl<Pair<A, B>>() {
            override fun execute(): Result<Pair<A, B>> {
                val resultA = callA.execute()

                if (!resultA.isSuccess) return getErrorA(resultA)
                val resultB = callB.execute()
                if (!resultB.isSuccess) return getErrorB(resultB)

                return Result(Pair(resultA.data(), resultB.data()), null)
            }

            override fun enqueue(callback: (Result<Pair<A, B>>) -> Unit) {
                callA.enqueue { resultA ->

                    if (!resultA.isSuccess) {
                        callback(getErrorA(resultA))
                    } else {
                        callB.enqueue { resultB ->
                            if (!resultB.isSuccess) {
                                callback(getErrorB(resultB))
                            } else {

                                val dataA = resultA.data()
                                val dataB = resultB.data()

                                callback(Result(Pair(dataA, dataB), null))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun <A, B> getErrorA(resultA: Result<A>): Result<Pair<A, B>> {
        return Result(
            null,
            ChatError("Error executing callA", resultA.error())
        )
    }

    private fun <A, B> getErrorB(resultB: Result<B>): Result<Pair<A, B>> {
        return Result(
            null,
            ChatError("Error executing callB", resultB.error())
        )
    }
}
