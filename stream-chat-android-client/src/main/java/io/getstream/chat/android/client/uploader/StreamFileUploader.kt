package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.api.models.ProgressRequestBody
import io.getstream.chat.android.client.api.models.RetroProgressCallback
import io.getstream.chat.android.client.extensions.getMediaType
import io.getstream.chat.android.client.utils.ProgressCallback
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

internal class StreamFileUploader(
    private val apiKey: String,
    private val retrofitCdnApi: RetrofitCdnApi
) : FileUploader {

    private var userId: String = ""
    private var connectionId: String = ""

    override fun setConnection(userId: String, connectionId: String) {
        this.userId = userId
        this.connectionId = connectionId
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ) {
        val body = ProgressRequestBody(file, callback)
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        retrofitCdnApi
            .sendFile(
                channelType,
                channelId,
                part,
                apiKey,
                userId,
                connectionId
            )
            .call
            .enqueue(RetroProgressCallback(callback))
    }

    override fun sendFile(channelType: String, channelId: String, file: File): String? {
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody(file.getMediaType())
        )

        val result = retrofitCdnApi.sendFile(
            channelType,
            channelId,
            part,
            apiKey,
            userId,
            connectionId
        ).execute()

        return if (result.isSuccess) {
            result.data().file
        } else {
            null
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ) {
        val body = ProgressRequestBody(file, callback)
        val part = MultipartBody.Part.createFormData("file", file.name, body)

        retrofitCdnApi
            .sendImage(
                channelType,
                channelId,
                part,
                apiKey,
                userId,
                connectionId
            )
            .call
            .enqueue(RetroProgressCallback(callback))
    }

    override fun sendImage(channelType: String, channelId: String, file: File): String? {
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody(file.getMediaType())
        )

        val result = retrofitCdnApi.sendImage(
            channelType,
            channelId,
            part,
            apiKey,
            userId,
            connectionId
        ).execute()

        return if (result.isSuccess) {
            result.data().file
        } else {
            null
        }
    }

    override fun deleteFile(channelType: String, channelId: String, url: String) {
        retrofitCdnApi.deleteFile(channelType, channelId, apiKey, connectionId, url).execute()
    }

    override fun deleteImage(channelType: String, channelId: String, url: String) {
        retrofitCdnApi.deleteImage(channelType, channelId, apiKey, connectionId, url).execute()
    }
}
