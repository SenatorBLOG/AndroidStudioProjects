package com.breatheonline.breathe.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.breatheonline.breathe.data.models.CoachTechnique
import com.breatheonline.breathe.ui.screens.Route
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.AiCoachViewModel
import com.breatheonline.breathe.viewmodel.ChatMessage
import com.breatheonline.breathe.viewmodel.ChatRole
import kotlinx.coroutines.delay

// ── Bottom sheet entry point ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachBottomSheet(
    onDismiss:     () -> Unit,
    navController: NavController,
    colors: AppColors,
    viewModel: AiCoachViewModel = hiltViewModel(),
) {
    val state     by viewModel.state.collectAsState()
    val listState  = rememberLazyListState()
    var inputText  by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Scroll to bottom whenever message count changes
    LaunchedEffect(state.messages.size, state.isLoading) {
        val target = if (state.isLoading) state.messages.size else state.messages.lastIndex
        if (target >= 0) listState.animateScrollToItem(target)
    }
    // On first open, jump to last message immediately
    LaunchedEffect(Unit) {
        if (state.messages.isNotEmpty()) listState.scrollToItem(state.messages.lastIndex)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.surface,
        dragHandle       = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing green dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(colors.primary, CircleShape),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text          = "AI COACH",
                        style         = MaterialTheme.typography.labelSmall,
                        color         = colors.subtitle,
                        letterSpacing = 3.sp,
                        fontWeight    = FontWeight.Bold,
                    )
                    state.messagesLeft?.let { left ->
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text  = "$left left today",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.subtitle.copy(alpha = 0.55f),
                        )
                    }
                }
                Row {
                    IconButton(onClick = viewModel::reset) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = colors.subtitle, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.subtitle, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Divider
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.subtitle.copy(alpha = 0.12f)))

            // ── Messages ──────────────────────────────────────────────────────
            LazyColumn(
                state                  = listState,
                modifier               = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 420.dp)
                    .padding(horizontal = 16.dp),
                contentPadding         = PaddingValues(vertical = 12.dp),
                verticalArrangement    = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(state.messages) { index, message ->
                    val isLatestCoach =
                        message.role == ChatRole.COACH && index == state.messages.lastIndex

                    when (message.role) {
                        ChatRole.USER  -> UserBubble(message, colors)
                        ChatRole.COACH -> CoachBubble(
                            message   = message,
                            isLatest  = isLatestCoach && !state.isLoading,
                            colors    = colors,
                            onTry     = { technique ->
                                onDismiss()
                                val exerciseType = techniqueKeyToRoute(technique.key)
                                navController.navigate(Route.breathe(exerciseType)) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }

                if (state.isLoading) {
                    item { TypingIndicator(colors) }
                }
            }

            // Divider
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.subtitle.copy(alpha = 0.12f)))

            // ── Input area ────────────────────────────────────────────────────
            if (state.limitReached) {
                LimitBanner(hoursUntilReset = state.hoursUntilReset, colors = colors)
            } else {
                InputRow(
                    value         = inputText,
                    onValueChange = { inputText = it },
                    onSend        = {
                        val t = inputText.trim()
                        if (t.isNotEmpty()) { viewModel.send(t); inputText = "" }
                    },
                    isLoading     = state.isLoading,
                    colors        = colors,
                )
            }

            // "Powered by Gemini" footer
            Text(
                text      = "POWERED BY GEMINI AI",
                style     = MaterialTheme.typography.labelSmall,
                color     = colors.subtitle.copy(alpha = 0.30f),
                letterSpacing = 1.5.sp,
                modifier  = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp, top = 4.dp),
            )
        }
    }
}

// ── User bubble ───────────────────────────────────────────────────────────────

@Composable
private fun UserBubble(message: ChatMessage, colors: AppColors) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = colors.primary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp),
                )
                .border(
                    width = 1.dp,
                    color = colors.primary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp),
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text  = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.title,
            )
        }
    }
}

// ── Coach bubble ──────────────────────────────────────────────────────────────

