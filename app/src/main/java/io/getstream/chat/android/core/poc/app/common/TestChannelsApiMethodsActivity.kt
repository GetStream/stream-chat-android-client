package io.getstream.chat.android.core.poc.app.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import io.getstream.chat.android.core.poc.common.extensions.echoResult
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.requests.QuerySort
import kotlinx.android.synthetic.main.activity_test_api.*

class TestChannelsApiMethodsActivity : AppCompatActivity() {

    val client = App.client
    val channelId = "general"
    val channelType = "team"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_api)

        buttonsContainer.children.iterator().forEach {
            it.isEnabled = false
        }

        client.setUser(User("bender"), object : TokenProvider {
            override fun getToken(listener: TokenProvider.TokenProviderListener) {
                listener.onSuccess("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ")
            }
        }) {
            echoResult(it, "Connected", "Socket connection error")
            initButtons()
        }
    }

    private fun initButtons() {

        buttonsContainer.children.iterator().forEach {
            it.isEnabled = true
        }

        btnQueryChannels.setOnClickListener { queryChannels() }
        btnUpdateChannel.setOnClickListener { updateChannel() }
        btnStopWatching.setOnClickListener { stopWatching() }
        btnAcceptInvite.setOnClickListener { acceptInvite() }
        btnRejectInvite.setOnClickListener { rejectInvite() }
        btnHideChannel.setOnClickListener { hideChannel() }
        btnShowChannel.setOnClickListener { showChannel() }
    }

    private fun showChannel(){
        client.showChannel(channelType, channelId).enqueue {
            echoResult(it)
        }
    }

    private fun hideChannel(){
        client.hideChannel(channelType, channelId).enqueue {
            echoResult(it)
        }
    }

    private fun rejectInvite() {
        client.rejectInvite(channelType, channelId).enqueue {
            echoResult(it)
        }
    }

    private fun acceptInvite() {
        client.acceptInvite(channelType, channelId, "hello-accept").enqueue {
            echoResult(it)
        }
    }

    private fun updateChannel() {
        val message = Message()
        message.text = "Hello"
        client.updateChannel(channelType, channelId, message).enqueue {
            echoResult(it)
        }
    }

    private fun queryChannels() {
        client.queryChannels(
            QueryChannelsRequest(
                FilterObject(),
                QuerySort()
            ).withLimit(1)
        ).enqueue {
            echoResult(it)
        }
    }

    private fun stopWatching() {
        client.stopWatching(channelType, channelId).enqueue {
            echoResult(it)
        }
    }
}