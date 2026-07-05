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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.icons.LucideAppIcons
import com.breatheonline.breathe.ui.theme.AppColors

@Composable
fun FaqScreen(colors: AppColors, navController: NavController) {
    var activeCategory by remember { mutableStateOf(FAQ_DATA[0].id) }
    var search by remember { mutableStateOf("") }

    val currentCat = FAQ_DATA.find { it.id == activeCategory } ?: FAQ_DATA[0]
    val filtered = remember(search) {
        if (search.trim().length > 1) {
            FAQ_DATA.flatMap { cat ->
                cat.items
                    .filter { it.q.contains(search, ignoreCase = true) || it.a.contains(search, ignoreCase = true) }
                    .map { Triple(it, cat.label, cat.iconKey) }
            }
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
                Text(stringResource(R.string.faq_title), style = MaterialTheme.typography.titleLarge, color = colors.title, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.faq_subtitle), style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
            }
        }

        FaqSearchBar(
            search = search,
            onSearch = { search = it },
            onClear = { search = "" },
            colors = colors,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        if (filtered == null) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FAQ_DATA.forEach { cat ->
                    CategoryPill(
                        cat = cat,
                        active = cat.id == activeCategory,
                        colors = colors,
                        onClick = { activeCategory = cat.id },
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (filtered != null) {
                item(key = "search_header") {
                    Text(
                        "${filtered.size} result${if (filtered.size != 1) "s" else ""} for \"$search\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                if (filtered.isEmpty()) {
                    item(key = "search_empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = LucideAppIcons.Search,
                                    contentDescription = null,
                                    tint = colors.subtitle,
                                    modifier = Modifier.size(36.dp),
                                )
                                Text(stringResource(R.string.faq_no_results), color = colors.title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    stringResource(R.string.faq_try_different_search),
                                    color = colors.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(filtered, key = { _, triple -> triple.first.q }) { i, (item, catLabel, catIconKey) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 4.dp),
                            ) {
                                Icon(
                                    imageVector = faqCategoryIcon(catIconKey),
                                    contentDescription = null,
                                    tint = colors.subtitle,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = catLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.subtitle,
                                    letterSpacing = 0.8.sp,
                                )
                            }
                            AccordionItem(item = item, index = i, colors = colors)
                        }
                    }
                }
            } else {
                item(key = "cat_header_${currentCat.id}") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 4.dp),
                    ) {
                        Icon(
                            imageVector = faqCategoryIcon(currentCat.iconKey),
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            currentCat.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.title,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.subtitle.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(stringResource(R.string.faq_question_count, currentCat.items.size), style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
                        }
                    }
                }
                itemsIndexed(currentCat.items, key = { _, item -> item.q }) { i, item ->
                    AccordionItem(item = item, index = i, colors = colors)
                }
                item(key = "cta") { CtaCard(colors) }
                item(key = "bottom_spacer") { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun FaqSearchBar(
    search: String,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.Search, null, tint = colors.subtitle.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        BasicTextField(
            value = search,
            onValueChange = onSearch,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.title),
            cursorBrush = SolidColor(colors.primary),
            decorationBox = { inner ->
                Box {
                    if (search.isEmpty()) {
                        Text(stringResource(R.string.faq_search_placeholder), style = MaterialTheme.typography.bodyMedium, color = colors.subtitle.copy(alpha = 0.45f))
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
                tint = colors.subtitle,
                modifier = Modifier.size(14.dp).clickable(onClick = onClear),
            )
        }
    }
}

@Composable
private fun CategoryPill(
    cat: FaqCategory,
    active: Boolean,
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
            Icon(
                imageVector = faqCategoryIcon(cat.iconKey),
                contentDescription = null,
                tint = if (active) colors.primary else colors.subtitle,
                modifier = Modifier.size(13.dp),
            )
            Text(
                cat.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) colors.primary else colors.subtitle,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun AccordionItem(item: FaqItem, index: Int, colors: AppColors) {
    var open by remember { mutableStateOf(false) }
    val chevronAngle by animateFloatAsState(
        targetValue = if (open) 180f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "chevron",
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { open = !open }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = String.format("%02d", index + 1),
                style = MaterialTheme.typography.labelSmall,
                color = colors.subtitle.copy(alpha = 0.45f),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = item.q,
                style = MaterialTheme.typography.bodyMedium,
                color = if (open) colors.title else colors.text,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = if (open) colors.primary else colors.subtitle.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp).rotate(chevronAngle),
            )
        }

        AnimatedVisibility(
            visible = open,
            enter = fadeIn(tween(200)) + expandVertically(tween(260)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(220)),
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
                    text = item.a,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.subtitle,
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
        Text(stringResource(R.string.faq_still_questions), style = MaterialTheme.typography.bodyMedium, color = colors.title, fontWeight = FontWeight.SemiBold)
        Text(
            "Can't find what you're looking for? Ask in the community - our members and the team are happy to help.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.subtitle,
            lineHeight = 18.sp,
        )
    }
}

private fun faqCategoryIcon(iconKey: String): ImageVector = when (iconKey) {
    "sprout" -> LucideAppIcons.Sprout
    "waves" -> LucideAppIcons.Waves
    "brain" -> LucideAppIcons.Brain
    "user" -> LucideAppIcons.UserRound
    "globe" -> LucideAppIcons.Globe
    else -> LucideAppIcons.CircleHelp
}
