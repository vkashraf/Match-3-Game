package com.example.core.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object GameEventBus {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    fun postEvent(event: GameEvent) {
        scope.launch {
            _events.emit(event)
        }
    }

    suspend fun emit(event: GameEvent) {
        _events.emit(event)
    }
}
