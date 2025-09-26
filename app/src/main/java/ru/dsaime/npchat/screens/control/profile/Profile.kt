package ru.dsaime.npchat.screens.control.profile

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.model.User
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.dialog.BottomDialogProperties
import ru.dsaime.npchat.ui.dialog.BottomDialogProperty

object ProfileReq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDialogContent(onNavigationRequest: (ProfileEffect.Navigation) -> Unit) {
    val vm = koinViewModel<ProfileViewModel>()
    val state = vm.viewState.collectAsState().value

    LaunchedEffect(1) {
        vm.effect
            .onEach { effect ->
                when (effect) {
                    is ProfileEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    val profile = state.profile
    if (profile == null) {
        return
    }

    BottomDialogHeader("Мой профиль")
    BottomDialogProperties(
        BottomDialogProperty(name = "ID", value = profile.id),
        BottomDialogProperty(name = "Name", value = profile.name),
        BottomDialogProperty(name = "Nick", value = profile.nick),
        BottomDialogProperty(name = "Login", value = "noval"),
    )
    LeftButton("Редактировать", vm.eventHandler(ProfileEvent.EditProfile))
    LeftButton("Список сессий", vm.eventHandler(ProfileEvent.Sessions))
    LeftButton("Завершить сессию", vm.eventHandler(ProfileEvent.EndSession))
}

sealed interface ProfileEvent {
    object EditProfile : ProfileEvent

    object Sessions : ProfileEvent

    object EndSession : ProfileEvent

    object Back : ProfileEvent
}

data class ProfileState(
    val profile: User? = null,
)

sealed interface ProfileEffect {
    sealed interface Navigation : ProfileEffect {
        object EditProfile : Navigation

        object Sessions : Navigation

        object EndSession : Navigation

        object Back : Navigation
    }
}

class ProfileViewModel(
    private val sessionsService: SessionsService,
) : BaseViewModel<ProfileEvent, ProfileState, ProfileEffect>() {
    override fun setInitialState() = ProfileState()

    override fun handleEvents(event: ProfileEvent) {
        when (event) {
            ProfileEvent.Back -> ProfileEffect.Navigation.Back.emit()
            ProfileEvent.EditProfile -> ProfileEffect.Navigation.EditProfile.emit()
            ProfileEvent.EndSession -> ProfileEffect.Navigation.EndSession.emit()
            ProfileEvent.Sessions -> ProfileEffect.Navigation.Sessions.emit()
        }
    }
}
