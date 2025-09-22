package ru.dsaime.npchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger
import ru.dsaime.npchat.di.koin.appModule
import ru.dsaime.npchat.model.Chat
import ru.dsaime.npchat.screens.chats.ChatsScreenDestination
import ru.dsaime.npchat.screens.chats.Effect
import ru.dsaime.npchat.screens.home.HomeScreenDestination
import ru.dsaime.npchat.screens.login.LoginEffect
import ru.dsaime.npchat.screens.login.LoginScreenDestination
import ru.dsaime.npchat.screens.registration.RegistrationEffect
import ru.dsaime.npchat.screens.registration.RegistrationScreenDestination
import ru.dsaime.npchat.screens.splash.SplashEffect
import ru.dsaime.npchat.screens.splash.SplashScreenDestination
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.dialog.BottomDialog
import ru.dsaime.npchat.ui.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.dialog.BottomDialogProperties
import ru.dsaime.npchat.ui.dialog.BottomDialogProperty
import ru.dsaime.npchat.ui.theme.Black
import ru.dsaime.npchat.ui.theme.NPChatTheme

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
//            var sheetVisible by remember { mutableStateOf(false) }
            val dialogs = remember { mutableStateListOf<Any>() }
            val onBack by
                remember {
                    derivedStateOf {
                        if (dialogs.size > 1) {
                            {
                                dialogs.removeAt(dialogs.lastIndex)
                                Unit
                            }
                        } else {
                            null
                        }
//                        dialogs.lastIndex
//                            .takeIf { it > 0 }
//                            ?.let { lastIndex ->
//                                {
//                                    dialogs.removeAt(lastIndex)
//                                    Unit
//                                }
//                            }
                    }
                }
            NPChatTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                BottomDialog(
                    isVisibleRequired = dialogs.isNotEmpty(),
                    onClosed = { dialogs.clear() },
                ) { dismissRequest ->
                    // Диалоги будут очищаться после того как скроется BottomSheet
                    val closeDialog: () -> Unit = {
                        scope.launch {
                            dismissRequest()
                            dialogs.clear()
                        }
                    }
                    // Кнопка назад будет возвращать на предыдущий диалог
                    BackHandler(dialogs.size > 1) {
                        dialogs.removeLastOrNull()
                    }
                    when (val args = dialogs.lastOrNull()) {
                        is DialogChatArgs ->
                            ChatDialogContent(
                                args = args,
                                onBack = onBack,
                                leave = { dialogs.add(DialogLeaveArgs(args.chat)) },
                            )

                        is DialogLeaveArgs -> LeaveDialogContent(args = args, onBack = onBack, confirm = closeDialog)
                    }
                }
                NavHost(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Black),
                    navController = navController,
                    startDestination = ROUTE_SPLASH,
                ) {
                    composable(ROUTE_SPLASH) { SplashScreenDestination { navController.navRequestHandle(it, dialogs) } }
                    composable(ROUTE_LOGIN) {
                        LoginScreenDestination { navController.navRequestHandle(it, dialogs) }
                    }
                    composable(ROUTE_REGISTRATION) { RegistrationScreenDestination { navController.navRequestHandle(it, dialogs) } }
                    composable(ROUTE_HOME) {
                        HomeScreenDestination(
                            onNavigationRequest = { navController.navRequestHandle(it, dialogs) },
                        ) {
                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = ROUTE_CHATS,
                            ) {
                                composable(ROUTE_CHATS) { ChatsScreenDestination { navController.navRequestHandle(it, dialogs) } }
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

fun NavController.navRequestHandle(
    req: Any,
    dialogs: MutableList<Any>,
) {
    when (req) {
        SplashEffect.Navigation.Home,
        LoginEffect.Navigation.Home,
        RegistrationEffect.Navigation.Home,
        -> navigate(ROUTE_HOME)

        SplashEffect.Navigation.Login -> navigate(ROUTE_LOGIN)
        LoginEffect.Navigation.Registration -> navigate(ROUTE_REGISTRATION)

        is Effect.Navigation.Chat -> dialogs.add(DialogChatArgs(req.chat))
    }
}

class DialogChatArgs(
    val chat: Chat,
)

@Composable
fun ChatDialogContent(
    args: DialogChatArgs,
    onBack: (() -> Unit)? = null,
    leave: () -> Unit,
) {
    val chat = args.chat
    BottomDialogHeader(chat.name, onBack)
    BottomDialogProperties(
        BottomDialogProperty("ID", chat.id),
        BottomDialogProperty("Name", chat.name),
        BottomDialogProperty("ChiefID", chat.chiefId),
    )
    LeftButton("Покинуть чат", leave)
}

class DialogLeaveArgs(
    val chat: Chat,
)

@Composable
fun LeaveDialogContent(
    args: DialogLeaveArgs,
    onBack: (() -> Unit)? = null,
    confirm: () -> Unit,
) {
    BottomDialogHeader("Покинуть чат", onBack)
    BottomDialogProperties(
        BottomDialogProperty("ID", args.chat.id),
        BottomDialogProperty("Name", args.chat.name),
        BottomDialogProperty("ChiefID", args.chat.chiefId),
    )
    LeftButton("Подтвердить", confirm, isRight = true)
}
