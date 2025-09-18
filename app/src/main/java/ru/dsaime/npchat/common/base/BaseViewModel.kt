package ru.dsaime.npchat.common.base

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// interface ViewEvent
//
// interface ViewState
//
// interface ViewSideEffect

// abstract class BaseViewModel<Event : ViewEvent, UiState : ViewState, Effect : ViewSideEffect> : ViewModel() {
abstract class BaseViewModel<ViewEvent, ViewState, ViewSideEffect> : ViewModel() {
    abstract fun setInitialState(): ViewState

    abstract fun handleEvents(event: ViewEvent)

    //    private val initialState: UiState by lazy { setInitialState() }
//    private val initialState: UiState =

    private val _viewState: MutableState<ViewState> = mutableStateOf(setInitialState())
    val viewState: State<ViewState> = _viewState

    @Suppress("ktlint:standard:backing-property-naming")
    private val _event: MutableSharedFlow<ViewEvent> = MutableSharedFlow()

    private val _effect: Channel<ViewSideEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            _event.collect {
                handleEvents(it)
            }
        }
    }

    fun setEvent(event: ViewEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    protected fun setState(reducer: ViewState.() -> ViewState) {
        val newState = viewState.value.reducer()
        _viewState.value = newState
    }

    @Deprecated("use .emit()", level = DeprecationLevel.HIDDEN)
    protected fun setEffect(builder: () -> ViewSideEffect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    protected fun ViewSideEffect.emit() {
        viewModelScope.launch { _effect.send(this@emit) }
    }
}
