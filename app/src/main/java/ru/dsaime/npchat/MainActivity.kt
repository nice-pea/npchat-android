package ru.dsaime.npchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger
import ru.dsaime.npchat.common.functions.toast
import ru.dsaime.npchat.di.koin.appModule
import ru.dsaime.npchat.screens.chats.ChatsScreenDestination
import ru.dsaime.npchat.screens.home.HomeScreenDestination
import ru.dsaime.npchat.screens.login.LoginEffect
import ru.dsaime.npchat.screens.login.LoginScreenDestination
import ru.dsaime.npchat.screens.registration.RegistrationEffect
import ru.dsaime.npchat.screens.registration.RegistrationScreenDestination
import ru.dsaime.npchat.screens.splash.SplashEffect
import ru.dsaime.npchat.screens.splash.SplashScreenDestination
import ru.dsaime.npchat.ui.components.Input
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.dialog.BottomDialog
import ru.dsaime.npchat.ui.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.dialog.BottomDialogProperties
import ru.dsaime.npchat.ui.dialog.BottomDialogProperty
import ru.dsaime.npchat.ui.theme.Black
import ru.dsaime.npchat.ui.theme.ColorDeleted
import ru.dsaime.npchat.ui.theme.NPChatTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
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
            var sheetVisible by remember { mutableStateOf(false) }
            NPChatTheme {
                val navController = rememberNavController()
                BottomDialog(
                    isVisibleRequired = sheetVisible,
                    onClosed = { sheetVisible = false },
                ) {
                    val ctx = LocalContext.current
                    BackHandler {
                        toast("BackHandler", ctx)
                    }
                    BottomDialogHeader("Название чата", { toast("нажал на кнопку", ctx) })
                    BottomDialogProperties(
                        BottomDialogProperty(name = "User", value = "@test"),
                        BottomDialogProperty(name = "test", value = "test", action = { toast("нажал на кнопку", ctx) }),
                        BottomDialogProperty(name = "Chat", value = "test"),
                    )
                    var inputText by remember { mutableStateOf("") }
                    Input(
//                        modifier = Modifier.weight(1f),
                        title = "Сервер",
                        placeholder = "http://example.com",
                        value = inputText,
                        onValueChange = { inputText = it },
                    )
                    var we by remember { mutableIntStateOf(10) }
                    Button(onClick = { sheetVisible = false }) { Text("Button") }
                    Button(onClick = { we = Random.nextInt(10, 499) }) { Text("Add") }

                    Box(Modifier.fillMaxWidth().height(we.dp).background(ColorDeleted))
                    LeftButton(
                        "Переход куда-то",
                        {},
                        helperText = "Подсказка на русском языке. Её будет заметно больше, чем на английском",
                    )
                    LeftButton(
                        "Contfirm",
                        {},
                        helperText = "The number of chats thed chats cannot be deleted",
                        isRight = true,
                    )
                }
                NavHost(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Black),
                    navController = navController,
                    startDestination = ROUTE_SPLASH,
                ) {
                    composable(ROUTE_SPLASH) { SplashScreenDestination(navController::navRequestHandle) }
                    composable(ROUTE_LOGIN) {
                        LoginScreenDestination {
                            when (it) {
                                LoginEffect.Navigation.ToTest -> {
                                    sheetVisible = true
                                }

                                else -> navController.navRequestHandle(it)
                            }
                        }
                    }
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
