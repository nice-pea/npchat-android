package ru.dsaime.npchat

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
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
import ru.dsaime.npchat.screens.chat.chats.ChatsScreenDestination
import ru.dsaime.npchat.screens.chat.create.CreateChatDialogContent
import ru.dsaime.npchat.screens.chat.create.CreateChatEffect
import ru.dsaime.npchat.screens.chat.create.CreateChatViewModel
import ru.dsaime.npchat.screens.control.main.ControlDialogContent
import ru.dsaime.npchat.screens.control.main.ControlEffect
import ru.dsaime.npchat.screens.control.main.ControlViewModel
import ru.dsaime.npchat.screens.control.profile.ProfileDialogContent
import ru.dsaime.npchat.screens.control.profile.ProfileEffect
import ru.dsaime.npchat.screens.control.profile.ProfileViewModel
import ru.dsaime.npchat.screens.control.profile.session.logout.LogoutDialogContent
import ru.dsaime.npchat.screens.control.profile.session.logout.LogoutEffect
import ru.dsaime.npchat.screens.control.profile.session.logout.LogoutViewModel
import ru.dsaime.npchat.screens.home.HomeEffect
import ru.dsaime.npchat.screens.home.HomeScreenDestination
import ru.dsaime.npchat.screens.hosts.add.AddHostDialogContent
import ru.dsaime.npchat.screens.hosts.add.AddHostEffect
import ru.dsaime.npchat.screens.hosts.add.AddHostViewModel
import ru.dsaime.npchat.screens.hosts.select.HostSelectDialogContent
import ru.dsaime.npchat.screens.hosts.select.HostSelectEffect
import ru.dsaime.npchat.screens.hosts.select.HostSelectViewModel
import ru.dsaime.npchat.screens.login.LoginEffect
import ru.dsaime.npchat.screens.login.LoginScreenDestination
import ru.dsaime.npchat.screens.registration.RegistrationEffect
import ru.dsaime.npchat.screens.registration.RegistrationScreenDestination
import ru.dsaime.npchat.screens.splash.SplashEffect
import ru.dsaime.npchat.screens.splash.SplashScreenDestination
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.dialog.BottomDialog
import ru.dsaime.npchat.ui.components.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.components.dialog.BottomDialogProperties
import ru.dsaime.npchat.ui.components.dialog.BottomDialogProperty
import ru.dsaime.npchat.ui.theme.Black
import ru.dsaime.npchat.ui.theme.NPChatTheme
import kotlin.reflect.KClass

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
            val dn = koinViewModel<Navigator>()
            val dnState by dn.viewState.collectAsState()

            NPChatTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                BottomDialog(
                    isVisibleRequired = dnState.stack.isNotEmpty(),
                    onClosed = dn.eventHandler(NavigatorEvent.Clear),
                ) { dismissRequest ->
                    // Кнопка назад будет возвращать на предыдущий диалог
                    BackHandler(dnState.stack.size > 1) {
                        dn.setEvent(NavigatorEvent.Back)
                    }
                    val ctx = Compo.current
                    ctx.run {
                        koinViewModel<ControlViewModel>
                    }
                    val showBackButton = dnState.stack.isNotEmpty()
                    when (val vm = dnState.current) {
//                        is DialogChatArgs ->
//                            ChatDialogContent(
//                                args = args,
//                                onBack = onBack,
//                                leave = { dialogs.add(DialogLeaveArgs(args.chat)) },
//                            )

//                        is DialogLeaveArgs -> LeaveDialogContent(args = args, onBack = onBack, confirm = closeDialog)
                        is ControlViewModel -> ControlDialogContent(vm) { navController.navRequestHandle(it, dn, koin) }
                        is CreateChatViewModel ->
                            CreateChatDialogContent(vm, showBackButton) { navController.navRequestHandle(it, dn, koin) }

                        is HostSelectViewModel -> HostSelectDialogContent(vm) { navController.navRequestHandle(it, dn, koin) }
                        is AddHostViewModel -> AddHostDialogContent(vm) { navController.navRequestHandle(it, dn, koin) }
                        is ProfileViewModel -> ProfileDialogContent(vm) { navController.navRequestHandle(it, dn, koin) }
                        is LogoutViewModel -> LogoutDialogContent(vm) { navController.navRequestHandle(it, dn, koin) }
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
                    composable(ROUTE_SPLASH) { SplashScreenDestination { navController.navRequestHandle(it, dn, koin) } }
                    composable(ROUTE_LOGIN) {
                        LoginScreenDestination { navController.navRequestHandle(it, dn, koin) }
                    }
                    composable(ROUTE_REGISTRATION) { RegistrationScreenDestination { navController.navRequestHandle(it, dn, koin) } }
                    composable(ROUTE_HOME) {
                        HomeScreenDestination(
                            onNavigationRequest = { navController.navRequestHandle(it, dn, koin) },
                        ) {
                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = ROUTE_CHATS,
                            ) {
                                composable(ROUTE_CHATS) { ChatsScreenDestination { navController.navRequestHandle(it, dn, koin) } }
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

// @Composable
fun NavController.navRequestHandle(
    req: Any,
    dialogNavigator: Navigator,
    ctx: Context,
) {
    val navigate: (String) -> Unit = {
        this.navigate(it)
        dialogNavigator.setEvent(NavigatorEvent.Clear)
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
        -> dialogNavigator.setEvent(NavigatorEvent.Clear)

        // Вернуться назад
//        CreateChatEffect.Navigation.Back -> dialogNavigator.setEvent(NavigatorEvent.RemoveLast(Crea)) dialogs.removeIf { it is CreateChatReq }
        AddHostEffect.Navigation.Back -> dialogNavigator.setEvent(NavigatorEvent.RemoveLast(AddHostViewModel::class))

        // Добавить диалог в стек
//        is ChatsEffect.Navigation.Chat -> dialogs.add(DialogChatArgs(req.chat))
//        is CreateChatEffect.Navigation.Chat -> dialogs.add(DialogChatArgs(req.chat))
        HomeEffect.Navigation.Control -> dialogNavigator.setEvent(NavigatorEvent.PopUpTo(ctx.{ koinViewModel<ControlViewModel>() }))
        ControlEffect.Navigation.CreateChat ->
            dialogNavigator.setEvent(
                NavigatorEvent.PopUpTo(ctx.run { koinViewModel<CreateChatViewModel>() }),
            )

        LoginEffect.Navigation.HostSelect ->
            dialogNavigator.setEvent(
                NavigatorEvent.PopUpTo(ctx.run { koinViewModel<HostSelectViewModel>() }),
            )

        HostSelectEffect.Navigation.AddHost ->
            dialogNavigator.setEvent(
                NavigatorEvent.PopUpTo(ctx.run { koinViewModel<AddHostViewModel>() }),
            )

        ControlEffect.Navigation.Profile -> dialogNavigator.setEvent(NavigatorEvent.PopUpTo(ctx.run { koinViewModel<ProfileViewModel>() }))
        ProfileEffect.Navigation.Logout -> dialogNavigator.setEvent(NavigatorEvent.PopUpTo(ctx.run { koinViewModel<LogoutViewModel>() }))
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

sealed interface NavigatorEvent {
    object Back : NavigatorEvent

    data class RemoveLast(
        val kClass: KClass<*>,
    ) : NavigatorEvent

    data class PopUpTo(
        val dst: Any,
    ) : NavigatorEvent

    object Clear : NavigatorEvent
}

sealed interface NavigatorEffect {
    object Empty : NavigatorEffect

    object NotEmpty : NavigatorEffect
}

data class NavigatorState(
    val stack: List<Any> = emptyList(),
    val current: Any? = null,
)

class Navigator : BaseViewModel<NavigatorEvent, NavigatorState, NavigatorEffect>() {
    override fun setInitialState() = NavigatorState()

    init {
        viewModelScope.launch {
            viewState
                .onEach {
                    println("dn: stack: ${it.stack.joinToString(", ") { it::class.simpleName.orEmpty() }}")
                    println("dn: current: ${it.current?.let { it::class.simpleName.orEmpty() } ?: "null"}")
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

    private fun <T : Any> removeLast(kClass: KClass<T>) {
        if (viewState.value.stack.isEmpty()) {
            return
        }

        val foundIndex =
            viewState.value.stack
                .indexOfLast { it::class == kClass }
                .takeIf { it != -1 }
                ?: return

        setState {
            val newStack = stack.subList(0, foundIndex) + stack.subList(foundIndex, stack.size)
            val newCurrent = newStack.lastOrNull()
            copy(stack = newStack, current = newCurrent)
        }
    }

    private fun removeLast() {
        if (viewState.value.stack.isEmpty()) {
            return
        }
        setState {
            val newStack = stack.subList(0, stack.lastIndex)
            val newCurrent = newStack.lastOrNull()
            copy(stack = newStack, current = newCurrent)
        }
    }

    private fun popUpTo(dst: Any) {
        val stack = viewState.value.stack.toMutableList()
        if (stack.isNotEmpty()) {
            val foundIndex = stack.indexOfLast { it::class == dst::class }
            if (foundIndex == stack.lastIndex) {
                return
            }
            if (foundIndex != -1) {
                repeat(stack.size - foundIndex + 1) { stack.removeAt(stack.lastIndex) }
                return
            }
        }
        stack.add(dst)
        setState { copy(stack = stack, current = stack.lastOrNull()) }
    }

    private fun clear() {
        if (viewState.value.stack.isEmpty()) {
            return
        }
        NavigatorEffect.Empty.emit()
        setState { copy(current = null, stack = emptyList()) }
    }

    override fun handleEvents(event: NavigatorEvent) {
        when (event) {
            NavigatorEvent.Back -> removeLast()
            NavigatorEvent.Clear -> clear()
            is NavigatorEvent.PopUpTo -> popUpTo(event.dst)
            is NavigatorEvent.RemoveLast -> removeLast(event.kClass)
        }
    }
}
