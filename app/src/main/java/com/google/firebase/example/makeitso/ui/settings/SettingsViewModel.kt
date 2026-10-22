package com.google.firebase.example.makeitso.ui.settings

import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// Navigation after sign out / account deletion is driven by the session status in MainActivity
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    fun signOut(showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            authRepository.signOut()
        }
    }

    fun deleteAccount(showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            authRepository.deleteAccount()
        }
    }
}
