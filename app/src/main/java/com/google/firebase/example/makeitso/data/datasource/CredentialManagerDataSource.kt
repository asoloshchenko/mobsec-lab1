package com.google.firebase.example.makeitso.data.datasource

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.example.makeitso.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// Credential Manager shows system UI, so every call needs an Activity context
class CredentialManagerDataSource @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    // One system dialog with saved passwords, passkeys and Google accounts on the device
    suspend fun getSavedCredential(
        activityContext: Context,
        hashedNonce: String,
        passkeyRequestJson: String?
    ): Credential? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetPasswordOption())
            .addCredentialOption(googleIdOption)
            .apply { passkeyRequestJson?.let { addCredentialOption(GetPublicKeyCredentialOption(it)) } }
            .build()

        return getCredential(activityContext, request)
    }

    // Explicit "Sign in with Google" button: always shows the Google account picker
    suspend fun getGoogleCredential(activityContext: Context, hashedNonce: String): Credential? {
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return getCredential(activityContext, request)
    }

    suspend fun savePassword(activityContext: Context, email: String, password: String) {
        try {
            CredentialManager.create(activityContext)
                .createCredential(activityContext, CreatePasswordRequest(email, password))
        } catch (e: CreateCredentialCancellationException) {
            Log.d(TAG, "User declined to save the password")
        } catch (e: CreateCredentialException) {
            Log.w(TAG, "Could not save the password", e)
        }
    }

    /** Returns registrationResponseJson, or null if the user cancelled. */
    suspend fun createPasskey(activityContext: Context, requestJson: String): String? {
        return try {
            val response = CredentialManager.create(activityContext)
                .createCredential(activityContext, CreatePublicKeyCredentialRequest(requestJson))
            (response as CreatePublicKeyCredentialResponse).registrationResponseJson
        } catch (e: CreateCredentialCancellationException) {
            null
        }
    }

    // Tells credential providers that the user signed out, so Google stops auto-selecting the account
    suspend fun clearCredentialState() {
        CredentialManager.create(appContext).clearCredentialState(ClearCredentialStateRequest())
    }

    private suspend fun getCredential(activityContext: Context, request: GetCredentialRequest): Credential? {
        return try {
            CredentialManager.create(activityContext).getCredential(activityContext, request).credential
        } catch (e: NoCredentialException) {
            null
        } catch (e: GetCredentialCancellationException) {
            null
        }
    }

    companion object {
        private const val TAG = "CredentialManager"
    }
}
