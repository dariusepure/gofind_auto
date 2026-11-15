package com.dariusepure.gofindauto.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import com.dariusepure.gofindauto.ui.auth.SignInScreen
import com.dariusepure.gofindauto.ui.auth.SignUpScreen
import com.dariusepure.gofindauto.ui.auth.ForgotPasswordScreen
import com.dariusepure.gofindauto.ui.auth.ResetPasswordScreen
import com.dariusepure.gofindauto.ui.cars.AddCarScreen
import com.dariusepure.gofindauto.ui.cars.CarDetailsScreen
import com.dariusepure.gofindauto.ui.cars.CarListScreen
import com.dariusepure.gofindauto.ui.cars.DiagnosisScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dariusepure.gofindauto.ui.common.LoadingState
import com.dariusepure.gofindauto.ui.theme.SettingsViewModel

sealed class Screen(val route: String) {
    data object SignIn : Screen("signin")
    data object SignUp : Screen("signup")
    data object ForgotPassword : Screen("forgotpassword")
    data object ResetPassword : Screen("resetpassword?oobCode={oobCode}") {
        fun createRoute(oobCode: String) = "resetpassword?oobCode=$oobCode"
    }
    data object Main : Screen("main")
    data object CarDetails : Screen("cardetails/{carId}") {
        fun createRoute(carId: String) = "cardetails/$carId"
    }
    data object AddCar : Screen("addcar")
    data object Diagnosis : Screen("diagnosis/{carId}") {
        fun createRoute(carId: String) = "diagnosis/$carId"
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String? = null,
    mainViewModel: MainViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass? = null
) {
    val signedIn by mainViewModel.signedIn.collectAsStateWithLifecycle()

    if (signedIn == null && startDestination == null) {
        LoadingState()
        return
    }

    val finalStartDestination = startDestination ?: if (signedIn == true) Screen.Main.route else Screen.SignIn.route

    NavHost(
        navController = navController,
        startDestination = finalStartDestination,
        modifier = modifier.fillMaxSize(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignedIn = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignedIn = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onBackToSignIn = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.ResetPassword.route) { backStackEntry ->
            val oobCode = backStackEntry.arguments?.getString("oobCode") ?: ""
            ResetPasswordScreen(
                oobCode = oobCode,
                onSuccess = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                onCarClick = { carId ->
                    navController.navigate(Screen.CarDetails.createRoute(carId))
                },
                onLogout = {
                    mainViewModel.signOut()
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.CarDetails.route) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: return@composable
            CarDetailsScreen(
                carId = carId,
                onBack = { navController.popBackStack() },
                onDiagnosisClick = {
                    navController.navigate(Screen.Diagnosis.createRoute(carId))
                }
            )
        }
        composable(Screen.AddCar.route) {
            AddCarScreen(
                onCarSaved = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Diagnosis.route) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: return@composable
            DiagnosisScreen(
                carId = carId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
