package com.breatheonline.breathe

import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.api.GoogleLoginRequest
import com.breatheonline.breathe.data.models.AchievementDto
import com.breatheonline.breathe.data.models.AchievementHighlightsResponse
import com.breatheonline.breathe.data.models.AuthResponse
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
import com.breatheonline.breathe.data.repository.IntegrationRepository
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class IntegrationRepositoryTest {

    private lateinit var fakeApi: FakeApiService
    private lateinit var repo: IntegrationRepository

    @Before
    fun setUp() {
        fakeApi = FakeApiService()
        repo = IntegrationRepository(fakeApi)
    }

    @Test
    fun `integrations flow starts empty`() {
        assertEquals(emptyList<IntegrationStatusDto>(), repo.integrations.value)
    }

    @Test
    fun `first refresh calls api and emits data`() = runTest {
        val data = listOf(IntegrationStatusDto("garmin", true, null, null))
        fakeApi.nextIntegrationResponse = Response.success(data)

        repo.refresh()

        assertEquals(1, fakeApi.integrationCallCount)
        assertEquals(data, repo.integrations.value)
    }

    @Test
    fun `second refresh within ttl skips network`() = runTest {
        val data = listOf(IntegrationStatusDto("garmin", true, null, null))
        fakeApi.nextIntegrationResponse = Response.success(data)

        repo.refresh()
        repo.refresh()

        assertEquals(1, fakeApi.integrationCallCount)
    }

    @Test
    fun `force refresh always calls network even within ttl`() = runTest {
        val data = listOf(IntegrationStatusDto("garmin", true, null, null))
        fakeApi.nextIntegrationResponse = Response.success(data)
        repo.refresh()
        fakeApi.nextIntegrationResponse = Response.success(data)

        repo.refresh(force = true)

        assertEquals(2, fakeApi.integrationCallCount)
    }

    @Test
    fun `error response does not update integrations flow`() = runTest {
        @Suppress("DEPRECATION")
        fakeApi.nextIntegrationResponse = Response.error(500, ResponseBody.create(null, ""))

        repo.refresh()

        assertEquals(emptyList<IntegrationStatusDto>(), repo.integrations.value)
    }

    @Test
    fun `error response does not update lastFetchMs so next call retries`() = runTest {
        @Suppress("DEPRECATION")
        fakeApi.nextIntegrationResponse = Response.error(500, ResponseBody.create(null, ""))
        repo.refresh()
        val afterError = fakeApi.integrationCallCount

        val data = listOf(IntegrationStatusDto("strava", false, null, null))
        fakeApi.nextIntegrationResponse = Response.success(data)
        repo.refresh()

        assertEquals(afterError + 1, fakeApi.integrationCallCount)
        assertEquals(data, repo.integrations.value)
    }
}

// ── Fake ──────────────────────────────────────────────────────────────────────

private class FakeApiService : ApiService {
    var integrationCallCount = 0
    var nextIntegrationResponse: Response<List<IntegrationStatusDto>> = Response.success(emptyList())

    override suspend fun getIntegrationStatus(): Response<List<IntegrationStatusDto>> {
        integrationCallCount++
        return nextIntegrationResponse
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Response<User> = error("not used")
    override suspend fun login(request: LoginRequest): Response<AuthResponse> = error("not used")
    override suspend fun register(request: RegisterRequest): Response<AuthResponse> = error("not used")
    override suspend fun loginWithGoogle(request: GoogleLoginRequest): Response<AuthResponse> = error("not used")
    override suspend fun getProfile(): Response<User> = error("not used")
    override suspend fun getSessions(): Response<List<RemoteSession>> = error("not used")
    override suspend fun createSession(request: CreateSessionRequest): Response<RemoteSession> = error("not used")
    override suspend fun getAchievements(): Response<List<AchievementDto>> = error("not used")
    override suspend fun getAchievementHighlights(limit: Int): Response<AchievementHighlightsResponse> = error("not used")
    override suspend fun getAchievement(slug: String): Response<AchievementDto> = error("not used")
    override suspend fun getNlpInsights(): Response<NlpInsights> = error("not used")
    override suspend fun getChallenges(): Response<List<ChallengeDto>> = error("not used")
    override suspend fun getMyChallenges(): Response<List<UserChallengeDto>> = error("not used")
    override suspend fun getChallengeRecommendation(): Response<ChallengeRecommendationDto> = error("not used")
    override suspend fun joinChallenge(slug: String): Response<UserChallengeDto> = error("not used")
    override suspend fun checkIn(challengeId: String): Response<UserChallengeDto> = error("not used")
    override suspend fun abandonChallenge(challengeId: String): Response<Unit> = error("not used")
    override suspend fun sendCoachMessage(request: CoachMessageRequest): Response<CoachMessageResponse> = error("not used")
    override suspend fun getPosts(category: String?, page: Int, limit: Int): Response<PostsPage> = error("not used")
    override suspend fun togglePostLike(postId: String): Response<LikeResponse> = error("not used")
    override suspend fun createPost(request: CreatePostRequest): Response<PostDto> = error("not used")
    override suspend fun reportPost(postId: String, request: ReportRequest): Response<Unit> = error("not used")
    override suspend fun getGlobePins(technique: String?, limit: Int): Response<List<GlobePinDto>> = error("not used")
    override suspend fun getGlobeStats(): Response<GlobeStatsDto> = error("not used")
    override suspend fun likePin(pinId: String): Response<PinLikeResponse> = error("not used")
    override suspend fun createPin(request: CreatePinRequest): Response<GlobePinDto> = error("not used")
    override suspend fun reportPin(pinId: String, request: ReportRequest): Response<Unit> = error("not used")
    override suspend fun blockUser(userId: String): Response<Unit> = error("not used")
    override suspend fun syncIntegrations(): Response<Unit> = error("not used")
    override suspend fun disconnectIntegration(provider: String): Response<Unit> = error("not used")
    override suspend fun importHealthData(data: HealthConnectImportRequest): Response<Unit> = error("not used")
}
