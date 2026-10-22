package com.google.firebase.example.makeitso.ui.signin

import android.content.Context
import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    private var savedCredentialRequested = false

    fun signIn(
        email: String,
        password: String,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackbar) {
            authRepository.signIn(email, password)
            _shouldRestartApp.value = true
        }
    }

    // Offered once when the screen opens, not on every recomposition or rotation
    fun signInWithSavedCredential(
        activityContext: Context,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        if (savedCredentialRequested) return
        savedCredentialRequested = true

        launchCatching(showErrorSnackbar) {
            if (authRepository.signInWithSavedCredential(activityContext)) {
                _shouldRestartApp.value = true
            }
        }
    }

    fun signInWithGoogle(
        activityContext: Context,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackbar) {
            if (authRepository.signInWithGoogle(activityContext)) {
                _shouldRestartApp.value = true
            }
        }
    }
}
