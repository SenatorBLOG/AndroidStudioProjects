package com.example.breathe.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.breathe.ui.screens.profile.ProfileScreen
import com.example.breathe.ui.theme.AppColors

// ── Route constants ───────────────────────────────────────────────────────────

object Route {
    const val SPLASH                = "splash"
    const val LOGIN                 = "login"
    const val REGISTER              = "register"
    const val HOME                  = "home"
    const val BREATHE               = "breathe/{exerciseType}"   // template — used in NavHost
    const val MEDITATION            = "meditation"
    const val PROFILE               = "profile"
    const val HISTORY               = "history"
    const val MEDITATION_REGULARITY = "meditation_regularity"
    const val SESSION_HISTORY       = "session_history"
    const val FAQ                   = "faq"
    const val JOURNAL               = "journal"
    const val PRIVACY_POLICY        = "privacy_policy"
    const val MUSIC                 = "music"
    const val GLOBE                 = "globe"
    const val INTERACTIVE           = "interactive"
    const val ARTICLE               = "article?url={url}"

    /** Build a filled breathe path, e.g. Route.breathe("4-7-8") → "breathe/4-7-8" */
    fun breathe(type: String = "4-7-8") = "breathe/$type"

    /** Build a custom breathe path, e.g. Route.breatheCustom(4,0,6,2) → "breathe/custom_4_0_6_2" */
    fun breatheCustom(inhale: Int, hold1: Int, exhale: Int, hold2: Int) =
        "breathe/custom_${inhale}_${hold1}_${exhale}_${hold2}"

    /** Build an article WebView path — URL is encoded to survive Navigation route parsing. */
    fun article(url: String) =
        "article?url=${java.net.URLEncoder.encode(url, "UTF-8")}"
}

/** Routes where the bottom bar should be visible. */
private val BOTTOM_BAR_ROUTES = setOf(
    Route.HOME,
    Route.MUSIC,
    Route.MEDITATION,
    Route.GLOBE,
    Route.PROFILE,
)

// ── Root NavGraph ─────────────────────────────────────────────────────────────

/**
 * Single-activity navigation graph.
 * [MainActivity] calls this as its sole `setContent` child — all routing lives here.
 *
 * Back navigation is handled automatically by [NavHost]; pressing Back on HOME
 * exits the app (it is the start destination of the main-app stack).
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    colors: AppColors,
    onThemeChange: (String) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    // destination.route returns the template string ("breathe/{exerciseType}"),
    // which is exactly what we store in Route.BREATHE — so all comparisons are exact.
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentRoute in BOTTOM_BAR_ROUTES) {
                MainBottomBar(
                    navController = navController,
                    currentRoute  = currentRoute,
                    colors        = colors,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController      = navController,
            startDestination   = Route.SPLASH,
            modifier           = Modifier.padding(innerPadding),
            enterTransition    = { fadeIn(tween(300)) },
            exitTransition     = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition  = { fadeOut(tween(300)) },
        ) {

            // ── Auth ──────────────────────────────────────────────────────────
            composable(Route.SPLASH) {
                SplashScreen(navController, colors)
            }
            composable(Route.LOGIN) {
                LoginScreen(navController, colors)
            }
            composable(Route.REGISTER) {
                RegisterScreen(navController, colors)
            }

            // ── Main app ──────────────────────────────────────────────────────
            composable(Route.HOME) {
                HomeScreen(navController, colors)
            }
            composable(
                route              = Route.BREATHE,
                arguments          = listOf(
                    navArgument("exerciseType") { type = NavType.StringType }
                ),
                enterTransition    = { slideInVertically(tween(400)) { it } + fadeIn(tween(400)) },
                exitTransition     = { fadeOut(tween(300)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition  = { slideOutVertically(tween(350)) { it } + fadeOut(tween(350)) },
            ) { backStack ->
                val exerciseType = backStack.arguments?.getString("exerciseType") ?: "4-7-8"
                BreatheScreen(
                    navController = navController,
                    colors        = colors,
                    exerciseType  = exerciseType,
                    onThemeChange = onThemeChange,
                )
            }
            composable(Route.MEDITATION) {
                BreatheScreen(
                    navController = navController,
                    colors        = colors,
                    exerciseType  = "4-7-8",
                    onThemeChange = onThemeChange,
                )
            }
            composable(Route.PROFILE) {
                ProfileScreen(
                    colors        = colors,
                    navController = navController,
                    onThemeChange = onThemeChange,
                )
            }
            composable(Route.HISTORY) {
                StatsScreen(colors = colors, navController = navController, modifier = androidx.compose.ui.Modifier)
            }
            composable(Route.MEDITATION_REGULARITY) {
                MeditationRegularityScreen(colors = colors, navController = navController)
            }
            composable(Route.SESSION_HISTORY) {
                HistoryScreen(navController = navController, colors = colors)
            }
            composable(Route.FAQ) {
                FaqScreen(colors = colors, navController = navController)
            }
            composable(Route.JOURNAL) {
                JournalScreen(colors = colors, navController = navController)
            }
            composable(Route.PRIVACY_POLICY) {
                PrivacyPolicyScreen(colors = colors, navController = navController)
            }
            composable(Route.MUSIC) {
                MusicScreen(colors = colors)
            }
            composable(Route.GLOBE) {
                GlobeScreen(colors = colors)
            }
            composable(Route.INTERACTIVE) {
                InteractiveScreen(navController = navController, colors = colors)
            }
            composable(
                route     = Route.ARTICLE,
                arguments = listOf(navArgument("url") { type = NavType.StringType; defaultValue = "" }),
            ) { backStack ->
                ArticleScreen(
                    navController = navController,
                    encodedUrl    = backStack.arguments?.getString("url") ?: "",
                    colors        = colors,
                )
            }
        }
    }
}