@Composable
private fun CoachBubble(
    message: ChatMessage,
    isLatest: Boolean,
    colors: AppColors,
    onTry:    (CoachTechnique) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = colors.background,
                    shape = RoundedCornerShape(2.dp, 16.dp, 16.dp, 16.dp),
                )
                .border(
                    width = 1.dp,
                    color = colors.subtitle.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(2.dp, 16.dp, 16.dp, 16.dp),
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            TypewriterText(
                text     = message.text,
                isLatest = isLatest,
                colors   = colors,
            )
        }

        // "Try technique" button
        if (message.technique != null) {
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primary)
                    .clickable { onTry(message.technique) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text       = "Try ${message.technique.label}",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = colors.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector        = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint               = colors.onPrimary,
                    modifier           = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ── Typewriter text ────────────────────────────────────────────────────────────

@Composable
private fun TypewriterText(text: String, isLatest: Boolean, colors: AppColors) {
    var displayed by remember(text) { mutableStateOf(if (isLatest) "" else text) }

    LaunchedEffect(text, isLatest) {
        if (isLatest) {
            val start = displayed.length
            for (i in start..text.length) {
                displayed = text.substring(0, i)
                delay(12L)
            }
        } else {
            displayed = text
        }
    }

    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text  = displayed,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.text,
        )
        if (isLatest && displayed.length < text.length) {
            Spacer(Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(2.dp)
                    .background(colors.primary),
            )
        }
    }
}

// ── Typing indicator ──────────────────────────────────────────────────────────

@Composable
private fun TypingIndicator(colors: AppColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    fun dotSpec(offsetMs: Int) = infiniteRepeatable<Float>(
        animation = keyframes {
            durationMillis = 1200
            0f at 0
            if (offsetMs > 0) 0f at offsetMs
            (-9f) at (offsetMs + 220)
            0f at (offsetMs + 440)
            0f at 1200
        },
        repeatMode = RepeatMode.Restart,
    )

    val y0 by infiniteTransition.animateFloat(0f, 0f, dotSpec(0),   label = "d0")
    val y1 by infiniteTransition.animateFloat(0f, 0f, dotSpec(300), label = "d1")
    val y2 by infiniteTransition.animateFloat(0f, 0f, dotSpec(600), label = "d2")

    Row(
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = colors.background,
                    shape = RoundedCornerShape(2.dp, 16.dp, 16.dp, 16.dp),
                )
                .border(
                    1.dp, colors.subtitle.copy(alpha = 0.15f),
                    RoundedCornerShape(2.dp, 16.dp, 16.dp, 16.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(y0, y1, y2).forEach { y ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .offset { IntOffset(0, y.toInt()) }
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.65f)),
                    )
                }
            }
        }
    }
}

// ── Input row ─────────────────────────────────────────────────────────────────

@Composable
private fun InputRow(
    value:         String,
    onValueChange: (String) -> Unit,
    onSend:        () -> Unit,
    isLoading:     Boolean,
    colors: AppColors,
) {
    val canSend = value.isNotBlank() && !isLoading

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.weight(1f),
            textStyle     = MaterialTheme.typography.bodyMedium.copy(color = colors.text),
            cursorBrush   = SolidColor(colors.primary),
            singleLine    = false,
            maxLines      = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .background(colors.background, RoundedCornerShape(20.dp))
                        .border(1.5.dp, colors.subtitle.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    if (value.isEmpty()) {
                        Text(
                            "Ask about meditation...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.subtitle.copy(alpha = 0.45f),
                        )
                    }
                    innerTextField()
                }
            },
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (canSend) colors.primary else colors.background)
                .border(1.dp, colors.subtitle.copy(alpha = 0.20f), CircleShape)
                .clickable(enabled = canSend, onClick = onSend),
        ) {
            Icon(
                imageVector        = Icons.Default.Send,
                contentDescription = "Send",
                tint               = if (canSend) colors.onPrimary else colors.subtitle.copy(alpha = 0.35f),
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}

// ── Limit banner ──────────────────────────────────────────────────────────────

@Composable
private fun LimitBanner(hoursUntilReset: Int, colors: AppColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(
                color = colors.primary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp),
            )
            .border(1.dp, colors.primary.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column {
            Text(
                text       = "Daily limit reached",
                style      = MaterialTheme.typography.labelMedium,
                color      = colors.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text     = "Resets in ${hoursUntilReset}h. Create an account for more queries.",
                style    = MaterialTheme.typography.labelSmall,
                color    = colors.subtitle,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

// ── Route key mapping ─────────────────────────────────────────────────────────

/** Maps backend technique keys to Android exercise route types. */
private fun techniqueKeyToRoute(key: String): String = when (key) {
    "wim-hof"  -> "wimhof"
    "4-7-8"    -> "4-7-8"
    "box"      -> "box"
    "coherent" -> "coherent"
    "belly"    -> "belly"
    "alternate"-> "alternate"
    else       -> key
}
