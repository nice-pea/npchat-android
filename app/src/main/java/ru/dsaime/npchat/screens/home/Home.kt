package ru.dsaime.npchat.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.common.base.ViewEvent
import ru.dsaime.npchat.common.base.ViewSideEffect
import ru.dsaime.npchat.common.base.ViewState
import ru.dsaime.npchat.data.BasicAuthService
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.ui.theme.Copper

const val ROUTE_HOME = "Home"

@Composable
fun HomeScreenDestination(navController: NavController) {
    val vm = koinViewModel<HomeViewModel>()
    HomeScreen(
        state = vm.viewState.value,
        effectFlow = vm.effect,
        onEventSent = vm::handleEvents,
        onNavigationRequest = {},
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    effectFlow: Flow<HomeEffect>?,
    onEventSent: (HomeEvent) -> Unit,
    onNavigationRequest: (HomeEffect.Navigation) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Home", color = Copper)
    }
}

sealed interface HomeEvent : ViewEvent

data class HomeState(
    val server: String = "",
) : ViewState

sealed interface HomeEffect : ViewSideEffect {
    sealed interface Navigation : HomeEffect
}

class HomeViewModel(
    private val repo: BasicAuthService,
    private val hostService: HostService,
    private val sessionsService: SessionsService,
) : BaseViewModel<HomeEvent, HomeState, HomeEffect>() {
    override fun setInitialState() = HomeState(server = "")

    override fun handleEvents(event: HomeEvent) {
        TODO("Not yet implemented")
    }
}
