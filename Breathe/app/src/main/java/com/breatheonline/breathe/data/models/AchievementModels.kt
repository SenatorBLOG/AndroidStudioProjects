package com.breatheonline.breathe.data.models

import com.google.gson.annotations.SerializedName

data class AchievementLevelDto(
    @SerializedName("level") val level: Int,
    @SerializedName("label") val label: String,
    @SerializedName("targetValue") val targetValue: Double,
    @SerializedName("description") val description: String? = null,
    @SerializedName("earnedAt") val earnedAt: String? = null,
)

data class AchievementDto(
    @SerializedName("_id") val id: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("iconKey") val iconKey: String,
    @SerializedName("iconUrl") val iconUrl: String? = null,
    @SerializedName("accentColor") val accentColor: String? = null,
    @SerializedName("sourceTypes") val sourceTypes: List<String> = emptyList(),
    @SerializedName("currentValue") val currentValue: Double = 0.0,
    @SerializedName("currentLevel") val currentLevel: Int = 0,
    @SerializedName("maxLevel") val maxLevel: Int = 0,
    @SerializedName("status") val status: String = "locked",
    @SerializedName("levels") val levels: List<AchievementLevelDto> = emptyList(),
    @SerializedName("lastProgressAt") val lastProgressAt: String? = null,
    @SerializedName("completedAt") val completedAt: String? = null,
)

data class AchievementHighlightsResponse(
    @SerializedName("items") val items: List<AchievementDto> = emptyList(),
)
