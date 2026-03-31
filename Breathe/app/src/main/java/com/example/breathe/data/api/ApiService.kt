package com.example.breathe.data.api

import com.example.breathe.data.models.AuthResponse
import com.example.breathe.data.models.ChallengeDto
import com.example.breathe.data.models.CreatePinRequest
import com.example.breathe.data.models.CreatePostRequest
import com.example.breathe.data.models.GlobePinDto
import com.example.breathe.data.models.GlobeStatsDto
import com.example.breathe.data.models.LikeResponse
import com.example.breathe.data.models.PinLikeResponse
import com.example.breathe.data.models.PostDto
import com.example.breathe.data.models.PostsPage
import com.example.breathe.data.models.ChallengeRecommendationDto
import com.example.breathe.data.models.CoachMessageRequest
import com.example.breathe.data.models.CoachMessageResponse
import com.example.breathe.data.models.CreateSessionRequest
import com.example.breathe.data.models.IntegrationStatusDto
import com.example.breathe.data.models.NlpInsights
import com.example.breathe.data.models.LoginRequest
import com.example.breathe.data.models.RegisterRequest
import com.example.breathe.data.models.RemoteSession
import com.example.breathe.data.models.UpdateProfileRequest
import com.example.breathe.data.models.User
import com.example.breathe.data.models.UserChallengeDto
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ── Auth (no token required) ──────────────────────────────────────────────

    @PATCH("auth/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest,
    ): Response<User>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): Response<AuthResponse>

    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest,
    ): Response<AuthResponse>

    // ── User (Bearer token added automatically by the OkHttp interceptor) ─────

    @GET("auth/me")
    suspend fun getProfile(): Response<User>

    // ── Sessions ──────────────────────────────────────────────────────────────

    @GET("sessions")
    suspend fun getSessions(): Response<List<RemoteSession>>

    @POST("sessions")
    suspend fun createSession(
        @Body request: CreateSessionRequest,
    ): Response<RemoteSession>

    // ── Journal / NLP ─────────────────────────────────────────────────────────

    @GET("nlp/insights")
    suspend fun getNlpInsights(): Response<NlpInsights>

    // ── Challenges ────────────────────────────────────────────────────────────

    @GET("challenges")
    suspend fun getChallenges(): Response<List<ChallengeDto>>

    @GET("challenges/my")
    suspend fun getMyChallenges(): Response<List<UserChallengeDto>>

    @GET("challenges/recommend")
    suspend fun getChallengeRecommendation(): Response<ChallengeRecommendationDto>

    @POST("challenges/{slug}/join")
    suspend fun joinChallenge(@Path("slug") slug: String): Response<UserChallengeDto>

    @POST("challenges/{id}/checkin")
    suspend fun checkIn(@Path("id") challengeId: String): Response<UserChallengeDto>

    @DELETE("challenges/{id}")
    suspend fun abandonChallenge(@Path("id") challengeId: String): Response<Unit>

    // ── AI Coach ──────────────────────────────────────────────────────────────

    @POST("coach/message")
    suspend fun sendCoachMessage(
        @Body request: CoachMessageRequest,
    ): Response<CoachMessageResponse>

    // ── Community posts ───────────────────────────────────────────────────────

    @GET("posts")
    suspend fun getPosts(
        @Query("category") category: String? = null,
        @Query("page")     page:     Int     = 1,
        @Query("limit")    limit:    Int     = 20,
    ): Response<PostsPage>

    @POST("posts/{id}/like")
    suspend fun togglePostLike(@Path("id") postId: String): Response<LikeResponse>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<PostDto>

    // ── Globe pins ────────────────────────────────────────────────────────────

    @GET("globe")
    suspend fun getGlobePins(
        @Query("technique") technique: String? = null,
        @Query("limit")     limit:     Int     = 100,
    ): Response<List<GlobePinDto>>

    @GET("globe/stats")
    suspend fun getGlobeStats(): Response<GlobeStatsDto>

    @POST("globe/{id}/like")
    suspend fun likePin(@Path("id") pinId: String): Response<PinLikeResponse>

    @POST("globe")
    suspend fun createPin(@Body request: CreatePinRequest): Response<GlobePinDto>

    // ── Integrations ──────────────────────────────────────────────────────────

    @GET("integrations")
    suspend fun getIntegrationStatus(): Response<List<IntegrationStatusDto>>

    @POST("integrations/sync")
    suspend fun syncIntegrations(): Response<Unit>
}

// Google Login Request
data class GoogleLoginRequest(
    @SerializedName("idToken")
    val accessToken: String
)