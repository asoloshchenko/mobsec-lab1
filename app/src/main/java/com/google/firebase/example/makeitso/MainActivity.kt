package com.google.firebase.example.makeitso

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.ui.settings.SettingsRoute
import com.google.firebase.example.makeitso.ui.settings.SettingsScreen
import com.google.firebase.example.makeitso.ui.signin.SignInRoute
import com.google.firebase.example.makeitso.ui.signin.SignInScreen
import com.google.firebase.example.makeitso.ui.signup.SignUpRoute
import com.google.firebase.example.makeitso.ui.signup.SignUpScreen
import com.google.firebase.example.makeitso.ui.theme.MakeItSoTheme
import com.google.firebase.example.makeitso.ui.todoitem.TodoItemRoute
import com.google.firebase.example.makeitso.ui.todoitem.TodoItemScreen
import com.google.firebase.example.makeitso.ui.todolist.TodoListRoute
import com.google.firebase.example.makeitso.ui.todolist.TodoListScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import com.google.firebase.example.makeitso.ui.profile.ProfileRoute
import com.google.firebase.example.makeitso.ui.profile.ProfileScreen
import com.google.firebase.example.makeitso.ui.shared.LoadingIndicator
import io.github.jan.supabase.auth.status.SessionStatus
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setSoftInputMode()

        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val navController = rememberNavController()
            val sessionStatus by authRepository.sessionStatus.collectAsStateWithLifecycle()
            val showErrorSnackbar: (ErrorMessage) -> Unit = { errorMessage ->
                val message = getErrorMessage(errorMessage)
                scope.launch { snackbarHostState.showSnackbar(message) }
            }

            MakeItSoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        // The stored session is restored asynchronously on app start
                        if (sessionStatus is SessionStatus.Initializing) {
                            LoadingIndicator()
                        } else {
                            val startDestination: Any = remember {
                                if (sessionStatus is SessionStatus.NotAuthenticated) SignInRoute else TodoListRoute
                            }

                            // Sign out, account deletion or an expired session: leave the protected area
                            // and drop it from the back stack so "back" cannot return to it
                            LaunchedEffect(sessionStatus) {
                                if (sessionStatus is SessionStatus.NotAuthenticated) {
                                    val route = navController.currentDestination?.route
                                    if (route != null && PUBLIC_ROUTES.none { route.contains(it) }) {
                                        navController.navigate(SignInRoute) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    }
                                }
                            }

                            NavHost(
                                navController = navController,
                                startDestination = startDestination,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable<TodoListRoute> { TodoListScreen(
                                    openSettingsScreen = {
                                        navController.navigate(SettingsRoute) { launchSingleTop = true }
                                    },
                                    openTodoItemScreen = { itemId ->
                                        navController.navigate(TodoItemRoute(itemId)) { launchSingleTop = true }
                                    }
                                ) }
                                composable<SettingsRoute> { SettingsScreen(
                                    openProfileScreen = {
                                        navController.navigate(ProfileRoute) { launchSingleTop = true }
                                    },
                                    showErrorSnackbar = showErrorSnackbar
                                ) }
                                composable<ProfileRoute> { ProfileScreen(showErrorSnackbar = showErrorSnackbar) }
                                composable<SignInRoute> { SignInScreen(
                                    openHomeScreen = {
                                        navController.navigate(TodoListRoute) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    },
                                    openSignUpScreen = {
                                        navController.navigate(SignUpRoute) { launchSingleTop = true }
                                    },
                                    showErrorSnackbar = showErrorSnackbar
                                ) }
                                composable<SignUpRoute> { SignUpScreen(
                                    openHomeScreen = {
                                        navController.navigate(TodoListRoute) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    },
                                    showErrorSnackbar = showErrorSnackbar
                                ) }
                                composable<TodoItemRoute> { TodoItemScreen(
                                    openTodoListScreen = { navController.popBackStack() },
                                    showErrorSnackbar = showErrorSnackbar
                                ) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setSoftInputMode() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun getErrorMessage(error: ErrorMessage): String {
        return when (error) {
            is ErrorMessage.StringError -> error.message
            is ErrorMessage.IdError -> this@MainActivity.getString(error.message)
        }
    }

    companion object {
        private val PUBLIC_ROUTES = listOf(
            SignInRoute::class.qualifiedName!!,
            SignUpRoute::class.qualifiedName!!
        )
    }
}
