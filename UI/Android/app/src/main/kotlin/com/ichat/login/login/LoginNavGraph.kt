package com.ichat.login.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ichat.login.IChatApplication
import com.ichat.login.home.HomePlaceholderScreen
import kotlinx.coroutines.flow.collectLatest

object Routes {
    const val Phone = "phone"
    const val Code  = "code"
    const val Home  = "home"
}

@Composable
fun LoginNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val app = context.applicationContext as IChatApplication
    val vm: LoginViewModel = viewModel(factory = LoginViewModelFactory(app.authService))
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            if (event == LoginEvent.LoginSuccess) {
                navController.navigate(Routes.Home) {
                    popUpTo(Routes.Phone) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(state.phase) {
        when (state.phase) {
            LoginPhase.Phone -> if (navController.currentDestination?.route != Routes.Phone) {
                navController.popBackStack(Routes.Phone, inclusive = false)
            }
            LoginPhase.Code  -> if (navController.currentDestination?.route != Routes.Code) {
                navController.navigate(Routes.Code)
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.Phone) {
        composable(Routes.Phone) { PhoneScreen(vm) }
        composable(Routes.Code)  { CodeScreen(vm)  }
        composable(Routes.Home)  { HomePlaceholderScreen() }
    }
}
