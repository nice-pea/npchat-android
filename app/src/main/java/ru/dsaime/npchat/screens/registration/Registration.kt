package ru.dsaime.npchat.screens.registration

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

const val ROUTE_REGISTRATION = "Registration"

@Composable
fun RegistrationScreenDestination(navController: NavController) {
    val vm = koinViewModel<RegistrationViewModel>()
    RegistrationScreen(
        state = vm.viewState.value,
        effectFlow = vm.effect,
        onEventSent = vm::handleEvents,
        onNavigationRequest = {},
    )
}

@Composable
fun RegistrationScreen(
    state: RegistrationState,
    effectFlow: Flow<RegistrationEffect>?,
    onEventSent: (RegistrationEvent) -> Unit,
    onNavigationRequest: (RegistrationEffect.Navigation) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Registration", color = Copper)
    }
}

sealed interface RegistrationEvent : ViewEvent

data class RegistrationState(
    val server: String = "",
) : ViewState

sealed interface RegistrationEffect : ViewSideEffect {
    sealed interface Navigation : RegistrationEffect
}

class RegistrationViewModel(
    private val repo: BasicAuthService,
    private val hostService: HostService,
    private val sessionsService: SessionsService,
) : BaseViewModel<RegistrationEvent, RegistrationState, RegistrationEffect>() {
    override fun setInitialState() = RegistrationState(server = "")

    override fun handleEvents(event: RegistrationEvent) {
        TODO("Not yet implemented")
    }
}
