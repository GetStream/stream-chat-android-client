package io.getstream.chat.android.core.poc.app.common

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.common.extensions.echoResult
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import kotlinx.android.synthetic.main.activity_test_user_api.*
import org.jetbrains.anko.intentFor

class TestUsersApiMethodsActivity : AppCompatActivity() {

    private val client = App.client

    companion object {
        fun getIntent(context: Context) = context.intentFor<TestUsersApiMethodsActivity>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_user_api)
        initViews()
    }

    private fun initViews() {
        testUsersApiSetUserBtn?.setOnClickListener { _ -> setUser() }
        testUsersApiSetGuestUserBtn?.setOnClickListener { _ -> setGuestUser() }
        testUsersApiSetAnonymousUserBtn?.setOnClickListener { _ -> setAnonymousUser() }
    }

    private fun setUser() {
        client.setUser(User("bender"), object : TokenProvider {
            override fun getToken(listener: TokenProvider.TokenProviderListener) {
                listener.onSuccess("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ")
            }
        }) { result ->
            echoResult(result, "Connected", "Socket connection error")
        }
    }

    private fun setGuestUser() {
        client.setGuestUser(User("guest"))
    }

    private fun setAnonymousUser() {
        client.setAnonymousUser()
    }
}