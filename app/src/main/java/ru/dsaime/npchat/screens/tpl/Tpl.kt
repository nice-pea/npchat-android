package ru.dsaime.npchat.screens.tpl

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
import ru.dsaime.npchat.data.BasicAuthService
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.ui.theme.Copper

const val ROUTE_TPL = "Tpl"

@Composable
fun TplScreenDestination(navController: NavController) {
    val vm = koinViewModel<TplViewModel>()
    TplScreen(
        state = vm.viewState.value,
        effectFlow = vm.effect,
        onEventSent = vm::setEvent,
        onNavigationRequest = {},
    )
}

@Composable
fun TplScreen(
    state: TplState,
    effectFlow: Flow<TplEffect>?,
    onEventSent: (TplEvent) -> Unit,
    onNavigationRequest: (TplEffect.Navigation) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Tpl", color = Copper)
    }
}

sealed interface TplEvent

data class TplState(
    val server: String = "",
)

sealed interface TplEffect {
    sealed interface Navigation : TplEffect
}

class TplViewModel(
    private val repo: BasicAuthService,
    private val hostService: HostService,
    private val sessionsService: SessionsService,
) : BaseViewModel<TplEvent, TplState, TplEffect>() {
    override fun setInitialState() = TplState(server = "")

    override fun handleEvents(event: TplEvent) {
        TODO("Not yet implemented")
    }
}
