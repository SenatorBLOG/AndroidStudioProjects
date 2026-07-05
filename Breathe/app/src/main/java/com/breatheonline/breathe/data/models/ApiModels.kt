package com.breatheonline.breathe.data.models

import com.google.gson.annotations.SerializedName

// ── Auth & User ──────────────────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
)

data class RegisterRequest(
    @SerializedName("name")     val name: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user")  val user: User,
)

data class User(
    @SerializedName("_id")         val id: String,
    @SerializedName("name")        val name: String?,
    @SerializedName("nickname")    val nickname: String?,
    @SerializedName("email")       val email: String,
    @SerializedName("avatar")      val avatar: String?,
    @SerializedName("bodyProfile") val bodyProfile: BodyProfile?,
    @SerializedName("createdAt")   val createdAt: String
)

data class BodyProfile(
    @SerializedName("heightCm") val heightCm: Int? = null,
    @SerializedName("weightKg") val weightKg: Int? = null,
    @SerializedName("age")      val age: Int? = null,
    @SerializedName("gender")   val gender: String? = null,
    @SerializedName("goal")     val goal: String? = null,
)

data class UpdateProfileRequest(
    @SerializedName("nickname")    val nickname: String,
    @SerializedName("avatar")      val avatar: String? = null,
    @SerializedName("bodyProfile") val bodyProfile: BodyProfile? = null,
)

// ── Sessions ──────────────────────────────────────────────────────────────────

data class RemoteSession(
    @SerializedName("_id")              val id:               String,
    @SerializedName("type")             val type:             String?,
    @SerializedName("duration")         val duration:         Int,
    @SerializedName("completedAt")      val completedAt:      String? = null,
    @SerializedName("sessionLength")    val sessionLength:    Double? = null,
    @SerializedName("cycles")           val cycles:           Int?    = null,
    @SerializedName("moodBefore")       val moodBefore:       Int?    = null,
    @SerializedName("moodAfter")        val moodAfter:        Int?    = null,
    @SerializedName("focusLevel")       val focusLevel:       Int?    = null,
    @SerializedName("stressLevel")      val stressLevel:      Int?    = null,
    @SerializedName("breathingDepth")   val breathingDepth:   Int?    = null,
    @SerializedName("calmnessScore")    val calmnessScore:    Int?    = null,
    @SerializedName("distractionCount") val distractionCount: Int?    = null,
    @SerializedName("timeOfDay")        val timeOfDay:        String? = null,
    @SerializedName("notes")            val notes:            String? = null,
    @SerializedName("technique")        val technique:        String? = null,
    @SerializedName("sessionDate")      val sessionDate:      String? = null,
) {
    /** Resolves the best available timestamp across old (sessionDate) and new (completedAt) schema. */
    val effectiveDate: String get() = completedAt ?: sessionDate ?: ""
}

data class CreateSessionRequest(
    @SerializedName("type") val type: String,
    @SerializedName("sessionLength") val sessionLength: Int,
    @SerializedName("cycles") val cycles: Int,
    @SerializedName("completedAt") val completedAt: String,
    @SerializedName("sessionDate") val sessionDate: String,
    @SerializedName("timeOfDay") val timeOfDay: String = "",
    @SerializedName("moodBefore") val moodBefore: Int? = null,
    @SerializedName("moodAfter") val moodAfter: Int? = null,
    @SerializedName("focusLevel") val focusLevel: Int? = null,
    @SerializedName("stressLevel") val stressLevel: Int? = null,
    @SerializedName("breathingDepth") val breathingDepth: Int? = null,
    @SerializedName("calmnessScore") val calmnessScore: Int? = null,
    @SerializedName("distractionCount") val distractionCount: Int = 0,
    @SerializedName("notes") val notes: String = "",
    val noiseLevel: String,
)

// ── Journal / NLP ─────────────────────────────────────────────────────────────

data class NlpData(
    @SerializedName("sentiment")          val sentiment:          String?       = null,
    @SerializedName("score")              val score:              Double?       = null,
    @SerializedName("themes")             val themes:             List<String>  = emptyList(),
    @SerializedName("intensity")          val intensity:          Int?          = null,
    @SerializedName("suggestedTechnique") val suggestedTechnique: String?       = null,
    @SerializedName("oneLineSummary")     val oneLineSummary:     String?       = null,
    @SerializedName("analyzedAt")         val analyzedAt:         String?       = null,
)

