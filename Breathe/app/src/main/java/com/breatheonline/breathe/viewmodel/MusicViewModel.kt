package com.breatheonline.breathe.viewmodel

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.models.MusicGenre
import com.breatheonline.breathe.data.models.MusicTrack
import com.breatheonline.breathe.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── State ─────────────────────────────────────────────────────────────────────

data class MusicUiState(
    val tracks:           List<MusicTrack> = emptyList(),
    val filteredTracks:   List<MusicTrack> = emptyList(),
    val isLoading:        Boolean          = false,
    val currentTrack:     MusicTrack?      = null,
    val isPlaying:        Boolean          = false,
    val isPreparing:      Boolean          = false,
    val currentPositionMs: Int             = 0,
    val durationMs:       Int              = 0,
    val volume:           Float            = 0.8f,
    val isMuted:          Boolean          = false,
    val selectedGenre: MusicGenre = MusicGenre.ALL,
    val searchQuery:      String           = "",
    val error:            String?          = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MusicUiState())
    val state: StateFlow<MusicUiState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var positionJob: Job?         = null

    init { loadTracks("meditation") }

    // ── Track loading ─────────────────────────────────────────────────────────

    fun loadTracks(tag: String = "meditation") {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.loadTracks(tag)
                .onSuccess { tracks ->
                    _state.update { s -> s.copy(tracks = tracks, isLoading = false) }
                    applyFilter(tracks)
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }

    fun selectGenre(genre: MusicGenre) {
        _state.update { it.copy(selectedGenre = genre) }
        loadTracks(if (genre == MusicGenre.ALL) "meditation" else genre.tag)
    }

    fun setSearchQuery(q: String) {
        _state.update { it.copy(searchQuery = q) }
        applyFilter()
    }

    private fun applyFilter(freshTracks: List<MusicTrack>? = null) {
        val st      = _state.value
        val source  = freshTracks ?: st.tracks
        val q       = st.searchQuery.lowercase()
        val filtered = if (q.isEmpty()) source
        else source.filter { t ->
            t.name.lowercase().contains(q) ||
            t.artistName.lowercase().contains(q) ||
            t.tags.any { it.lowercase().contains(q) }
        }
        _state.update { it.copy(filteredTracks = filtered) }
    }

    // ── Playback ──────────────────────────────────────────────────────────────

    fun playTrack(track: MusicTrack) {
        if (_state.value.currentTrack?.id == track.id) {
            togglePlayPause()
            return
        }
        stopAndRelease()
        _state.update { it.copy(
            currentTrack      = track,
            isPreparing       = true,
            isPlaying         = false,
            currentPositionMs = 0,
            durationMs        = 0,
        )}

        mediaPlayer = MediaPlayer().apply {
            try {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(track.audioUrl)
                setOnPreparedListener { mp ->
                    mp.setVolume(effectiveVolume(), effectiveVolume())
                    mp.start()
                    _state.update { it.copy(
                        isPlaying   = true,
                        isPreparing = false,
                        durationMs  = mp.duration.coerceAtLeast(0),
                    )}
                    startPositionTracking()
                }
                setOnCompletionListener { nextTrack() }
                setOnErrorListener { _, _, _ ->
                    _state.update { it.copy(isPreparing = false, isPlaying = false) }
                    true
                }
                prepareAsync()
            } catch (_: Exception) {
                _state.update { it.copy(isPreparing = false) }
            }
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (_state.value.isPreparing) return
        try {
            if (mp.isPlaying) {
                mp.pause()
                _state.update { it.copy(isPlaying = false) }
                positionJob?.cancel()
            } else {
                mp.start()
                _state.update { it.copy(isPlaying = true) }
                startPositionTracking()
            }
        } catch (_: Exception) {}
    }

    fun seekTo(posMs: Int) {
        try {
            mediaPlayer?.seekTo(posMs)
            _state.update { it.copy(currentPositionMs = posMs) }
        } catch (_: Exception) {}
    }

    fun nextTrack() {
        val tracks = displayTracks()
        val idx    = tracks.indexOfFirst { it.id == _state.value.currentTrack?.id }
        if (idx >= 0) playTrack(tracks[(idx + 1) % tracks.size])
    }

    fun previousTrack() {
        val tracks = displayTracks()
        val idx    = tracks.indexOfFirst { it.id == _state.value.currentTrack?.id }
        if (idx >= 0) playTrack(tracks[(idx - 1 + tracks.size) % tracks.size])
    }

    fun setVolume(v: Float) {
        _state.update { it.copy(volume = v, isMuted = v == 0f) }
        applyVolume()
    }

    fun toggleMute() {
        _state.update { it.copy(isMuted = !it.isMuted) }
        applyVolume()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun displayTracks() =
        _state.value.filteredTracks.ifEmpty { _state.value.tracks }

    private fun effectiveVolume(): Float {
        val s = _state.value
        return if (s.isMuted) 0f else s.volume
    }

    private fun applyVolume() {
        val vol = effectiveVolume()
        try { mediaPlayer?.setVolume(vol, vol) } catch (_: Exception) {}
    }

    private fun startPositionTracking() {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (true) {
                delay(500)
                val mp = mediaPlayer ?: break
                try {
                    if (mp.isPlaying) {
                        val pos = mp.currentPosition
                        if (pos >= 0) _state.update { it.copy(currentPositionMs = pos) }
                    }
                } catch (_: Exception) { break }
            }
        }
    }

    private fun stopAndRelease() {
        positionJob?.cancel()
        try { mediaPlayer?.stop(); mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
    }

    override fun onCleared() {
        stopAndRelease()
        super.onCleared()
    }
}
