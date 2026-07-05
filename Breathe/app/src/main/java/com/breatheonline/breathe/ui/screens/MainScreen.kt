package com.breatheonline.breathe.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
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
    val scale by animateFloatAsState(
        targetValue = if (selected && tab.isCenter) 1.08f else if (selected) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "navItemScale_${tab.route}",
    )

    val interactionSource = remember { MutableInteractionSource() }

    if (tab.isCenter) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(68.dp)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .offset(y = (-4).dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(
                        elevation = if (selected) 12.dp else 8.dp,
                        shape = CircleShape,
                        ambientColor = colors.primary.copy(alpha = 0.42f),
                        spotColor = colors.primary.copy(alpha = 0.42f),
                    )
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = stringResource(tab.labelRes),
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text      = stringResource(tab.labelRes),
                style     = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, textAlign = TextAlign.Center),
                color     = if (selected) colors.primary else colors.subtitle,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(64.dp)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .graphicsLayer { scaleX = scale; scaleY = scale },
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.12f)),
                    )
                }
                Icon(
                    imageVector = tab.icon,
                    contentDescription = stringResource(tab.labelRes),
                    tint = if (selected) colors.primary else colors.subtitle,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text      = stringResource(tab.labelRes),
                style     = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, textAlign = TextAlign.Center),
                color     = if (selected) colors.primary else colors.subtitle,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}
