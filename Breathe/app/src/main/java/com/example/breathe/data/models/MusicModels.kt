package com.example.breathe.data.models

import com.google.gson.annotations.SerializedName

// ── Public model ──────────────────────────────────────────────────────────────

data class MusicTrack(
    val id:         String,
    val name:       String,
    val artistName: String,
    val duration:   Int,       // seconds
    val audioUrl:   String,
    val imageUrl:   String,
    val genre:      String,
    val tags:       List<String>,
)

enum class MusicGenre(val tag: String, val label: String) {
    ALL("meditation", "All"),
    MEDITATION("meditation", "Meditation"),
    AMBIENT("ambient", "Ambient"),
    RELAXATION("relaxation", "Relaxation"),
    CHILLOUT("chillout", "Chill Out"),
    LOUNGE("lounge", "Lounge"),
}

// ── Jamendo API response (internal) ───────────────────────────────────────────

internal data class JamendoResponse(
    @SerializedName("results") val results: List<JamendoTrack> = emptyList(),
)

internal data class JamendoTrack(
    @SerializedName("id")           val id:          String,
    @SerializedName("name")         val name:        String,
    @SerializedName("artist_name")  val artistName:  String,
    @SerializedName("duration")     val duration:    Int,
    @SerializedName("audio")        val audio:       String,
    @SerializedName("album_image")  val albumImage:  String?,
    @SerializedName("image")        val image:       String?,
    @SerializedName("musicinfo")    val musicInfo:   JamendoMusicInfo?,
) {
    fun toMusicTrack(): MusicTrack {
        val genres      = musicInfo?.tags?.genres.orEmpty()
        val instruments = musicInfo?.tags?.instruments.orEmpty()
        val vartags     = musicInfo?.tags?.vartags.orEmpty()
        val allTags     = (genres + instruments + vartags).take(3)
        return MusicTrack(
            id         = id,
            name       = name,
            artistName = artistName,
            duration   = duration,
            audioUrl   = audio,
            imageUrl   = albumImage ?: image ?: "",
            genre      = genres.firstOrNull() ?: "meditation",
            tags       = if (allTags.isEmpty()) listOf("meditation") else allTags,
        )
    }
}

internal data class JamendoMusicInfo(
    @SerializedName("tags") val tags: JamendoTags?,
)

internal data class JamendoTags(
    @SerializedName("genres")      val genres:      List<String>?,
    @SerializedName("instruments") val instruments: List<String>?,
    @SerializedName("vartags")     val vartags:     List<String>?,
)
