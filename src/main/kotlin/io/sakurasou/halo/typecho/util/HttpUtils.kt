package io.sakurasou.halo.typecho.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * @author mashirot
 * 2024/5/8 23:25
 */
object HttpUtils {
    private val client = OkHttpClient()

    fun sendPostReq(url: String, body: String, pat: String) {
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val reqBody = body.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .post(reqBody)
            .addHeader("Authorization", "Bearer $pat")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val respData = response.body?.string()

            // TODO 判断是否成功
        }
    }

    fun sendGetReq(url: String, pat: String): String {
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $pat")
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            response.body?.string() ?: ""
        }
    }
}