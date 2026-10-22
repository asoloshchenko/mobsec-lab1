package com.google.firebase.example.makeitso.ui.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.example.makeitso.R
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.model.User
import com.google.firebase.example.makeitso.ui.shared.CenterTopAppBar
import com.google.firebase.example.makeitso.ui.shared.LoadingIndicator
import com.google.firebase.example.makeitso.ui.shared.StandardButton
import com.google.firebase.example.makeitso.ui.theme.MakeItSoTheme
import kotlinx.serialization.Serializable

@Serializable
object ProfileRoute

@Composable
fun ProfileScreen(
    showErrorSnackbar: (ErrorMessage) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val activityContext = LocalContext.current

    LaunchedEffect(true) {
        viewModel.loadCurrentUser()
    }

    val currentUser = user
    if (currentUser == null) {
        LoadingIndicator()
    } else {
        ProfileScreenContent(
            user = currentUser,
            isPasskeySupported = viewModel.isPasskeySupported,
            createPasskey = { viewModel.createPasskey(activityContext, showErrorSnackbar) }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileScreenContent(
    user: User,
    isPasskeySupported: Boolean,
    createPasskey: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = stringResource(R.string.profile),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 4.dp
                )
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.size(16.dp))

            val label = methodLabel()

            ProfileField(R.string.sign_in_method, label(user.signInMethod))
            ProfileField(R.string.linked_providers, user.providers.joinToString { label(it) })
            ProfileField(R.string.email, user.email)
            ProfileField(R.string.display_name, user.displayName)
            ProfileField(R.string.user_id, user.id)
            ProfileField(R.string.created_at, user.createdAt)
            ProfileField(R.string.last_sign_in_at, user.lastSignInAt)

            if (isPasskeySupported) {
                Spacer(Modifier.size(24.dp))
                StandardButton(label = R.string.create_passkey, onButtonClick = createPasskey)
            }
        }
    }
}

@Composable
private fun ProfileField(@StringRes label: Int, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = stringResource(label), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(4.dp))
        Text(text = value.ifBlank { "—" }, fontSize = 16.sp)
        Spacer(Modifier.size(8.dp))
        HorizontalDivider()
    }
}

// "amr" method of the access token: password for email/password, id_token for Google ID tokens.
// The passkey Edge Function exchanges a one-time magic link token for the session, and nothing else
// in this app uses magic links, so otp/magiclink means "signed in with a passkey".
// Linked providers come from app_metadata: email, google
@Composable
private fun methodLabel(): (String) -> String {
    val emailPassword = stringResource(R.string.method_email_password)
    val google = stringResource(R.string.method_google)
    val passkey = stringResource(R.string.method_passkey)
    return { value ->
        when (value) {
            "password", "email" -> emailPassword
            "id_token", "oauth", "google" -> google
            "otp", "magiclink" -> passkey
            else -> value
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun ProfileScreenPreview() {
    MakeItSoTheme(darkTheme = true) {
        ProfileScreenContent(
            User(
                id = "3f1c0b8e-0000-0000-0000-000000000000",
                email = "user@example.com",
                displayName = "Jane Doe",
                signInMethod = "id_token",
                providers = listOf("email", "google"),
                createdAt = "2026-10-14T18:00:00Z",
                lastSignInAt = "2026-10-14T18:30:00Z"
            ),
            isPasskeySupported = true,
            createPasskey = {}
        )
    }
}
