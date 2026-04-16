package com.breatheonline.breathe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breatheonline.breathe.ui.theme.AppColors

// ── Tab model ─────────────────────────────────────────────────────────────────

private data class BottomTab(
    val route:    String,
    val label:    String,
    val icon:     ImageVector,
    val isCenter: Boolean = false,
)

private val TABS = listOf(
    BottomTab(Route.HOME,       "Home",    Icons.Default.Home),
    BottomTab(Route.MUSIC,      "Music",   Icons.Default.MusicNote),
    BottomTab(Route.MEDITATION, "Breathe", Icons.Default.Spa,      isCenter = true),
    BottomTab(Route.GLOBE,      "Community", Icons.Default.Forum),
    BottomTab(Route.PROFILE,    "Profile", Icons.Default.Person),
)

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
fun MainBottomBar(
    navController: NavController,
    currentRoute:  String?,
    colors: AppColors,
) {
    NavigationBar(
        modifier       = Modifier.height(72.dp),
        containerColor = colors.surface,
        contentColor   = colors.title,
        tonalElevation = 0.dp,
    ) {
        TABS.forEach { tab ->
            val selected = currentRoute == tab.route

            NavigationBarItem(
                selected = selected,
                onClick  = {
                    navController.navigate(tab.route) {
                        popUpTo(Route.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon = {
                    if (tab.isCenter) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .shadow(if (selected) 8.dp else 4.dp, CircleShape)
                                .background(colors.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = tab.icon,
                                contentDescription = tab.label,
                                tint               = Color.White,
                                modifier           = Modifier.size(24.dp),
                            )
                        }
                    } else {
                        Icon(
                            imageVector        = tab.icon,
                            contentDescription = tab.label,
                        )
                    }
                },
                label = {
                    Text(
                        text       = tab.label,
                        fontSize   = 10.sp,
                        color      = when {
                            tab.isCenter -> colors.primary
                            selected     -> colors.primary
                            else         -> colors.subtitle
                        },
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = colors.primary,
                    unselectedIconColor = colors.subtitle,
                    selectedTextColor   = colors.primary,
                    unselectedTextColor = colors.subtitle,
                    indicatorColor      = if (tab.isCenter) Color.Transparent
                                         else colors.primary.copy(alpha = 0.15f),
                ),
            )
        }
    }
}