data class JournalSession(
    @SerializedName("_id")           val id:            String,
    @SerializedName("sessionDate")   val sessionDate:   String?,
    @SerializedName("sessionLength") val sessionLength: Double   = 0.0,
    @SerializedName("moodBefore")    val moodBefore:    Int?     = null,
    @SerializedName("moodAfter")     val moodAfter:     Int?     = null,
    @SerializedName("notes")         val notes:         String?  = null,
    @SerializedName("nlp")           val nlp:           NlpData? = null,
)

data class TopTheme(
    @SerializedName("theme") val theme: String,
    @SerializedName("count") val count: Int,
)

data class SentimentDist(
    @SerializedName("positive") val positive: Int = 0,
    @SerializedName("neutral")  val neutral:  Int = 0,
    @SerializedName("negative") val negative: Int = 0,
)

data class NlpTimelinePoint(
    @SerializedName("date")           val date:           String,
    @SerializedName("score")          val score:          Double,
    @SerializedName("sentiment")      val sentiment:      String,
    @SerializedName("oneLineSummary") val oneLineSummary: String? = null,
)

data class NlpInsights(
    @SerializedName("totalAnalyzed") val totalAnalyzed: Int,
    @SerializedName("avgScore")      val avgScore:      Double?,
    @SerializedName("topThemes")     val topThemes:     List<TopTheme>       = emptyList(),
    @SerializedName("sentimentDist") val sentimentDist: SentimentDist        = SentimentDist(),
    @SerializedName("sessions")      val sessions:      List<JournalSession> = emptyList(),
    @SerializedName("timeline")      val timeline:      List<NlpTimelinePoint> = emptyList(),
)

// ── Challenges ────────────────────────────────────────────────────────────────

data class BadgeDto(
    @SerializedName("emoji")
    val emoji: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("earnedAt")
    val earnedAt: String? = null
)

data class ChallengeDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("slug")
    val slug: String,
    @SerializedName("title")
    val name: String,
    @SerializedName("subtitle")
    val description: String,
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("duration")
    val duration: Int? = null,
    @SerializedName("badge")
    val badge: BadgeDto? = null,
    @SerializedName("joinCount")
    val joinCount: Int? = null
)


data class UserChallengeDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("challenge")
    val challenge: ChallengeDto?,
    @SerializedName("startedAt")
    val startedAt: String,
    @SerializedName("completedAt")
    val completedAt: String? = null,
    @SerializedName("completedDays")
    val completedDays: List<String> = emptyList(),
    @SerializedName("badge")
    val userBadge: BadgeDto? = null,
    @SerializedName("abandoned")
    val abandoned: Boolean = false
)

data class ChallengeRecommendationDto(
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("reason")
    val reason: String? = null,
    @SerializedName("challenge")
    val challenge: ChallengeDto? = null
)

data class CheckInResponse(
    val ok: Boolean,
    val userChallenge: UserChallengeDto,
    val completed: Boolean
)

// ── AI Coach ──────────────────────────────────────────────────────────────────

data class CoachHistoryItem(
    @SerializedName("role") val role: String,   // "user" or "model"
    @SerializedName("text") val text: String,
)

data class CoachMessageRequest(
    @SerializedName("message") val message: String,
    @SerializedName("history") val history: List<CoachHistoryItem> = emptyList(),
)

data class CoachTechnique(
    @SerializedName("key")   val key:   String,
    @SerializedName("label") val label: String,
)

data class CoachMessageResponse(
    @SerializedName("reply")           val reply:           String,
    @SerializedName("technique")       val technique:       CoachTechnique?,
    @SerializedName("messagesLeft")    val messagesLeft:    Int?,
    @SerializedName("isAuthenticated") val isAuthenticated: Boolean,
)

data class CoachLimitError(
    @SerializedName("hoursUntilReset") val hoursUntilReset: Int = 6,
    @SerializedName("isAnonymous")     val isAnonymous:     Boolean = false,
)

// ── Globe pins ────────────────────────────────────────────────────────────────

data class GlobePinDto(
    @SerializedName("_id")        val id:       String,
    @SerializedName("userId")     val userId:   String? = null,
    @SerializedName("username")   val username: String? = null,
    @SerializedName("lat")        val lat:      Double,
    @SerializedName("lng")        val lng:      Double,
    @SerializedName("city")       val city:     String? = null,
    @SerializedName("country")    val country:  String? = null,
    @SerializedName("title")      val title:    String? = null,
    @SerializedName("note")       val note:     String? = null,
    @SerializedName("technique")  val technique:String? = null,
    @SerializedName("photoUrl")   val photoUrl: String? = null,
    @SerializedName("likeCount")  val likeCount:Int     = 0,
    @SerializedName("createdAt")  val createdAt:String? = null,
)

