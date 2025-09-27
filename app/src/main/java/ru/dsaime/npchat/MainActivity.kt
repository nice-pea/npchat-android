package ru.dsaime.npchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sebaslogen.resaca.koin.koinViewModelScoped
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.di.koin.appModule
import ru.dsaime.npchat.model.Chat
import ru.dsaime.npchat.screens.chat.chats.ChatsEffect
import ru.dsaime.npchat.screens.chat.chats.ChatsScreenDestination
import ru.dsaime.npchat.screens.chat.create.CreateChatDialog
import ru.dsaime.npchat.screens.chat.create.CreateChatEffect
import ru.dsaime.npchat.screens.control.main.ControlDialog
import ru.dsaime.npchat.screens.control.main.ControlEffect
import ru.dsaime.npchat.screens.control.profile.ProfileDialog
import ru.dsaime.npchat.screens.control.profile.ProfileEffect
import ru.dsaime.npchat.screens.control.profile.session.logout.LogoutDialog
import ru.dsaime.npchat.screens.control.profile.session.logout.LogoutEffect
import ru.dsaime.npchat.screens.home.HomeEffect
import ru.dsaime.npchat.screens.home.HomeScreenDestination
import ru.dsaime.npchat.screens.hosts.add.AddHostDialog
import ru.dsaime.npchat.screens.hosts.add.AddHostEffect
import ru.dsaime.npchat.screens.hosts.select.HostSelectDialog
import ru.dsaime.npchat.screens.hosts.select.HostSelectEffect
import ru.dsaime.npchat.screens.login.LoginEffect
import ru.dsaime.npchat.screens.login.LoginScreenDestination
import ru.dsaime.npchat.screens.registration.RegistrationEffect
import ru.dsaime.npchat.screens.registration.RegistrationScreenDestination
import ru.dsaime.npchat.screens.splash.SplashEffect
import ru.dsaime.npchat.screens.splash.SplashScreenDestination
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.dialog.BottomDialog
import ru.dsaime.npchat.ui.components.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.components.dialog.BottomDialogParams
import ru.dsaime.npchat.ui.components.dialog.BottomDialogProperties
import ru.dsaime.npchat.ui.components.dialog.BottomDialogProperty
import ru.dsaime.npchat.ui.theme.Black
import ru.dsaime.npchat.ui.theme.NPChatTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        val koin =
            startKoin {
                logger(PrintLogger(Level.DEBUG))
                androidContext(this@MainActivity)
                modules(appModule)
            }.koin

        setContent {
            val scope = rememberCoroutineScope()
            val dn = koinViewModel<NavigatorViewModel>()
            val dnState by dn.viewState.collectAsState()

            NPChatTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val sheetState = rememberModalBottomSheetState(true)
                val hideBottomSheet: () -> Unit = {
                    scope.launch {
                        sheetState.hide()
                        dn.clear()
                    }
                }
                val onNavigationRequest: (Any) -> Unit = { navController.navRequestHandle(it, dn, hideBottomSheet) }
                // Закрывать нижний диалог при навигации
                LaunchedEffect(1) {
                    navController.visibleEntries
                        .collect {
                            hideBottomSheet()
                        }
                }
                BottomDialog(
                    isVisibleRequired = dnState.stack.isNotEmpty(),
                    state = sheetState,
                    onClosed = dn.eventHandler(NavigatorEvent.Clear),
                ) {
                    // Кнопка назад будет возвращать на предыдущий диалог
                    BackHandler(dn.canPopUp) { dn.popUp() }
                    // Параметры диалога, такие как признак возможности вернуться назад
                    val params = BottomDialogParams(canPopUp = dn.canPopUp, onPopUp = dn::popUp)
                    // Отображать компонент диалога в зависимости от последнего элемента стека
                    when (val key = dnState.current) {
                        is DRChat -> ChatDialog(params, chat = key.chat, leave = { dn.push(DRLeave(key.chat)) })
                        is DRLeave -> LeaveDialogContent(params, chat = key.chat, confirm = hideBottomSheet)
                        DR_CONTROL -> ControlDialog(params, onNavigationRequest)
                        DR_CREATE_CHAT -> CreateChatDialog(params, koinViewModelScoped(), onNavigationRequest)
                        DR_HOST_SELECT -> HostSelectDialog(params, koinViewModelScoped(), onNavigationRequest)
                        DR_ADD_HOST -> AddHostDialog(params, koinViewModelScoped(), onNavigationRequest)
                        DR_PROFILE -> ProfileDialog(params, koinViewModelScoped(), onNavigationRequest)
                        DR_LOGOUT -> LogoutDialog(params, koinViewModelScoped(), onNavigationRequest)
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
                    composable(ROUTE_SPLASH) { SplashScreenDestination(onNavigationRequest) }
                    composable(ROUTE_LOGIN) { LoginScreenDestination(onNavigationRequest) }
                    composable(ROUTE_REGISTRATION) { RegistrationScreenDestination(onNavigationRequest) }
                    composable(ROUTE_HOME) {
                        HomeScreenDestination(onNavigationRequest = onNavigationRequest) {
                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = ROUTE_CHATS,
                            ) {
                                composable(ROUTE_CHATS) { ChatsScreenDestination(onNavigationRequest) }
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
    dn: NavigatorViewModel,
    hideBottomSheet: () -> Unit = {},
) {
    when (req) {
        // SplashEffect
        SplashEffect.Navigation.Home -> navigate(ROUTE_HOME, oneWay(ROUTE_SPLASH))
        SplashEffect.Navigation.Login -> navigate(ROUTE_LOGIN, oneWay(ROUTE_SPLASH))

        // LoginEffect
        LoginEffect.Navigation.Home -> navigate(ROUTE_HOME, oneWay(ROUTE_LOGIN))
        LoginEffect.Navigation.Registration -> navigate(ROUTE_REGISTRATION)
        LoginEffect.Navigation.HostSelect -> dn.push(DR_HOST_SELECT)

        // RegistrationEffect
        RegistrationEffect.Navigation.Home -> navigate(ROUTE_HOME, oneWay(ROUTE_REGISTRATION))

        // HomeEffect
        HomeEffect.Navigation.Chats -> navigate("$ROUTE_HOME/$ROUTE_CHATS")
        HomeEffect.Navigation.Control -> dn.push(DR_CONTROL)

        // LogoutEffect
        LogoutEffect.Navigation.Login -> navigate(ROUTE_LOGIN, oneWay(ROUTE_HOME))

        // ChatsEffect
        is ChatsEffect.Navigation.Chat -> dn.push(DRChat(req.chat))

        // CreateChatEffect
        CreateChatEffect.Navigation.Close -> hideBottomSheet()
        CreateChatEffect.Navigation.Back -> dn.popUp()
        is CreateChatEffect.Navigation.Chat -> dn.push(DRChat(req.chat))

        // AddHostEffect
        AddHostEffect.Navigation.Close -> hideBottomSheet()
        AddHostEffect.Navigation.Back -> dn.popUp()

        // HostSelectEffect
        HostSelectEffect.Navigation.Close -> hideBottomSheet()
        HostSelectEffect.Navigation.AddHost -> dn.push(DR_ADD_HOST)

        // ControlEffect
        ControlEffect.Navigation.CreateChat -> dn.push(DR_CREATE_CHAT)
        ControlEffect.Navigation.Profile -> dn.push(DR_PROFILE)

        // ProfileEffect
        ProfileEffect.Navigation.Logout -> dn.push(DR_LOGOUT)
    }
}

private fun NavController.oneWay(key: String): NavOptionsBuilder.() -> Unit =
    {
        popUpTo(key) {
            inclusive = true
        }
        launchSingleTop = true
    }

data class DRChat(
    val chat: Chat,
)

data class DRLeave(
    val chat: Chat,
)

private const val DR_CONTROL = "DR_Control"
private const val DR_CREATE_CHAT = "DR_CreateChat"
private const val DR_HOST_SELECT = "DR_HostSelect"
private const val DR_ADD_HOST = "DR_AddHost"
private const val DR_PROFILE = "DR_Profile"
private const val DR_LOGOUT = "DR_Logout"

@Composable
fun ChatDialog(
    params: BottomDialogParams,
    chat: Chat,
    leave: () -> Unit,
) {
    BottomDialogHeader(chat.name, params)
    BottomDialogProperties(
        BottomDialogProperty("ID", chat.id),
        BottomDialogProperty("Name", chat.name),
        BottomDialogProperty("ChiefID", chat.chiefId),
    )
    LeftButton("Покинуть чат", leave)
}

@Composable
fun LeaveDialogContent(
    params: BottomDialogParams,
    chat: Chat,
    confirm: () -> Unit,
) {
    BottomDialogHeader("Покинуть чат", params)
    BottomDialogProperties(
        BottomDialogProperty("ID", chat.id),
        BottomDialogProperty("Name", chat.name),
        BottomDialogProperty("ChiefID", chat.chiefId),
    )
    LeftButton("Подтвердить", confirm, isRight = true)
}

sealed interface NavigatorEvent {
    object PopUp : NavigatorEvent

    data class PopUpTo(
        val key: DialogKey,
    ) : NavigatorEvent

    data class Push(
        val key: DialogKey,
    ) : NavigatorEvent

    object Clear : NavigatorEvent
}

sealed interface NavigatorEffect {
    object Empty : NavigatorEffect

    object NotEmpty : NavigatorEffect
}

data class NavigatorState(
    val stack: List<DialogKey> = emptyList(),
) {
    val current: DialogKey?
        get() = stack.lastOrNull()
}

typealias DialogKey = Any

class NavigatorViewModel : BaseViewModel<NavigatorEvent, NavigatorState, NavigatorEffect>() {
    override fun setInitialState() = NavigatorState()

    init {
        viewModelScope.launch {
            viewState
                .map { it.stack.lastOrNull() }
                .collect { current ->
                    if (current != null) {
                        NavigatorEffect.NotEmpty.emit()
                    } else {
                        NavigatorEffect.Empty.emit()
                    }
                }
        }
    }

    fun popUp() {
        val currentStack = viewState.value.stack
        if (currentStack.isEmpty()) return

        setState {
            val newStack = currentStack.dropLast(1)
            copy(stack = newStack)
        }
    }

    fun push(key: DialogKey) {
        setState { copy(stack = stack + key) }
    }

    fun popUpTo(key: DialogKey) {
        if (viewState.value.stack.isEmpty()) return

        setState {
            val new = stack.toMutableList().dropWhile { it != key }
            copy(stack = new)
        }
    }

    fun clear() {
        setState { copy(stack = emptyList()) }
    }

    val canPopUp: Boolean
        get() = viewState.value.stack.size > 1

    override fun handleEvents(event: NavigatorEvent) {
        when (event) {
            NavigatorEvent.PopUp -> popUp()
            NavigatorEvent.Clear -> clear()
            is NavigatorEvent.PopUpTo -> popUpTo(event.key)
            is NavigatorEvent.Push -> push(event.key)
        }
    }
}
