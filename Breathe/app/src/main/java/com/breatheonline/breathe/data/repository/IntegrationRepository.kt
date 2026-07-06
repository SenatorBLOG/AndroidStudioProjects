package com.breatheonline.breathe.data.repository

import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.IntegrationStatusDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_TTL_MS = 60_000L

/**
 * Singleton cache for /integrations/status.
 * Multiple ViewModels (Home, Stats, Health, Profile, Achievements) each called this
 * endpoint independently on every screen open. This repository gates network requests
 * behind a 60-second TTL so the endpoint is hit at most once per minute regardless of
 * how many ViewModels are alive.
 */
@Singleton
class IntegrationRepository @Inject constructor(
    private val apiService: ApiService,
) {
    private val _integrations = MutableStateFlow<List<IntegrationStatusDto>>(emptyList())
    val integrations: StateFlow<List<IntegrationStatusDto>> = _integrations.asStateFlow()

    private var lastFetchMs = 0L
    private val mutex = Mutex()

    suspend fun refresh(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastFetchMs) < CACHE_TTL_MS) return
        mutex.withLock {
            if (!force && (System.currentTimeMillis() - lastFetchMs) < CACHE_TTL_MS) return
            runCatching { apiService.getIntegrationStatus() }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        _integrations.value = resp.body() ?: emptyList()
                        lastFetchMs = System.currentTimeMillis()
                    }
                }
        }
    }
}
