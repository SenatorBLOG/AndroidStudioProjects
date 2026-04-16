package com.breatheonline.breathe.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class AuthEvent {
    data object Unauthorized : AuthEvent()
}

/**
 * Process-wide event bus for authentication events.
 *
 * Why a plain object instead of a Hilt singleton:
 * OkHttp interceptors run on a thread pool — outside any coroutine/DI context.
 * A plain Kotlin object is reachable without injection and [tryEmit] is
 * non-suspending, so it works safely from the interceptor thread.
 *
 * Collectors (e.g. MainActivity) observe [events] inside a LaunchedEffect.
 */
object AuthEventBus {
    // extraBufferCapacity = 1 so tryEmit never drops the event when no one is
    // collecting yet (e.g. activity is briefly paused during navigation)
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    /** Non-suspending — safe to call from OkHttp interceptor threads. */
    fun emit(event: AuthEvent) {
        _events.tryEmit(event)
    }
}
