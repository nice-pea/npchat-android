package ru.dsaime.npchat.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.screens.chats.RouteChats
import ru.dsaime.npchat.screens.login.RouteLogin
import ru.dsaime.npchat.ui.components.Gap
import ru.dsaime.npchat.ui.components.Progress
import ru.dsaime.npchat.ui.modifiers.fadeIn
import ru.dsaime.npchat.ui.theme.Dp10
import ru.dsaime.npchat.ui.theme.Font
import kotlin.time.Duration.Companion.milliseconds


@Preview()
@Composable
private fun PreviewSplashScreen() {
    SplashScreen(
        effectFlow = flow { },
        onEventSent = {},
        onNavigationRequested = {}
    )
}

const val RouteSplash = "Splash"
private const val Title = "nice-pea-chat\n(NPC)"

@Composable
fun SplashScreenDestination(
    navController: NavController,
) {
    val vm = koinViewModel<SplashViewModel>()
    SplashScreen(
        effectFlow = vm.effect,
        onEventSent = vm::handleEvents,
        onNavigationRequested = {
            when (it) {
                SplashContract.Effect.Navigation.ToHome -> navController.navigate(RouteChats)
                SplashContract.Effect.Navigation.ToLogin -> navController.navigate(RouteLogin)
            }
        }
    )

}


@Composable
fun SplashScreen(
    effectFlow: Flow<SplashContract.Effect>?,
    onEventSent: (SplashContract.Event) -> Unit,
    onNavigationRequested: (SplashContract.Effect.Navigation) -> Unit
) {
    LaunchedEffect(1) {
        effectFlow?.onEach { effect ->
            when (effect) {
                is SplashContract.Effect.Navigation -> onNavigationRequested(effect)
            }
        }?.collect()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.fadeIn(300.milliseconds),
            text = Title,
            style = Font.White16W400,
            textAlign = TextAlign.Center
        )
        Gap(Dp10)
        Progress(modifier = Modifier.fadeIn(300.milliseconds))
    }

    LaunchedEffect(1) {
        onEventSent(SplashContract.Event.CheckSession)
    }
}