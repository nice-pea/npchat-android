package ru.dsaime.npchat.screens.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.core.Koin
import ru.dsaime.npchat.screens.home.HomeScreenDestination
import ru.dsaime.npchat.screens.home.ROUTE_HOME
import ru.dsaime.npchat.screens.login.LoginScreenDestination
import ru.dsaime.npchat.screens.login.ROUTE_LOGIN
import ru.dsaime.npchat.screens.splash.ROUTE_SPLASH
import ru.dsaime.npchat.screens.splash.SplashScreenDestination
import ru.dsaime.npchat.ui.theme.Black

@Composable
fun ComposeApp(koin: Koin) {
    val navController = rememberNavController()
    NavHost(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Black),
        navController = navController,
        startDestination = ROUTE_SPLASH,
    ) {
        composable(ROUTE_SPLASH) { SplashScreenDestination(navController) }
        composable(ROUTE_LOGIN) { LoginScreenDestination(navController) }
        composable(ROUTE_HOME) { HomeScreenDestination(navController) }
    }
}
