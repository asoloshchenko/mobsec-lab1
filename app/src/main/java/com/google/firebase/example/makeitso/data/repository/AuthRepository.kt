package com.google.firebase.example.makeitso.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import android.util.Base64
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.example.makeitso.data.datasource.AuthRemoteDataSource
import com.google.firebase.example.makeitso.data.datasource.CredentialManagerDataSource
import com.google.firebase.example.makeitso.data.datasource.PasskeyRemoteDataSource
import com.google.firebase.example.makeitso.data.model.User
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val credentialManagerDataSource: CredentialManagerDataSource,
    private val passkeyRemoteDataSource: PasskeyRemoteDataSource
) {
    // Passkeys in Credential Manager need Android 9+
    val isPasskeySupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    val currentUser: UserInfo? get() = authRemoteDataSource.currentUser
    val sessionStatus: StateFlow<SessionStatus> = authRemoteDataSource.sessionStatus

    suspend fun signIn(email: String, password: String) {
        authRemoteDataSource.signIn(email, password)
    }

    // Registers the account and then offers to store the password in the system password manager.
    // The app itself never persists the password.
    suspend fun signUp(activityContext: Context, email: String, password: String) {
        authRemoteDataSource.signUp(email, password)
        credentialManagerDataSource.savePassword(activityContext, email, password)
    }

    /** Shows saved passwords, passkeys and Google accounts in one dialog. Returns false if the user picked nothing. */
    suspend fun signInWithSavedCredential(activityContext: Context): Boolean {
        val nonce = Nonce()
        val passkeyRequest = if (isPasskeySupported) passkeyAuthenticationOptions() else null
        val credential = credentialManagerDataSource.getSavedCredential(activityContext, nonce.hashed, passkeyRequest)
            ?: return false
        signInWithCredential(credential, nonce)
        return true
    }

    /** Shows the Google account picker. Returns false if the user dismissed it. */
    suspend fun signInWithGoogle(activityContext: Context): Boolean {
        val nonce = Nonce()
        val credential = credentialManagerDataSource.getGoogleCredential(activityContext, nonce.hashed)
            ?: return false
        signInWithCredential(credential, nonce)
        return true
    }

    /** Registers a passkey for the signed-in user on the server. Returns false if the user cancelled. */
    suspend fun createPasskey(activityContext: Context): Boolean {
        if (!isPasskeySupported) throw IllegalStateException("Passkeys require Android 9 or newer")
        if (currentUser == null) throw IllegalStateException("Sign in to create a passkey")
        val requestJson = passkeyRemoteDataSource.registrationOptions()
        val responseJson = credentialManagerDataSource.createPasskey(activityContext, requestJson)
            ?: return false
        passkeyRemoteDataSource.verifyRegistration(responseJson)
        return true
    }

    suspend fun signOut() {
        authRemoteDataSource.signOut()
        credentialManagerDataSource.clearCredentialState()
    }

    suspend fun deleteAccount() {
        authRemoteDataSource.deleteAccount()
        credentialManagerDataSource.clearCredentialState()
    }

    fun getCurrentUserProfile(): User? {
        val user = authRemoteDataSource.currentUser ?: return null
        val providers = user.appMetadata?.get("providers")
            ?.let { it as? JsonArray }
            ?.map { it.jsonPrimitive.content }
            ?: listOfNotNull(user.appMetadata?.get("provider")?.jsonPrimitive?.content)

        return User(
            id = user.id,
            email = user.email.orEmpty(),
            displayName = user.userMetadata.stringOrEmpty("full_name")
                .ifBlank { user.userMetadata.stringOrEmpty("name") },
            signInMethod = currentSignInMethod().orEmpty(),
            providers = providers,
            createdAt = user.createdAt?.toString().orEmpty(),
            lastSignInAt = user.lastSignInAt?.toString().orEmpty()
        )
    }

    // If the passkey server is unreachable, still offer passwords and Google instead of failing the dialog
    private suspend fun passkeyAuthenticationOptions(): String? {
        return try {
            passkeyRemoteDataSource.authenticationOptions()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.w(TAG, "Passkey sign-in is unavailable", e)
            null
        }
    }

    private suspend fun signInWithCredential(credential: Credential, nonce: Nonce) {
        when {
            credential is PasswordCredential ->
                authRemoteDataSource.signIn(credential.id, credential.password)

            credential is PublicKeyCredential -> {
                val session = passkeyRemoteDataSource.verifyAuthentication(credential.authenticationResponseJson)
                authRemoteDataSource.importSession(
                    session.accessToken, session.refreshToken, session.expiresIn, session.tokenType
                )
            }

            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                authRemoteDataSource.signInWithGoogle(googleCredential.idToken, nonce.raw)
            }

            else -> throw IllegalStateException("Unsupported credential type: ${credential.type}")
        }
    }

    // How the current session was obtained: the "amr" claim of the access token (password, id_token, ...)
    private fun currentSignInMethod(): String? {
        val accessToken = authRemoteDataSource.currentSession?.accessToken ?: return null
        val payload = accessToken.split(".").getOrNull(1) ?: return null
        return try {
            val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
            Json.parseToJsonElement(json).jsonObject["amr"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("method")
                ?.jsonPrimitive?.content
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun JsonObject?.stringOrEmpty(key: String): String =
        this?.get(key)?.jsonPrimitive?.content.orEmpty()

    companion object {
        private const val TAG = "AuthRepository"
    }

    // Google receives the SHA-256 of the nonce, Supabase receives the raw value and compares the hash
    private class Nonce {
        val raw: String = UUID.randomUUID().toString()
        val hashed: String = MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
