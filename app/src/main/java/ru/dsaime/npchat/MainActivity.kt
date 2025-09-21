package ru.dsaime.npchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import ru.dsaime.npchat.ui.theme.ColorDeleted
import ru.dsaime.npchat.ui.theme.ColorPart
import ru.dsaime.npchat.ui.theme.ColorSenderAlt
import ru.dsaime.npchat.ui.theme.NPChatTheme
import ru.dsaime.npchat.ui.theme.White

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
            val sheetVisible = remember { mutableStateOf(false) }
            NPChatTheme {
                val navController = rememberNavController()
                BottomDialogLayout(
                    sheet = {
                        Column {
                            Text("test")
                            Button(onClick = { sheetVisible.value = false }) { Text("Button") }
                        }
                    },
                    sheetColor = ColorSenderAlt,
                    visible = sheetVisible,
                    onClosed = { sheetVisible.value = false },
                ) {
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
                                        navController.navigate("dialog")
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
                        dialog("dialog") {
                            Column(
                                modifier =
                                    Modifier
                                        .height(499.dp)
                                        .background(ColorDeleted),
                            ) {
                                Text("test")
                                Text("стэк пустой или нет = ${navController.previousBackStackEntry}")
                                Button(onClick = {
                                    navController.navigateUp()
                                }) {
                                    Text("Button")
                                }
                                Button(onClick = { navController.navigate("dialog2", {}) }) { }
                                Button(onClick = { sheetVisible.value = true }) { Text("btmsheet") }
                            }
                        }
                        dialog("dialog2") {
                            Column(
                                modifier =
                                    Modifier
                                        .height(300.dp)
                                        .background(ColorPart),
                            ) {
                                Text("test2")
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
* Вызов компонента (visible = false):
* - isShown = false
* - state не существует
* - модалку не показываем
* Вызов компонента (visible = true):
* - isShown = true
* - модалку показать
* - state visible = true
* Закрыть снаружи (visible = false):
* - вызвать state.hide()
* - ждать когда пропадает
* Закрыть внутри (onClosed):
* Открыть после закрытия (visible = true):
* Открыть после закрытия (onClosed):
* */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomDialogLayout(
    visible: State<Boolean>,
    onClosed: () -> Unit,
    sheet: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
    sheetColor: Color = White,
//    skipPartiallyExpanded: Boolean = true,
    content: @Composable () -> Unit,
) {
    var isShown by remember { mutableStateOf(visible.value) }
    LaunchedEffect(visible.value) {
        isShown = visible.value
    }
    LaunchedEffect(isShown) {
        if (!isShown && visible.value) onClosed()
    }

//    // Основной контент приложения
    Box(Modifier.fillMaxSize()) {
        content()
        // ModalBottomSheet как отдельный слой
        if (isShown) {
            val scope = rememberCoroutineScope()
            val state =
                rememberPorkedAroundSheetState(onDismissRequest = {
                    isShown = false
                })
            ModalBottomSheet(
                shape = RectangleShape,
                onDismissRequest = { scope.launch { state.hide() } },
                sheetState = state,
                containerColor = Color.Transparent, // Прозрачный фон обертки
                tonalElevation = 0.dp, // Убираем тень, если нужно
                dragHandle = null, // Убираем хэндл, если не нужен
            ) {
                // Контент листа
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(sheetColor)
                            .animateContentSize(),
                ) {
                    sheet { scope.launch { state.hide() } }
                }

                // Обработка системной кнопки "Назад"
                BackHandler { scope.launch { state.hide() } }
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

@Composable
@ExperimentalMaterial3Api
fun rememberPorkedAroundSheetState(
    onDismissRequest: () -> Unit,
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
): SheetState {
    val scope = rememberCoroutineScope()
    return rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded, confirmValueChange = { value ->
        val upstreamResult = confirmValueChange(value)
        if (upstreamResult && value == SheetValue.Hidden) {
            scope.launch {
                delay(100)
                onDismissRequest()
            }
        }
        upstreamResult
    })
}
