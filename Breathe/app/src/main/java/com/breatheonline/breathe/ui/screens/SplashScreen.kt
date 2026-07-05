package com.breatheonline.breathe.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel (thin — just reads token state) ─────────────────────────────────

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
) : ViewModel() {
    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * Entry point of the app.
 * Animates the logo in, then routes to [main] or [login] based on token presence.
 *
 * Navigation wiring in MainActivity:
 *   startDestination = "splash"
 *   composable("splash") { SplashScreen(navController, colors) }
 */
@Composable
fun SplashScreen(
    navController: NavController,
    colors: AppColors,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val scale = remember { Animatable(0.25f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade-in and spring-scale run in parallel, then navigate
        launch {
            alpha.animateTo(
                targetValue   = 1f,
                animationSpec = tween(durationMillis = 600),
            )
        }
        scale.animateTo(
            targetValue   = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMediumLow,
            ),
        )
        delay(200)

        val destination = if (viewModel.isLoggedIn()) Route.HOME else Route.LOGIN
        navController.navigate(destination) {
            popUpTo(Route.SPLASH) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        // Animated logo container — scales + fades in together
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(scale.value)
                .alpha(alpha.value)
                .drawBehind {
                    // Outer radial glow (same technique as the breathing circle)
                    val glowRadius = size.width / 2 + 72.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to colors.glowOuter,
                            1f to Color.Transparent,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = glowRadius,
                        ),
                        radius = glowRadius,
                    )
                    // Inner soft highlight
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f    to colors.glowInner.copy(alpha = 0.04f),
                                0.85f to colors.glowInner.copy(alpha = 0.22f),
                                1f    to colors.glowInner.copy(alpha = 0.40f),
                            ),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.width / 2,
                        ),
                        radius = size.width / 2,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text  = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.title,
                )
                Text(
                    text     = stringResource(R.string.splash_tagline),
                    style    = MaterialTheme.typography.titleMedium,
                    color    = colors.subtitle,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
