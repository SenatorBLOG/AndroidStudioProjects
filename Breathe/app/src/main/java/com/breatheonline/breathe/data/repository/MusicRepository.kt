package com.breatheonline.breathe.data.repository

import com.breatheonline.breathe.data.models.JamendoResponse
import com.breatheonline.breathe.data.models.MusicTrack
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named

private const val CLIENT_ID = "a70d8995"
private const val BASE_URL  = "https://api.jamendo.com/v3.0/tracks/"

class MusicRepository @Inject constructor(
    @Named("bare") private val client: OkHttpClient,
    private val gson: Gson,
) {
    private val cache = mutableMapOf<String, List<MusicTrack>>()

    suspend fun loadTracks(tag: String = "meditation"): Result<List<MusicTrack>> {
        cache[tag]?.let { return Result.success(it) }
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL?client_id=$CLIENT_ID&format=json&limit=40" +
                          "&tags=$tag&include=musicinfo&audioformat=mp32"
                val request  = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body     = response.body?.string()
                    ?: return@withContext Result.success(emptyList())
                val parsed = gson.fromJson(body, JamendoResponse::class.java)
                val tracks = parsed.results.map { it.toMusicTrack() }
                cache[tag] = tracks
                Result.success(tracks)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
