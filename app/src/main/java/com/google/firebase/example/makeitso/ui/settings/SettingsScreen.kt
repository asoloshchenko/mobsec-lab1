package com.google.firebase.example.makeitso.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.example.makeitso.R
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.ui.shared.CenterTopAppBar
import com.google.firebase.example.makeitso.ui.shared.StandardButton
import com.google.firebase.example.makeitso.ui.theme.DarkBlue
import com.google.firebase.example.makeitso.ui.theme.DarkGrey
import com.google.firebase.example.makeitso.ui.theme.LightRed
import com.google.firebase.example.makeitso.ui.theme.MakeItSoTheme
import kotlinx.serialization.Serializable

@Serializable
object SettingsRoute

@Composable
fun SettingsScreen(
    openProfileScreen: () -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    SettingsScreenContent(
        openProfileScreen = openProfileScreen,
        signOut = { viewModel.signOut(showErrorSnackbar) },
        deleteAccount = { viewModel.deleteAccount(showErrorSnackbar) }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreenContent(
    openProfileScreen: () -> Unit,
    signOut: () -> Unit,
    deleteAccount: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = stringResource(R.string.settings),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp
                )
        ) {
            Spacer(Modifier.size(24.dp))

            StandardButton(
                label = R.string.profile,
                onButtonClick = {
                    openProfileScreen()
                }
            )

            Spacer(Modifier.size(16.dp))

            StandardButton(
                label = R.string.sign_out,
                onButtonClick = {
                    signOut()
                }
            )

            Spacer(Modifier.size(16.dp))

            DeleteAccountButton(deleteAccount)
        }
    }
}

@Composable
fun DeleteAccountButton(deleteAccount: () -> Unit) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    StandardButton(
        label = R.string.delete_account,
        onButtonClick = {
            showDeleteAccountDialog = true
        }
    )

    if (showDeleteAccountDialog) {
        AlertDialog(
            containerColor = LightRed,
            textContentColor = DarkBlue,
            titleContentColor = DarkBlue,
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(stringResource(R.string.delete_account_description)) },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.cancel), fontSize = 16.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccount()
                    },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.delete), fontSize = 16.sp)
                }
            },
            onDismissRequest = { showDeleteAccountDialog = false }
        )
    }
}

private fun getDialogButtonColors(): ButtonColors {
    return ButtonColors(
        containerColor = LightRed,
        contentColor = DarkBlue,
        disabledContainerColor = LightRed,
        disabledContentColor = DarkGrey
    )
}

@Composable
@Preview(showSystemUi = true)
fun SettingsScreenPreview() {
    MakeItSoTheme(darkTheme = true) {
        SettingsScreenContent(
            openProfileScreen = {},
            signOut = {},
            deleteAccount = {}
        )
    }
}