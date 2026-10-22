package com.google.firebase.example.makeitso.data.datasource

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) {
    val currentUser: UserInfo? get() = auth.currentUserOrNull()

    val currentSession: UserSession? get() = auth.currentSessionOrNull()

    val sessionStatus: StateFlow<SessionStatus> get() = auth.sessionStatus

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        // Without ENABLE_EMAIL_AUTOCONFIRM the server returns no session until the email is confirmed
        if (auth.currentSessionOrNull() == null) {
            throw IllegalStateException("Confirm your email address and then sign in")
        }
    }

    suspend fun signInWithGoogle(idToken: String, rawNonce: String) {
        auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
            this.nonce = rawNonce
        }
    }

    // Session issued by the passkey Edge Function after it verified the WebAuthn assertion
    suspend fun importSession(accessToken: String, refreshToken: String, expiresIn: Long, tokenType: String) {
        auth.importSession(
            UserSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = expiresIn,
                tokenType = tokenType
            )
        )
        auth.retrieveUserForCurrentSession(updateSession = true)
    }

    // Revokes the refresh token on the server and clears the locally stored session
    suspend fun signOut() {
        try {
            auth.signOut()
        } finally {
            auth.clearSession()
        }
    }

    suspend fun deleteAccount() {
        postgrest.rpc(DELETE_CURRENT_USER_FUNCTION)
        auth.clearSession()
    }

    companion object {
        private const val DELETE_CURRENT_USER_FUNCTION = "delete_current_user"
    }
}
