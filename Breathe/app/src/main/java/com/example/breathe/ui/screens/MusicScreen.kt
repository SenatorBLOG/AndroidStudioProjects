package com.example.breathe.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.breathe.data.models.MusicGenre
import com.example.breathe.data.models.MusicTrack
import com.example.breathe.ui.theme.AppColors
import com.example.breathe.viewmodel.MusicViewModel
import com.example.breathe.viewmodel.MusicUiState

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MusicScreen(colors: AppColors) {
    // Activity-scoped ViewModel so playback persists while navigating other tabs
    val activity  = LocalContext.current as ComponentActivity
    val viewModel = hiltViewModel<MusicViewModel>(viewModelStoreOwner = activity)
    val state     by viewModel.state.collectAsState()

    val displayTracks = state.filteredTracks.ifEmpty { if (!state.isLoading) state.tracks else emptyList() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text          = "BREATHE · SOUNDS",
                    style         = MaterialTheme.typography.labelSmall,
                    color         = colors.subtitle,
                    letterSpacing = 2.sp,
                )
                Text(
                    text       = "Music Library",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = colors.title,
                    fontWeight = FontWeight.Light,
                )
            }
            IconButton(onClick = {
                viewModel.loadTracks(
                    if (state.selectedGenre == MusicGenre.ALL) "meditation" else state.selectedGenre.tag
                )
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.subtitle, modifier = Modifier.size(18.dp))
            }
        }

        // ── Search ────────────────────────────────────────────────────────────
        SearchBar(
            query         = state.searchQuery,
            onQueryChange = viewModel::setSearchQuery,
            colors        = colors,
        )

        Spacer(Modifier.height(12.dp))

        // ── Genre chips ───────────────────────────────────────────────────────
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (genre in MusicGenre.entries) {
                item {
                    GenreChip(
                        genre    = genre,
                        selected = state.selectedGenre == genre,
                        onClick  = { viewModel.selectGenre(genre) },
                        colors   = colors,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Status row ────────────────────────────────────────────────────────
        Text(
            text = when {
                state.isLoading -> "Loading…"
                else -> "${displayTracks.size} track${if (displayTracks.size != 1) "s" else ""}" +
                        if (state.searchQuery.isNotEmpty()) " · \"${state.searchQuery}\"" else ""
            },
            style    = MaterialTheme.typography.labelSmall,
            color    = colors.subtitle,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )

        // ── Track list ────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> LoadingSkeleton(colors)

                displayTracks.isEmpty() && state.searchQuery.isNotEmpty() ->
                    EmptySearchState(
                        query   = state.searchQuery,
                        onClear = { viewModel.setSearchQuery("") },
                        colors  = colors,
                    )

                displayTracks.isEmpty() ->
                    EmptyLibraryState(
                        onLoad  = { viewModel.loadTracks() },
                        error   = state.error,
                        colors  = colors,
                    )

                else ->
                    LazyColumn(contentPadding = PaddingValues(bottom = 8.dp)) {
                        itemsIndexed(displayTracks) { index, track ->
                            TrackRow(
                                track     = track,
                                index     = index,
                                isCurrent = state.currentTrack?.id == track.id,
                                isPlaying = state.isPlaying && state.currentTrack?.id == track.id,
                                onPlay    = { viewModel.playTrack(track) },
                                colors    = colors,
                            )
                        }
                    }
            }
        }

        // ── Mini player (persistent at bottom) ────────────────────────────────
        if (state.currentTrack != null) {
            MiniPlayer(
                state      = state,
                onPlayPause = viewModel::togglePlayPause,
                onPrevious  = viewModel::previousTrack,
                onNext      = viewModel::nextTrack,
                onSeek      = viewModel::seekTo,
                colors      = colors,
            )
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, colors: AppColors) {
    val keyboard = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(colors.surface, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = colors.subtitle, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))

        BasicTextField(
            value         = query,
            onValueChange = onQueryChange,
            modifier      = Modifier.weight(1f),
            textStyle     = MaterialTheme.typography.bodySmall.copy(color = colors.text),
            cursorBrush   = SolidColor(colors.primary),
            singleLine    = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            "Search tracks, artists, tags…",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.subtitle.copy(alpha = 0.5f),
                        )
                    }
                    inner()
                }
            },
        )

        if (query.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector        = Icons.Default.Clear,
                contentDescription = "Clear",
                tint               = colors.subtitle,
                modifier           = Modifier.size(16.dp).clickable { onQueryChange("") },
            )
        }
    }
}

