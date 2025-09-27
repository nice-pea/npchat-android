package ru.dsaime.npchat.screens.control.profile

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.model.User
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.components.dialog.BottomDialogParams
import ru.dsaime.npchat.ui.components.dialog.BottomDialogProperties
import ru.dsaime.npchat.ui.components.dialog.BottomDialogProperty
import ru.dsaime.npchat.ui.theme.Font

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDialog(
    params: BottomDialogParams,
    vm: ProfileViewModel,
    onNavigationRequest: (ProfileEffect.Navigation) -> Unit,
) {
    val state = vm.viewState.collectAsState().value

    LaunchedEffect(1) {
        vm.effect
            .onEach { effect ->
                when (effect) {
                    is ProfileEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    BottomDialogHeader("Мой профиль", params)
    when (state) {
        is ProfileState.Error -> Text(state.msg, style = Font.Text16W400)
        ProfileState.Loading -> CircularProgressIndicator()
        is ProfileState.Ok -> {
            val props =
                listOfNotNull(
                    BottomDialogProperty(name = "ID", value = state.profile.id),
                    BottomDialogProperty(name = "Name", value = state.profile.name),
                    BottomDialogProperty(name = "Nick", value = state.profile.nick),
                    state.profile.login?.let { BottomDialogProperty(name = "Login", value = it) },
                )
            BottomDialogProperties(*props.toTypedArray())
            LeftButton("Редактировать", vm.eventHandler(ProfileEvent.EditProfile))
            LeftButton("Список сессий", vm.eventHandler(ProfileEvent.Sessions))
            LeftButton("Завершить сессию", vm.eventHandler(ProfileEvent.EndSession))
        }
    }
}

sealed interface ProfileEvent {
    object EditProfile : ProfileEvent

    object Sessions : ProfileEvent

    object EndSession : ProfileEvent

    object Back : ProfileEvent
}

sealed interface ProfileState {
    data class Ok(
        val profile: User,
    ) : ProfileState

    data class Error(
        val msg: String,
    ) : ProfileState

    object Loading : ProfileState
}

sealed interface ProfileEffect {
    sealed interface Navigation : ProfileEffect {
        object EditProfile : Navigation

        object Sessions : Navigation

        object Logout : Navigation

        object Back : Navigation
    }
}

class ProfileViewModel(
    private val sessionsService: SessionsService,
) : BaseViewModel<ProfileEvent, ProfileState, ProfileEffect>() {
    override fun setInitialState() = ProfileState.Loading

    init {
        viewModelScope.launch {
            sessionsService
                .me()
                .onSuccess {
                    setState { ProfileState.Ok(it) }
                }.onFailure { msg ->
                    setState { ProfileState.Error(msg) }
                }
        }
    }

    override fun handleEvents(event: ProfileEvent) {
        when (event) {
            ProfileEvent.Back -> ProfileEffect.Navigation.Back.emit()
            ProfileEvent.EditProfile -> ProfileEffect.Navigation.EditProfile.emit()
            ProfileEvent.EndSession -> ProfileEffect.Navigation.Logout.emit()
            ProfileEvent.Sessions -> ProfileEffect.Navigation.Sessions.emit()
        }
    }
}
