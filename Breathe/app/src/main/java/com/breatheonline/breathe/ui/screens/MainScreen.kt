package com.breatheonline.breathe.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors

private data class BottomTab(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val isCenter: Boolean = false,
)

private val TABS = listOf(
    BottomTab(Route.HOME, R.string.tab_home, Icons.Default.Home),
    BottomTab(Route.MEDITATION, R.string.tab_breathe, Icons.Default.Spa, isCenter = true),
    BottomTab(Route.HISTORY, R.string.tab_stats, Icons.Default.BarChart),
    BottomTab(Route.PROFILE, R.string.tab_profile, Icons.Default.Person),
)

private val BottomBarShellHeight = 84.dp

/**
 * Floating glass dock: translucent rounded container with a hairline border,
 * icon-only resting tabs, and a pill that expands with the label on the
 * selected tab. The center Breathe tab is a raised gradient orb.
 */
@Composable
fun MainBottomBar(
    navController: NavController,
    currentRoute: String?,
    colors: AppColors,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(BottomBarShellHeight)
            .padding(horizontal = 24.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.35f),
                )
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        0f to colors.surface.copy(alpha = if (colors.isLight) 0.98f else 0.94f),
                        1f to colors.surface,
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        0f to colors.primary.copy(alpha = 0.20f),
                        1f to colors.subtitle.copy(alpha = 0.06f),
                    ),
                    RoundedCornerShape(30.dp),
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TABS.forEach { tab ->
                val selected = currentRoute == tab.route
                NavItem(
                    tab = tab,
                    selected = selected,
                    colors = colors,
                    onClick = {
                        // For HISTORY, navigate via the helper so the optional query arg is omitted
                        val navRoute = if (tab.route == Route.HISTORY) Route.history() else tab.route
                        navController.navigate(navRoute) {
                            popUpTo(Route.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: BottomTab,
    selected: Boolean,
    colors: AppColors,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "navItemScale_${tab.route}",
    )
    val click: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
    }

    if (tab.isCenter) {
        // Raised gradient orb — the app's signature action
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .size(52.dp)
                .shadow(
                    elevation = if (selected) 14.dp else 9.dp,
                    shape = CircleShape,
                    ambientColor = colors.primary.copy(alpha = 0.50f),
                    spotColor = colors.primary.copy(alpha = 0.50f),
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        0f to colors.primary,
                        1f to colors.orbSecondary,
                    )
                )
                .clickable(interactionSource = interactionSource, indication = null, onClick = click),
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = stringResource(tab.labelRes),
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    } else {
        // Resting: icon only. Selected: pill expands to icon + label.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (selected) colors.primary.copy(alpha = if (colors.isLight) 0.12f else 0.16f)
                    else Color.Transparent
                )
                .clickable(interactionSource = interactionSource, indication = null, onClick = click)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = stringResource(tab.labelRes),
                tint = if (selected) colors.primary else colors.subtitle.copy(alpha = 0.75f),
                modifier = Modifier.size(21.dp),
            )
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Row {
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
