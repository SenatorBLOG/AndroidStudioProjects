package com.breatheonline.breathe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.MusicUiState
import kotlinx.coroutines.delay

@Composable
fun GlobalMiniPlayer(
    state:    MusicUiState,
    colors:   AppColors,
    onToggle: () -> Unit,
    onNext:   () -> Unit,
    onPrev:   () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = state.currentTrack ?: return

    var expanded  by remember { mutableStateOf(false) }
    var discAngle by remember { mutableFloatStateOf(0f) }

    // Spin the disc while playing; freeze at current angle when paused
    LaunchedEffect(state.isPlaying) {
        if (!state.isPlaying) return@LaunchedEffect
        while (true) {
            discAngle = (discAngle + 1.5f) % 360f
            delay(16L)
        }
    }

    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.End,
        verticalArrangement   = Arrangement.spacedBy(8.dp),
    ) {
        // ── Spinning disc FAB ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(6.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(colors.surface)
                .border(
                    width = if (state.isPlaying) 2.dp else 1.dp,
                    color = if (state.isPlaying) colors.primary.copy(alpha = 0.55f)
                            else colors.subtitle.copy(alpha = 0.20f),
                    shape = CircleShape,
                )
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center,
        ) {
            // Album art or fallback icon — the whole disc spins
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .graphicsLayer { rotationZ = discAngle },
                contentAlignment = Alignment.Center,
            ) {
                // Solid background so fallback icon is visible
                Box(
                    Modifier
                        .size(44.dp)
                        .background(colors.primary.copy(alpha = 0.20f)),
                )
                if (track.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(track.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint               = colors.primary.copy(alpha = 0.7f),
                        modifier           = Modifier.size(22.dp),
                    )
                }
            }
            // Vinyl center hole — always on top, doesn't spin
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colors.background),
            )
        }

        // ── Expanded controls panel ───────────────────────────────────────────
        AnimatedVisibility(
            visible = expanded,
            enter   = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit    = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text       = track.name,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = colors.title,
                    fontWeight = FontWeight.SemiBold,
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
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier         = Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onPrev),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.SkipPrevious, null, tint = colors.subtitle, modifier = Modifier.size(22.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable(onClick = onToggle),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint               = colors.onPrimary,
                            modifier           = Modifier.size(22.dp),
                        )
                    }
                    Box(
                        modifier         = Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onNext),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.SkipNext, null, tint = colors.subtitle, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}
