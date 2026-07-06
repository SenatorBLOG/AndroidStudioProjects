package com.breatheonline.breathe

import com.breatheonline.breathe.data.repository.MusicRepository
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class MusicRepositoryCacheTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun makeRepo(interceptor: Interceptor) = MusicRepository(
        client = OkHttpClient.Builder().addInterceptor(interceptor).build(),
        gson   = Gson(),
    )

    private fun stubInterceptor(callCount: AtomicInteger, json: String) = Interceptor { chain ->
        callCount.incrementAndGet()
        Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    fun `cache miss fetches from network`() = runTest {
        val calls = AtomicInteger(0)
        val repo = makeRepo(stubInterceptor(calls, EMPTY_RESPONSE))

        repo.loadTracks("meditation")

        assertEquals(1, calls.get())
    }

    @Test
    fun `cache hit skips network on second call for same tag`() = runTest {
        val calls = AtomicInteger(0)
        val repo = makeRepo(stubInterceptor(calls, EMPTY_RESPONSE))

        repo.loadTracks("meditation")
        repo.loadTracks("meditation")

        assertEquals(1, calls.get())
    }

    @Test
    fun `different tags are cached independently and each hits network once`() = runTest {
        val calls = AtomicInteger(0)
        val repo = makeRepo(stubInterceptor(calls, EMPTY_RESPONSE))

        repo.loadTracks("meditation")
        repo.loadTracks("ambient")
        repo.loadTracks("ambient")    // second "ambient" → cache hit

        assertEquals(2, calls.get())
    }

    @Test
    fun `successful response returns parsed tracks`() = runTest {
        val repo = makeRepo(stubInterceptor(AtomicInteger(), SINGLE_TRACK_RESPONSE))

        val result = repo.loadTracks("meditation")

        assertTrue(result.isSuccess)
        val tracks = result.getOrThrow()
        assertEquals(1, tracks.size)
        assertEquals("Calm", tracks[0].name)
        assertEquals("TestArtist", tracks[0].artistName)
        assertEquals(300, tracks[0].duration)
    }

    @Test
    fun `empty results list returns success with empty list`() = runTest {
        val repo = makeRepo(stubInterceptor(AtomicInteger(), EMPTY_RESPONSE))

        val result = repo.loadTracks("lounge")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().size)
    }

    @Test
    fun `cached result equals original network result`() = runTest {
        val repo = makeRepo(stubInterceptor(AtomicInteger(), SINGLE_TRACK_RESPONSE))

        val first  = repo.loadTracks("meditation").getOrThrow()
        val second = repo.loadTracks("meditation").getOrThrow()

        assertEquals(first, second)
    }

    // ── Fixtures ──────────────────────────────────────────────────────────────

    companion object {
        private const val EMPTY_RESPONSE = """{"results":[]}"""

        private val SINGLE_TRACK_RESPONSE = """
            {
              "results": [{
                "id": "42",
                "name": "Calm",
                "artist_name": "TestArtist",
                "duration": 300,
                "audio": "https://example.com/track.mp3",
                "album_image": "https://example.com/cover.jpg",
                "image": null,
                "musicinfo": null
              }]
            }
        """.trimIndent()
    }
}
