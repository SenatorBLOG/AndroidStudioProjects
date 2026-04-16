package com.breatheonline.breathe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breatheonline.breathe.ui.theme.AppColors

// ── Data ──────────────────────────────────────────────────────────────────────

private data class FaqItem(val q: String, val a: String)
private data class FaqCategory(val id: String, val icon: String, val label: String, val items: List<FaqItem>)

private val FAQ_DATA = listOf(
    FaqCategory(
        id = "basics", icon = "🌱", label = "Getting Started",
        items = listOf(
            FaqItem("What is Breathe?",
                "Breathe is a guided breathing and meditation app. You follow an animated orb that expands and contracts — inhaling when it grows, exhaling when it shrinks. No prior experience needed."),
            FaqItem("Do I need to create an account?",
                "No. You can use the breathing sessions completely free without an account. Creating an account lets you save your session history, track streaks, and join the community."),
            FaqItem("How long should my first session be?",
                "Start with 3–5 minutes. Even 2 minutes of controlled breathing creates a measurable shift in your nervous system. Build up gradually — most users settle into 10–15 minute daily sessions after a week."),
            FaqItem("What time of day is best?",
                "Morning sessions set a calm baseline for the day. Evening sessions help you wind down. The best time is simply when you can be consistent — even a lunch break works great."),
        )
    ),
    FaqCategory(
        id = "techniques", icon = "🌊", label = "Techniques",
        items = listOf(
            FaqItem("What is Box Breathing (4-4-4-4)?",
                "Inhale for 4 seconds, hold for 4, exhale for 4, hold again for 4. Used by Navy SEALs and athletes to regain focus under pressure. Excellent for stress and pre-performance anxiety."),
            FaqItem("What is 4-7-8 breathing?",
                "Inhale for 4 seconds, hold for 7, then exhale slowly for 8. The extended exhale activates your parasympathetic nervous system — lowering heart rate and cortisol. Most people feel drowsy within 2–3 cycles."),
            FaqItem("What is the Wim Hof Method?",
                "30 deep power breaths followed by a breath retention. This temporarily floods your body with oxygen and adrenaline, creating an energised, alert state. Not recommended before driving or in water."),
            FaqItem("What is Coherent Breathing?",
                "Breathing at ~5.5 breaths per minute synchronises your heart rate variability (HRV) to its resonant frequency — associated with reduced anxiety, improved mood, and better cardiovascular health."),
            FaqItem("Can I create a custom pattern?",
                "Yes. On the breathing screen you can set your own inhale, hold, exhale, and hold-out durations. Your custom pattern is used for the session immediately."),
        )
    ),
    FaqCategory(
        id = "science", icon = "🧠", label = "The Science",
        items = listOf(
            FaqItem("Why does slow breathing reduce stress?",
                "Slow, controlled exhales stimulate the vagus nerve, which directly signals your brain to reduce the stress response. Heart rate drops, cortisol decreases, and prefrontal cortex activity increases."),
            FaqItem("What is heart rate variability (HRV)?",
                "HRV is the variation in time between heartbeats. Higher HRV indicates a more resilient nervous system. Breathing exercises — especially coherent breathing — are one of the most effective ways to improve HRV over time."),
            FaqItem("How many sessions until I feel a difference?",
                "Most people notice a shift in their anxiety levels after 5–7 consistent sessions. Structural benefits to HRV and baseline calm typically appear after 3–4 weeks of daily practice."),
        )
    ),
    FaqCategory(
        id = "account", icon = "👤", label = "Account",
        items = listOf(
            FaqItem("How are my sessions saved?",
                "Sessions are automatically saved when you stop a breathing session. You can add a mood rating and notes in the feedback modal that appears after stopping."),
            FaqItem("What does mood tracking do?",
                "After each session you can log your mood before and after on a 1–10 scale. Over time the Stats page shows your mood trends so you can see how meditation is shifting your baseline."),
            FaqItem("Can I delete my session history?",
                "Yes. On the Sessions tab you can delete individual sessions. Deleted sessions cannot be recovered."),
            FaqItem("How do I delete my account?",
                "Go to Profile → logout, then contact support. This permanently removes all your data including sessions, mood logs, and streaks."),
        )
    ),
    FaqCategory(
        id = "community", icon = "🌍", label = "Community",
        items = listOf(
            FaqItem("Who can read community posts?",
                "Everyone — you do not need an account to read posts and comments. Creating an account is only required to write posts, leave comments, or like content."),
            FaqItem("What can I post?",
                "Share your experiences, ask questions about techniques, celebrate streaks and milestones, or post tips you have discovered. Keep it supportive and on-topic."),
            FaqItem("How do I report inappropriate content?",
                "Tap the flag icon on any post to report it. Reported posts are reviewed manually. We aim to act on reports within 24 hours."),
        )
    ),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun FaqScreen(colors: AppColors, navController: NavController) {
    var activeCategory by remember { mutableStateOf(FAQ_DATA[0].id) }
    var search by remember { mutableStateOf("") }

    val currentCat = FAQ_DATA.find { it.id == activeCategory } ?: FAQ_DATA[0]

    // null = not searching; list = search results (may be empty)
    val filtered = remember(search) {
        if (search.trim().length > 1) {
            FAQ_DATA.flatMap { cat ->
                cat.items
                    .filter { it.q.contains(search, ignoreCase = true) || it.a.contains(search, ignoreCase = true) }
                    .map { Triple(it, cat.label, cat.icon) }
            }
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.subtitle, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("FAQ", style = MaterialTheme.typography.titleLarge, color = colors.title, fontWeight = FontWeight.SemiBold)
                Text("Help & frequently asked questions", style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
            }
        }

        // ── Search bar ────────────────────────────────────────────────────────
        FaqSearchBar(
            search  = search,
            onSearch = { search = it },
            onClear = { search = "" },
            colors  = colors,
            modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        // ── Category pills (hidden while searching) ───────────────────────────
        if (filtered == null) {
            Row(
                modifier              = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FAQ_DATA.forEach { cat ->
                    CategoryPill(
                        cat    = cat,
                        active = cat.id == activeCategory,
                        colors = colors,
                        onClick = { activeCategory = cat.id },
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── Content ───────────────────────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (filtered != null) {
                // ── Search results ────────────────────────────────────────────
                Text(
                    "${filtered.size} result${if (filtered.size != 1) "s" else ""} for \"$search\"",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.subtitle,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                if (filtered.isEmpty()) {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("🔍", fontSize = 36.sp)
                            Text("No results found", color = colors.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Try a different search term",
                                color     = colors.subtitle,
                                style     = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    filtered.forEachIndexed { i, (item, catLabel, catIcon) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "$catIcon $catLabel",
                                style         = MaterialTheme.typography.labelSmall,
                                color         = colors.subtitle,
                                letterSpacing = 0.8.sp,
                                modifier      = Modifier.padding(horizontal = 4.dp),
                            )
                            AccordionItem(item = item, index = i, colors = colors)
                        }
                    }
                }
            } else {
                // ── Category questions ────────────────────────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.padding(horizontal = 4.dp),
                ) {
                    Text(currentCat.icon, fontSize = 20.sp)
                    Text(
                        currentCat.label,
                        style      = MaterialTheme.typography.titleMedium,
                        color      = colors.title,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.subtitle.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("${currentCat.items.size} q", style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
                    }
                }

                currentCat.items.forEachIndexed { i, item ->
                    AccordionItem(item = item, index = i, colors = colors)
                }

                // CTA card
                CtaCard(colors)

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ── Components ────────────────────────────────────────────────────────────────

@Composable
private fun FaqSearchBar(
    search:   String,
    onSearch: (String) -> Unit,
    onClear:  () -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.Search, null, tint = colors.subtitle.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        BasicTextField(
            value         = search,
            onValueChange = onSearch,
            singleLine    = true,
            textStyle     = MaterialTheme.typography.bodyMedium.copy(color = colors.title),
            cursorBrush   = SolidColor(colors.primary),
            decorationBox = { inner ->
                Box {
                    if (search.isEmpty()) {
                        Text("Search questions…", style = MaterialTheme.typography.bodyMedium, color = colors.subtitle.copy(alpha = 0.45f))
                    }
                    inner()
                }
            },
            modifier = Modifier.weight(1f),
        )
        if (search.isNotEmpty()) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint     = colors.subtitle,
                modifier = Modifier.size(14.dp).clickable(onClick = onClear),
            )
        }
    }
}

@Composable
private fun CategoryPill(
    cat:     FaqCategory,
    active:  Boolean,
    colors: AppColors,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) colors.primary.copy(alpha = 0.12f) else colors.surface)
            .border(
                1.dp,
                if (active) colors.primary.copy(alpha = 0.40f) else colors.subtitle.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(cat.icon, fontSize = 13.sp)
            Text(
                cat.label,
                style      = MaterialTheme.typography.labelSmall,
                color      = if (active) colors.primary else colors.subtitle,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun AccordionItem(item: FaqItem, index: Int, colors: AppColors) {
    var open by remember { mutableStateOf(false) }
    val chevronAngle by animateFloatAsState(
        targetValue  = if (open) 180f else 0f,
        animationSpec = tween(durationMillis = 280),
        label        = "chevron",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (open) colors.primary.copy(alpha = 0.04f) else colors.surface)
            .border(
                1.dp,
                if (open) colors.primary.copy(alpha = 0.28f) else colors.subtitle.copy(alpha = 0.10f),
                RoundedCornerShape(14.dp),
            ),
    ) {
        // Header
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable { open = !open }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text       = String.format("%02d", index + 1),
                style      = MaterialTheme.typography.labelSmall,
                color      = colors.subtitle.copy(alpha = 0.45f),
                fontFamily = FontFamily.Monospace,
                modifier   = Modifier.width(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text     = item.q,
                style    = MaterialTheme.typography.bodyMedium,
                color    = if (open) colors.title else colors.text,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint     = if (open) colors.primary else colors.subtitle.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp).rotate(chevronAngle),
            )
        }

        // Answer (animated)
        AnimatedVisibility(
            visible = open,
            enter   = fadeIn(tween(200)) + expandVertically(tween(260)),
            exit    = fadeOut(tween(150)) + shrinkVertically(tween(220)),
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(start = 48.dp, end = 16.dp, bottom = 14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(1.dp))
                        .background(colors.subtitle.copy(alpha = 0.20f)),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = item.a,
                    style      = MaterialTheme.typography.bodySmall,
                    color      = colors.subtitle,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@Composable
private fun CtaCard(colors: AppColors) {
    Spacer(Modifier.height(4.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Still have questions?", style = MaterialTheme.typography.bodyMedium, color = colors.title, fontWeight = FontWeight.SemiBold)
        Text(
            "Can't find what you're looking for? Ask in the community — our members and the team are happy to help.",
            style      = MaterialTheme.typography.bodySmall,
            color      = colors.subtitle,
            lineHeight = 18.sp,
        )
    }
}
