package ru.dsaime.npchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger
import ru.dsaime.npchat.di.koin.appModule
import ru.dsaime.npchat.screens.chats.ChatsScreenDestination
import ru.dsaime.npchat.screens.home.HomeScreenDestination
import ru.dsaime.npchat.screens.login.LoginEffect
import ru.dsaime.npchat.screens.login.LoginScreenDestination
import ru.dsaime.npchat.screens.registration.RegistrationEffect
import ru.dsaime.npchat.screens.registration.RegistrationScreenDestination
import ru.dsaime.npchat.screens.splash.SplashEffect
import ru.dsaime.npchat.screens.splash.SplashScreenDestination
import ru.dsaime.npchat.ui.theme.Black
import ru.dsaime.npchat.ui.theme.NPChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        val koinApp =
            startKoin {
                logger(PrintLogger(Level.DEBUG)) // ← добавь это
                androidContext(this@MainActivity)
                modules(appModule)
            }

        setContent {
            NPChatTheme {
                val navController = rememberNavController()
                NavHost(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Black),
                    navController = navController,
                    startDestination = ROUTE_SPLASH,
                ) {
                    composable(ROUTE_SPLASH) { SplashScreenDestination(navController::navRequestHandle) }
                    composable(ROUTE_LOGIN) { LoginScreenDestination(navController::navRequestHandle) }
                    composable(ROUTE_REGISTRATION) { RegistrationScreenDestination(navController::navRequestHandle) }
                    composable(ROUTE_HOME) {
                        HomeScreenDestination(navController::navRequestHandle) {
                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = ROUTE_CHATS,
                            ) {
                                composable(ROUTE_CHATS) { ChatsScreenDestination(navController::navRequestHandle) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val ROUTE_SPLASH = "Splash"
private const val ROUTE_HOME = "Home"
private const val ROUTE_LOGIN = "Login"
private const val ROUTE_REGISTRATION = "Registration"
private const val ROUTE_CHATS = "Chats"

fun NavController.navRequestHandle(req: Any) {
    when (req) {
        SplashEffect.Navigation.ToHome,
        LoginEffect.Navigation.ToHome,
        RegistrationEffect.Navigation.ToHome,
        -> navigate(ROUTE_HOME)

        SplashEffect.Navigation.ToLogin -> navigate(ROUTE_LOGIN)
        LoginEffect.Navigation.ToRegistration -> navigate(ROUTE_REGISTRATION)
    }
}