data class TopCityDto(
    @SerializedName("_id")   val city:  String,
    @SerializedName("count") val count: Int,
)

data class GlobeStatsDto(
    @SerializedName("totalPins")          val totalPins:          Int,
    @SerializedName("countries")          val countries:          Int,
    @SerializedName("topCities")          val topCities:          List<TopCityDto>    = emptyList(),
    @SerializedName("techniqueBreakdown") val techniqueBreakdown: Map<String, Int>    = emptyMap(),
)

data class CreatePinRequest(
    @SerializedName("lat")         val lat:         Double,
    @SerializedName("lng")         val lng:         Double,
    @SerializedName("city")        val city:        String = "",
    @SerializedName("country")     val country:     String = "",
    @SerializedName("title")       val title:       String = "Meditation spot",
    @SerializedName("note")        val note:        String = "",
    @SerializedName("technique")   val technique:   String = "other",
    @SerializedName("sessionLink") val sessionLink: String = "",
    @SerializedName("photoUrl")    val photoUrl:    String = "",
)

data class PinLikeResponse(
    @SerializedName("likeCount") val likeCount: Int,
)

// ── Community posts ───────────────────────────────────────────────────────────

data class PostAuthorDto(
    @SerializedName("_id")      val id:       String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("name")     val name:     String? = null,
    @SerializedName("email")    val email:    String? = null,
)

data class PostDto(
    @SerializedName("_id")          val id:           String,
    @SerializedName("author")       val author:       PostAuthorDto? = null,
    @SerializedName("text")         val text:         String,
    @SerializedName("category")     val category:     String         = "experience",
    @SerializedName("tags")         val tags:         List<String>   = emptyList(),
    @SerializedName("likeCount")    val likeCount:    Int            = 0,
    @SerializedName("likedByMe")    val likedByMe:    Boolean        = false,
    @SerializedName("commentCount") val commentCount: Int            = 0,
    @SerializedName("createdAt")    val createdAt:    String         = "",
)

data class PostsPage(
    @SerializedName("posts") val posts: List<PostDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("page")  val page:  Int,
    @SerializedName("pages") val pages: Int,
)

data class CreatePostRequest(
    @SerializedName("text")     val text:     String,
    @SerializedName("category") val category: String       = "experience",
    @SerializedName("tags")     val tags:     List<String> = emptyList(),
)

data class LikeResponse(
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("likedByMe") val likedByMe: Boolean,
)

// ── Moderation ────────────────────────────────────────────────────────────────

data class ReportRequest(
    @SerializedName("reason") val reason: String = "inappropriate",
)

// ── Integrations ──────────────────────────────────────────────────────────────

data class IntegrationStatusDto(
    val provider: String,
    val connected: Boolean,
    val lastSyncAt: String?,
    val data: IntegrationDataDto?
)

data class IntegrationDataDto(
    val sleep: List<SleepDayDto>?,
    val hrv: List<HrvDayDto>?,
    val heartRate: List<HrDayDto>?
)

enum class SleepStage { DEEP, LIGHT, REM, AWAKE }

data class SleepStageSegment(
    @SerializedName("startMin") val startMin: Int,
    @SerializedName("endMin")   val endMin: Int,
    @SerializedName("stage")    val stage: SleepStage,
)

data class SleepDayDto(
    @SerializedName("date")          val date:          String,
    @SerializedName("duration")      val duration:      Int,
    @SerializedName("deepSleepMin")  val deepSleepMin:  Int? = null,
    @SerializedName("remSleepMin")   val remSleepMin:   Int? = null,
    @SerializedName("lightSleepMin") val lightSleepMin: Int? = null,
    @SerializedName("awakeMin")      val awakeMin:      Int? = null,
    @SerializedName("bedtime")       val bedtime:       String? = null,
    @SerializedName("wakeTime")      val wakeTime:      String? = null,
    @SerializedName("stages")        val stages:        List<SleepStageSegment>? = null,
    @SerializedName("score")         val score:         Int? = null,
)
data class HrvDayDto(val date: String, val rmssd: Double?)
data class HrDayDto(val date: String, val restingRate: Int?, val avgRate: Int?)

// Request body for POST /integrations/apple-health (also used for Health Connect import)
data class HealthConnectImportRequest(
    val sleep:     List<SleepDayDto>,
    val hrv:       List<HrvDayDto>,
    val heartRate: List<HrDayDto>,
    val steps:     Int? = null,
)
