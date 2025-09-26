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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
import ru.dsaime.npchat.screens.chat.create.CreateChatDialogContent
import ru.dsaime.npchat.screens.chat.create.CreateChatEffect
import ru.dsaime.npchat.screens.control.main.ControlDialogContent
import ru.dsaime.npchat.screens.control.main.ControlEffect
import ru.dsaime.npchat.screens.control.profile.ProfileDialogContent
import ru.dsaime.npchat.screens.control.profile.ProfileEffect
import ru.dsaime.npchat.screens.control.profile.session.logout.LogoutDialogContent
import ru.dsaime.npchat.screens.control.profile.session.logout.LogoutEffect
import ru.dsaime.npchat.screens.home.HomeEffect
import ru.dsaime.npchat.screens.home.HomeScreenDestination
import ru.dsaime.npchat.screens.hosts.add.AddHostDialogContent
import ru.dsaime.npchat.screens.hosts.add.AddHostEffect
import ru.dsaime.npchat.screens.hosts.select.HostSelectDialogContent
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
            val dn = koinViewModel<Navigator>()
            val dnState by dn.viewState.collectAsState()

            NPChatTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val sheetState = rememberModalBottomSheetState(true)
                val hideBottomSheet: () -> Unit = { scope.launch { sheetState.hide() } }
                val onNavigationRequest: (Any) -> Unit = { navController.navRequestHandle(it, dn, hideBottomSheet) }
                BottomDialog(
                    isVisibleRequired = dnState.stack.isNotEmpty(),
                    state = sheetState,
                    onClosed = dn.eventHandler(NavigatorEvent.Clear),
                ) {
                    // Кнопка назад будет возвращать на предыдущий диалог
                    BackHandler(dn.canPop) {
                        dn.popUp()
                    }
                    val params = BottomDialogParams(showBackButton = dn.canPop, onBack = dn::popUp)
                    when (val key = dnState.current) {
                        is DRChat -> ChatDialog(chat = key.chat, params, leave = { dn.push(DRLeave(key.chat)) })
                        is DRLeave -> LeaveDialogContent(chat = key.chat, params, confirm = hideBottomSheet)
                        DR_CONTROL -> ControlDialogContent(params, onNavigationRequest)
                        DR_CREATE_CHAT -> CreateChatDialogContent(params, onNavigationRequest)
                        DR_HOST_SELECT -> HostSelectDialogContent(params, onNavigationRequest)
                        DR_ADD_HOST -> AddHostDialogContent(params, onNavigationRequest)
                        DR_PROFILE -> ProfileDialogContent(params, onNavigationRequest)
                        DR_LOGOUT -> LogoutDialogContent(params, onNavigationRequest)
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
                    composable(ROUTE_LOGIN) {
                        LoginScreenDestination(onNavigationRequest)
                    }
                    composable(ROUTE_REGISTRATION) { RegistrationScreenDestination(onNavigationRequest) }
                    composable(ROUTE_HOME) {
                        HomeScreenDestination(
                            onNavigationRequest = onNavigationRequest,
                        ) {
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
    dn: Navigator,
    hideBottomSheet: () -> Unit = {},
) {
    val navigate: (String) -> Unit = {
        this.navigate(it)
        hideBottomSheet()
    }
    when (req) {
        // Экраны /////////////////////

        SplashEffect.Navigation.Home,
        LoginEffect.Navigation.Home,
        RegistrationEffect.Navigation.Home,
        -> navigate(ROUTE_HOME)

        SplashEffect.Navigation.Login,
        -> navigate(ROUTE_LOGIN)

        LoginEffect.Navigation.Registration -> navigate(ROUTE_REGISTRATION)

        HomeEffect.Navigation.Chats -> navigate(ROUTE_CHATS)
        LogoutEffect.Navigation.Login -> navigate(ROUTE_LOGIN)

        // Диалоги /////////////////////

        // Закрыть bottom sheet
        CreateChatEffect.Navigation.Close,
        AddHostEffect.Navigation.Close,
        HostSelectEffect.Navigation.Close,
        -> hideBottomSheet()

        // Вернуться назад
        CreateChatEffect.Navigation.Back,
        AddHostEffect.Navigation.Back,
        -> dn.popUp()

        // Добавить диалог в стек
        is ChatsEffect.Navigation.Chat -> dn.push(DRChat(req.chat))
        is CreateChatEffect.Navigation.Chat -> dn.push(DRChat(req.chat))
        HomeEffect.Navigation.Control -> dn.push(DR_CONTROL)
        ControlEffect.Navigation.CreateChat -> dn.push(DR_CREATE_CHAT)
        LoginEffect.Navigation.HostSelect -> dn.push(DR_HOST_SELECT)
        HostSelectEffect.Navigation.AddHost -> dn.push(DR_ADD_HOST)
        ControlEffect.Navigation.Profile -> dn.push(DR_PROFILE)
        ProfileEffect.Navigation.Logout -> dn.push(DR_LOGOUT)
    }
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
    chat: Chat,
    params: BottomDialogParams,
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
    chat: Chat,
    params: BottomDialogParams,
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
    val current: DialogKey? = null,
)

typealias DialogKey = Any

class Navigator : BaseViewModel<NavigatorEvent, NavigatorState, NavigatorEffect>() {
    override fun setInitialState() = NavigatorState()

    init {
        viewModelScope.launch {
            viewState
                .onEach {
                    println("dn: stack: ${it.stack.joinToString(", ")}")
                    println("dn: current: ${it.current ?: "null"}")
                }.map { it.current }
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
            val newCurrent = newStack.lastOrNull()
            copy(stack = newStack, current = newCurrent)
        }
    }

    fun push(key: DialogKey) {
        setState { copy(stack = stack + key, current = key) }
    }

    fun popUpTo(key: DialogKey) {
        if (key == viewState.value.current) return
        if (viewState.value.stack.isEmpty()) return

        setState {
            val new = stack.toMutableList().dropWhile { it != key }
            copy(stack = new, current = new.lastOrNull())
        }
    }

    fun clear() {
        setState { copy(stack = emptyList(), current = null) }
    }

    val canPop: Boolean
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
