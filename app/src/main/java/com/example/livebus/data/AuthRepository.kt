package com.example.livebus.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import com.example.livebus.BuildConfig

data class LoginResult(
    val username: String,
    val role: String // "ADMIN", "DRIVER", "PASSENGER"
)

@Singleton
class AuthRepository @Inject constructor() {

    private fun getHttpBaseUrl(): String {
        val wsUrl = BuildConfig.WEBSOCKET_URL
        val base = wsUrl.replace("ws://", "http://").replace("wss://", "https://")
        return if (base.endsWith("/ws-livebus")) {
            base.substring(0, base.length - "/ws-livebus".length)
        } else {
            base
        }
    }

    suspend fun login(username: String, password: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        try {
            val url = URL("${getHttpBaseUrl()}/api/auth/login")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val jsonBody = JSONObject().apply {
                put("username", username)
                put("password", password)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                val returnedUsername = jsonResponse.getString("username")
                val role = jsonResponse.getString("role")
                Result.success(LoginResult(returnedUsername, role))
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception(errorText.ifEmpty { "Authentication failed with status $responseCode" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
