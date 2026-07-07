package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.UserRole
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: BeysaViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        modifier = Modifier.padding(innerPadding),
                        label = "screen_routing"
                    ) { screen ->
                        when (screen) {
                            is Screen.Login -> {
                                LoginScreen(
                                    viewModel = viewModel,
                                    onNavigateToRegister = { viewModel.logout(); viewModel.registerNewCustomer("", "", "") }
                                )
                            }
                            is Screen.Register -> {
                                RegisterScreen(
                                    viewModel = viewModel,
                                    onNavigateToLogin = { viewModel.logout() }
                                )
                            }
                            is Screen.MainHub -> {
                                when (currentUser?.role) {
                                    UserRole.CUSTOMER -> CustomerDashboard(viewModel = viewModel)
                                    UserRole.DRIVER -> DriverDashboard(viewModel = viewModel)
                                    UserRole.MANAGER -> ManagerDashboard(viewModel = viewModel)
                                    UserRole.SUPER_ADMIN -> SuperAdminDashboard(viewModel = viewModel)
                                    null -> LoginScreen(
                                        viewModel = viewModel,
                                        onNavigateToRegister = { viewModel.logout(); viewModel.registerNewCustomer("", "", "") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
