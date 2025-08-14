package ru.dsaime.npchat.screens.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.core.Koin
import ru.dsaime.npchat.screens.chat.messages.MessagesScreen
import ru.dsaime.npchat.screens.chat.messages.RouteMessages
import ru.dsaime.npchat.screens.chats.ChatsScreen
import ru.dsaime.npchat.screens.chats.RouteChats
import ru.dsaime.npchat.screens.login.LoginScreen
import ru.dsaime.npchat.screens.login.RouteLogin
import ru.dsaime.npchat.screens.splash.RouteSplash
import ru.dsaime.npchat.screens.splash.SplashScreen
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
        composable(RouteSplash) { SplashScreen(navController) }
        composable(RouteLogin) { LoginScreen(navController) }
        composable(RouteChats) { ChatsScreen(navController) }
//        composable<RouteMessages> {
//            val messages = it.toRoute<RouteMessages>()
//            MessagesScreen(navController, messages.chatID)
//        }
    }
}
