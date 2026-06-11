package com.app.mealmate.data.remote

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GeminiRemoteDataSource(
    private val apiKey: String,
) {
    suspend fun generateShoppingList(prompt: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API key is missing.")
        }

        val encodedKey = URLEncoder.encode(apiKey, Charsets.UTF_8.name())
        val connection = URL("$GENERATE_CONTENT_URL?key=$encodedKey").openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 20_000
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        try {
            connection.outputStream.use { output ->
                output.write(requestBody(prompt).toString().toByteArray(Charsets.UTF_8))
            }

            val responseText = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw IllegalStateException(error ?: "Gemini request failed with ${connection.responseCode}.")
            }

            JSONObject(responseText)
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw IllegalStateException("Gemini returned an empty response.")
        } finally {
            connection.disconnect()
        }
    }

    private fun requestBody(prompt: String): JSONObject {
        return JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt)),
                    ),
                ),
            )
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.2)
                    .put("maxOutputTokens", 700),
            )
    }

    private companion object {
        const val GENERATE_CONTENT_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    }
}
