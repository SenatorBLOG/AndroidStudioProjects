package com.breatheonline.breathe.data.repository

import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.ChallengeDto
import com.breatheonline.breathe.data.models.ChallengeRecommendationDto
import com.breatheonline.breathe.data.models.UserChallengeDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengesRepository @Inject constructor(
    private val api: ApiService
) {
    // Получаем список всех доступных челленджей
    suspend fun getChallenges(): Result<List<ChallengeDto>> = runCatching {
        val response = api.getChallenges()
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("API error: ${response.code()} - ${response.message()}")
        }
    }

    // Получаем мои активные челленджи
    suspend fun getMyChallenges(): Result<List<UserChallengeDto>> = runCatching {
        val response = api.getMyChallenges()
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("API error: ${response.code()} - ${response.message()}")
        }
    }

    // Получаем AI рекомендацию
    suspend fun getChallengeRecommendation(): Result<ChallengeRecommendationDto> = runCatching {
        val response = api.getChallengeRecommendation()
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("API error: ${response.code()} - ${response.message()}")
        }
    }

    // Вступаем в челленж
    suspend fun joinChallenge(slug: String): Result<UserChallengeDto> = runCatching {
        val response = api.joinChallenge(slug)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("API error: ${response.code()} - ${response.message()}")
        }
    }

    // Отмечаемся сегодня в челлендже
    suspend fun checkIn(challengeId: String): Result<UserChallengeDto> = runCatching {
        val response = api.checkIn(challengeId)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("API error: ${response.code()} - ${response.message()}")
        }
    }

    // Отказываемся от челленджа
    suspend fun abandonChallenge(challengeId: String): Result<Unit> = runCatching {
        val response = api.abandonChallenge(challengeId)
        if (!response.isSuccessful) {
            throw Exception("API error: ${response.code()} - ${response.message()}")
        }
    }
}