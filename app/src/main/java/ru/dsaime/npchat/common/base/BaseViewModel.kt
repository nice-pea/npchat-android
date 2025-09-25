package ru.dsaime.npchat.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// interface ViewAction
// interface ViewState
// interface ViewSideEffect

abstract class BaseViewModel<A, S, E> : ViewModel() {
    protected abstract fun setInitialState(): S

    protected abstract fun handleEvents(event: A)

    private val _viewState = MutableStateFlow(setInitialState())
    val viewState = _viewState.asStateFlow()

    @Suppress("ktlint:standard:backing-property-naming")
    private val _event: MutableSharedFlow<A> = MutableSharedFlow()

    private val _effect: Channel<E> = Channel()
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

    protected fun setState(reducer: S.() -> S) {
        val newState = viewState.value.reducer()
        _viewState.value = newState
    }

    @Deprecated("use .emit()", level = DeprecationLevel.HIDDEN)
    protected fun setEffect(builder: () -> E) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    protected fun E.emit() {
        viewModelScope.launch { _effect.send(this@emit) }
    }

    // Отправить событие из UI во ViewModel
    fun setEvent(event: A) {
        viewModelScope.launch { _event.emit(event) }
    }

    // Сокращение, чтобы вместо { vm.handleEvents(CreateChatEvent.Back) }
    // Писать vm.eventHandler(CreateChatEvent.Back)
    fun eventHandler(event: A): () -> Unit = { setEvent(event) }

    // Сокращение, чтобы вместо { vm.handleEvents(CreateChatEvent.SetName(it)) }
    // Писать vm.eventHandler(CreateChatEvent::SetName)
    fun <T> eventHandler(eventConstructor: (T) -> A): (T) -> Unit = { setEvent(eventConstructor(it)) }
}

// Сокращение для лямбды
fun <A> ((A) -> Unit).eventHandler(event: A): () -> Unit = { invoke(event) }

// Сокращение для лямбды
fun <A, T> ((A) -> Unit).eventHandler(eventConstructor: (T) -> A): (T) -> Unit = { invoke(eventConstructor(it)) }
