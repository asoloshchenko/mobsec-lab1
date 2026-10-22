package com.google.firebase.example.makeitso.ui.profile

import android.content.Context
import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.R
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.model.User
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?>
        get() = _user.asStateFlow()

    val isPasskeySupported: Boolean = authRepository.isPasskeySupported

    fun loadCurrentUser() {
        launchCatching {
            _user.value = authRepository.getCurrentUserProfile()
        }
    }

    fun createPasskey(activityContext: Context, showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            if (authRepository.createPasskey(activityContext)) {
                showErrorSnackbar(ErrorMessage.IdError(R.string.passkey_created))
            }
        }
    }
}
