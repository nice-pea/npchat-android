package ru.dsaime.npchat.screens.home

import ru.dsaime.npchat.common.base.BaseViewModel

sealed interface HomeEvent

data class HomeState(
    val server: String = "",
)

sealed interface HomeEffect {
    sealed interface Navigation : HomeEffect
}

class HomeViewModel(
//    private val repo: BasicAuthService,
//    private val hostService: HostService,
//    private val sessionsService: SessionsService,
) : BaseViewModel<HomeEvent, HomeState, HomeEffect>() {
    override fun setInitialState() = HomeState(server = "")

    override fun handleEvents(event: HomeEvent) {
        TODO("Not yet implemented")
    }
}