// ── Genre chip ────────────────────────────────────────────────────────────────

@Composable
private fun GenreChip(
    genre:    MusicGenre,
    selected: Boolean,
    onClick:  () -> Unit,
    colors:   AppColors,
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) colors.primary.copy(alpha = 0.18f) else colors.surface,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text       = genre.label,
            style      = MaterialTheme.typography.labelSmall,
            color      = if (selected) colors.primary else colors.subtitle,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

// ── Track row ─────────────────────────────────────────────────────────────────

@Composable
private fun TrackRow(
    track:     MusicTrack,
    index:     Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay:    () -> Unit,
    colors:    AppColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCurrent) colors.surface.copy(alpha = 0.7f) else colors.background
            )
            .clickable(onClick = onPlay)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Index / state indicator (width 28dp)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.width(28.dp),
        ) {
            when {
                isCurrent && isPlaying  -> MiniWave(playing = true, colors = colors)
                isCurrent               -> Icon(Icons.Default.Pause, null, tint = colors.primary, modifier = Modifier.size(14.dp))
                else -> Text(
                    text  = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.subtitle.copy(alpha = 0.5f),
                )
            }
        }

        // Album art
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface),
        ) {
            if (track.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier     = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(Icons.Default.MusicNote, null, tint = colors.subtitle.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        // Name + artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = track.name,
                style      = MaterialTheme.typography.bodySmall,
                color      = if (isCurrent) colors.primary else colors.title,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Text(
                text     = track.artistName,
                style    = MaterialTheme.typography.labelSmall,
                color    = colors.subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Tags (first 2, compact)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            track.tags.take(2).forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(colors.surface, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                ) {
                    Text(
                        text  = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle.copy(alpha = 0.6f),
                        fontSize = 8.sp,
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // Duration
        Text(
            text  = fmtSec(track.duration),
            style = MaterialTheme.typography.labelSmall,
            color = colors.subtitle.copy(alpha = 0.5f),
        )
    }
}

// ── Mini player ───────────────────────────────────────────────────────────────

@Composable
private fun MiniPlayer(
    state:       MusicUiState,
    onPlayPause: () -> Unit,
    onPrevious:  () -> Unit,
    onNext:      () -> Unit,
    onSeek:      (Int) -> Unit,
    colors:      AppColors,
) {
    val track = state.currentTrack ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface),
    ) {
        // Seek bar (full width, no padding, flush to top)
        Slider(
            value = if (state.durationMs > 0)
                        state.currentPositionMs.toFloat() / state.durationMs.toFloat()
                    else 0f,
            onValueChange = { fraction -> onSeek((fraction * state.durationMs).toInt()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(horizontal = 4.dp),
            colors = SliderDefaults.colors(
                thumbColor          = colors.primary,
                activeTrackColor    = colors.primary,
                inactiveTrackColor  = colors.subtitle.copy(alpha = 0.20f),
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Album art thumbnail
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.background),
            ) {
                if (track.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(track.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier     = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = colors.subtitle.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.width(10.dp))

            // Track info + time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = track.name,
                    style      = MaterialTheme.typography.bodySmall,
                    color      = colors.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Row {
                    Text(
                        text  = "${fmtMs(state.currentPositionMs)} / ${fmtMs(state.durationMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle.copy(alpha = 0.6f),
                    )
                    if (state.isPreparing) {
                        Spacer(Modifier.width(6.dp))
                        MiniWave(playing = true, colors = colors)
                    }
                }
            }

            // Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.SkipPrevious, null, tint = colors.subtitle, modifier = Modifier.size(22.dp))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable(onClick = onPlayPause),
                ) {
                    Icon(
                        imageVector        = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint               = colors.onPrimary,
                        modifier           = Modifier.size(22.dp),
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, null, tint = colors.subtitle, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

// ── Mini wave animation ───────────────────────────────────────────────────────

@Composable
private fun MiniWave(playing: Boolean, colors: AppColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val h0 by infiniteTransition.animateFloat(0.28f, 1f, infiniteRepeatable(tween(800, 0,   FastOutSlowInEasing), RepeatMode.Reverse), label = "w0")
    val h1 by infiniteTransition.animateFloat(0.28f, 1f, infiniteRepeatable(tween(800, 110, FastOutSlowInEasing), RepeatMode.Reverse), label = "w1")
    val h2 by infiniteTransition.animateFloat(0.28f, 1f, infiniteRepeatable(tween(800, 220, FastOutSlowInEasing), RepeatMode.Reverse), label = "w2")
    val h3 by infiniteTransition.animateFloat(0.28f, 1f, infiniteRepeatable(tween(800, 80,  FastOutSlowInEasing), RepeatMode.Reverse), label = "w3")
    val h4 by infiniteTransition.animateFloat(0.28f, 1f, infiniteRepeatable(tween(800, 170, FastOutSlowInEasing), RepeatMode.Reverse), label = "w4")

    val bars = if (playing) listOf(h0, h1, h2, h3, h4) else List(5) { 0.28f }

    Row(
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        modifier              = Modifier.height(14.dp).width(14.dp),
    ) {
        bars.forEach { h ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight(h)
                    .clip(RoundedCornerShape(1.dp))
                    .background(colors.primary),
            )
        }
    }
}

// ── Loading skeleton ──────────────────────────────────────────────────────────

@Composable
private fun LoadingSkeleton(colors: AppColors) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(12) {
            Row(
                modifier          = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(28.dp).height(12.dp).background(colors.surface, RoundedCornerShape(4.dp)))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.size(40.dp).background(colors.surface, RoundedCornerShape(8.dp)))
                Spacer(Modifier.width(12.dp))
                Column {
                    Box(Modifier.fillMaxWidth(0.55f).height(11.dp).background(colors.surface, RoundedCornerShape(4.dp)))
                    Spacer(Modifier.height(5.dp))
                    Box(Modifier.fillMaxWidth(0.35f).height(9.dp).background(colors.surface, RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}

// ── Empty states ──────────────────────────────────────────────────────────────

@Composable
private fun EmptySearchState(query: String, onClear: () -> Unit, colors: AppColors) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.MusicNote, null, tint = colors.subtitle.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(12.dp))
        Text("No tracks for \"$query\"", style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClear)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text("Clear search", style = MaterialTheme.typography.labelMedium, color = colors.primary)
        }
    }
}

@Composable
private fun EmptyLibraryState(onLoad: () -> Unit, error: String?, colors: AppColors) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.MusicNote, null, tint = colors.subtitle.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            text  = if (error != null) "Couldn't load tracks" else "No tracks found",
            style = MaterialTheme.typography.bodySmall,
            color = colors.subtitle,
        )
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, style = MaterialTheme.typography.labelSmall, color = colors.subtitle.copy(alpha = 0.5f))
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .clickable(onClick = onLoad)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text("Reload", style = MaterialTheme.typography.labelMedium, color = colors.primary)
        }
    }
}

// ── Formatters ────────────────────────────────────────────────────────────────

private fun fmtSec(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

private fun fmtMs(ms: Int): String {
    if (ms <= 0) return "0:00"
    return fmtSec(ms / 1000)
}
