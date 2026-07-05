package com.breatheonline.breathe.data.api

import com.breatheonline.breathe.data.models.AuthResponse
import com.breatheonline.breathe.data.models.AchievementDto
import com.breatheonline.breathe.data.models.AchievementHighlightsResponse
import com.breatheonline.breathe.data.models.ChallengeDto
import com.breatheonline.breathe.data.models.ChallengeRecommendationDto
import com.breatheonline.breathe.data.models.CoachMessageRequest
import com.breatheonline.breathe.data.models.CoachMessageResponse
import com.breatheonline.breathe.data.models.CreatePinRequest
import com.breatheonline.breathe.data.models.CreatePostRequest
import com.breatheonline.breathe.data.models.CreateSessionRequest
import com.breatheonline.breathe.data.models.GlobePinDto
import com.breatheonline.breathe.data.models.GlobeStatsDto
import com.breatheonline.breathe.data.models.HealthConnectImportRequest
import com.breatheonline.breathe.data.models.IntegrationStatusDto
import com.breatheonline.breathe.data.models.LikeResponse
import com.breatheonline.breathe.data.models.LoginRequest
import com.breatheonline.breathe.data.models.NlpInsights
import com.breatheonline.breathe.data.models.PinLikeResponse
import com.breatheonline.breathe.data.models.PostDto
import com.breatheonline.breathe.data.models.PostsPage
import com.breatheonline.breathe.data.models.RegisterRequest
import com.breatheonline.breathe.data.models.RemoteSession
import com.breatheonline.breathe.data.models.ReportRequest
import com.breatheonline.breathe.data.models.UpdateProfileRequest
import com.breatheonline.breathe.data.models.User
import com.breatheonline.breathe.data.models.UserChallengeDto
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

    @GET("achievements")
    suspend fun getAchievements(): Response<List<AchievementDto>>

    @GET("achievements/highlights")
    suspend fun getAchievementHighlights(
        @Query("limit") limit: Int = 3,
    ): Response<AchievementHighlightsResponse>

    @GET("achievements/{slug}")
    suspend fun getAchievement(@Path("slug") slug: String): Response<AchievementDto>

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

    @POST("posts/{id}/report")
    suspend fun reportPost(@Path("id") postId: String, @Body request: ReportRequest): Response<Unit>

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

    @POST("globe/{id}/report")
    suspend fun reportPin(@Path("id") pinId: String, @Body request: ReportRequest): Response<Unit>

    // ── Moderation ────────────────────────────────────────────────────────────

    @POST("users/{id}/block")
    suspend fun blockUser(@Path("id") userId: String): Response<Unit>

    // ── Integrations ──────────────────────────────────────────────────────────

    @GET("integrations/status")
    suspend fun getIntegrationStatus(): Response<List<IntegrationStatusDto>>

    @POST("integrations/sync")
    suspend fun syncIntegrations(): Response<Unit>

    @DELETE("integrations/{provider}")
    suspend fun disconnectIntegration(@Path("provider") provider: String): Response<Unit>

    @POST("integrations/apple-health")
    suspend fun importHealthData(@Body data: HealthConnectImportRequest): Response<Unit>
}

// Google Login Request
data class GoogleLoginRequest(
    @SerializedName("idToken")
    val accessToken: String
)
