package ru.dsaime.npchat.screens.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.core.Koin
import ru.dsaime.npchat.screens.login.LoginScreen
import ru.dsaime.npchat.screens.login.RouteLogin
import ru.dsaime.npchat.screens.splash.RouteSplash
import ru.dsaime.npchat.screens.splash.SplashScreenDestination
import ru.dsaime.npchat.ui.theme.Black


@Composable
fun ComposeApp(koin: Koin) {
    val navController = rememberNavController()
    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(Black),
        navController = navController,
        startDestination = RouteSplash
    ) {
        composable(RouteSplash) { SplashScreenDestination(navController) }
        composable(RouteLogin) { LoginScreen(navController) }
//        composable(RouteChats) { ChatsScreen(navController) }
//        composable<RouteMessages> {
//            val messages = it.toRoute<RouteMessages>()
//            MessagesScreen(navController, messages.chatID)
//        }
    }
}
