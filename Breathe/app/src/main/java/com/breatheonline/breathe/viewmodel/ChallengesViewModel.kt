package com.breatheonline.breathe.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.models.ChallengeDto
import com.breatheonline.breathe.data.models.ChallengeRecommendationDto
import com.breatheonline.breathe.data.models.UserChallengeDto
import com.breatheonline.breathe.data.repository.ChallengesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val repo: ChallengesRepository
): ViewModel() {

    // State для доступных челленджей
    var availableChallenges by mutableStateOf<List<ChallengeDto>>(emptyList())
    var loadingAvailable by mutableStateOf(false)

    // State для моих челленджей
    var myChallenges by mutableStateOf<List<UserChallengeDto>>(emptyList())
    var loadingMy by mutableStateOf(false)

    // State для рекомендации
    var recommendation by mutableStateOf<ChallengeRecommendationDto?>(null)
    var loadingRecommendation by mutableStateOf(false)

    // State для операций
    var joiningId by mutableStateOf<String?>(null)
    var checkingInId by mutableStateOf<String?>(null)
    var abandoningId by mutableStateOf<String?>(null)

    // Глобальный error
    var error by mutableStateOf<String?>(null)

    fun loadAvailableChallenges() {
        viewModelScope.launch {
            loadingAvailable = true
            error = null
            repo.getChallenges().onSuccess { challenges ->
                availableChallenges = challenges
            }.onFailure { e ->
                error = e.message ?: "Unknown error loading challenges"
                e.printStackTrace()
            }
            loadingAvailable = false
        }
    }

    fun loadMyChallenges() {
        viewModelScope.launch {
            loadingMy = true
            error = null
            repo.getMyChallenges().onSuccess { challenges ->
                myChallenges = challenges
            }.onFailure { e ->
                error = e.message ?: "Unknown error loading my challenges"
                e.printStackTrace()
            }
            loadingMy = false
        }
    }

    fun loadRecommendation() {
        viewModelScope.launch {
            loadingRecommendation = true
            error = null
            repo.getChallengeRecommendation().onSuccess { rec ->
                recommendation = rec
            }.onFailure { e ->
                error = e.message ?: "Unknown error loading recommendation"
                e.printStackTrace()
            }
            loadingRecommendation = false
        }
    }

    fun joinChallenge(slug: String) {
        viewModelScope.launch {
            joiningId = slug
            error = null
            repo.joinChallenge(slug).onSuccess { newChallenge ->
                myChallenges = myChallenges + newChallenge
            }.onFailure { e ->
                error = e.message ?: "Failed to join challenge"
                e.printStackTrace()
            }
            joiningId = null
        }
    }

    fun checkIn(challengeId: String) {
        viewModelScope.launch {
            checkingInId = challengeId
            error = null
            repo.checkIn(challengeId).onSuccess { updated ->
                myChallenges = myChallenges.map { 
                    if (it.id == challengeId) updated else it 
                }
            }.onFailure { e ->
                error = e.message ?: "Failed to check in"
                e.printStackTrace()
            }
            checkingInId = null
        }
    }

    fun abandonChallenge(challengeId: String) {
        viewModelScope.launch {
            abandoningId = challengeId
            error = null
            repo.abandonChallenge(challengeId).onSuccess {
                myChallenges = myChallenges.filter { it.id != challengeId }
            }.onFailure { e ->
                error = e.message ?: "Failed to abandon challenge"
                e.printStackTrace()
            }
            abandoningId = null
        }
    }

    // Загружаем все при инициализации
    init {
        loadAvailableChallenges()
        loadMyChallenges()
        loadRecommendation()
    }
}