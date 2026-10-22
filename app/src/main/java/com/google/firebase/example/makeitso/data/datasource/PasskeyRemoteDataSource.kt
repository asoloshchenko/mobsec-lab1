package com.google.firebase.example.makeitso.data.datasource

import io.github.jan.supabase.functions.Functions
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

// WebAuthn relying party: the "passkey" Edge Function (supabase/functions/passkey).
// Options are passed to Credential Manager as is, and its JSON responses go back unchanged.
class PasskeyRemoteDataSource @Inject constructor(
    private val functions: Functions
) {
    // Requires a signed-in user: the function reads the user from the JWT
    suspend fun registrationOptions(): String = call("register-options")

    suspend fun verifyRegistration(registrationResponseJson: String) {
        call("register-verify", registrationResponseJson)
    }

    suspend fun authenticationOptions(): String = call("login-options")

    suspend fun verifyAuthentication(authenticationResponseJson: String): PasskeySession {
        return json.decodeFromString(call("login-verify", authenticationResponseJson))
    }

    private suspend fun call(action: String, credentialResponseJson: String? = null): String {
        val body = buildJsonObject {
            put("action", action)
            credentialResponseJson?.let { put("response", Json.parseToJsonElement(it)) }
        }
        return functions.invoke(FUNCTION_NAME, body).bodyAsText()
    }

    @Serializable
    data class PasskeySession(
        @SerialName("access_token") val accessToken: String,
        @SerialName("refresh_token") val refreshToken: String,
        @SerialName("expires_in") val expiresIn: Long,
        @SerialName("token_type") val tokenType: String
    )

    companion object {
        private const val FUNCTION_NAME = "passkey"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
