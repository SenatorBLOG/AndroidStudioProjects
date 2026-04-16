package com.breatheonline.breathe.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.breatheonline.breathe.data.models.GlobePinDto
import com.breatheonline.breathe.data.models.PostDto
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.GlobePinsViewModel
import com.breatheonline.breathe.viewmodel.PinsStatus
import com.breatheonline.breathe.viewmodel.PostsStatus
import com.breatheonline.breathe.viewmodel.PostsViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.OnCircleAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import java.time.Duration
import java.time.Instant
import kotlin.collections.forEach
import kotlin.math.absoluteValue

// ── Constants ─────────────────────────────────────────────────────────────────

private val FILTER_CATS = listOf(
    "all"         to "All",
    "experience"  to "Experience",
    "question"    to "Question",
    "achievement" to "Achievement",
    "tip"         to "Tip",
)

private val COMPOSE_CATS = listOf(
    "experience"  to "✨ Experience",
    "question"    to "❓ Question",
    "achievement" to "🏆 Achievement",
    "tip"         to "💡 Tip",
)

private val AVATAR_COLORS = listOf(
    Color(0xFF7C6EF8), Color(0xFFE879F9), Color(0xFF34D399),
    Color(0xFFFBBF24), Color(0xFF60A5FA), Color(0xFFF87171),
)

private val TECHNIQUE_LABELS = mapOf(
    "box"       to "Box",
    "4-7-8"     to "4-7-8",
    "wim-hof"   to "Wim Hof",
    "coherent"  to "Coherent",
    "belly"     to "Belly",
    "alternate" to "Alternate",
    "other"     to "Other",
)

private val TECHNIQUE_COLORS = mapOf(
    "box"       to Color(0xFF3A82F7),
    "4-7-8"     to Color(0xFF7AC4FF),
    "wim-hof"   to Color(0xFFFF9A5C),
    "coherent"  to Color(0xFF4ADE80),
    "belly"     to Color(0xFFFFD97D),
    "alternate" to Color(0xFFC084FC),
    "other"     to Color(0xFF94A3B8),
)

private val TECHNIQUE_EMOJIS = mapOf(
    "box"       to "🔵",
    "4-7-8"     to "🌙",
    "wim-hof"   to "🔥",
    "coherent"  to "💚",
    "belly"     to "🌊",
    "alternate" to "🌸",
    "other"     to "🧘",
)

private val FILTER_TECHS = listOf("all" to "All") +
    TECHNIQUE_LABELS.entries.map { it.key to it.value }

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun catColor(cat: String): Color = when (cat) {
    "experience"  -> Color(0xFF7C6EF8)
    "question"    -> Color(0xFFFB923C)
    "achievement" -> Color(0xFF4ADE80)
    "tip"         -> Color(0xFF38BDF8)
    else          -> Color(0xFF7C6EF8)
}

private fun avatarColor(name: String): Color =
    AVATAR_COLORS[name.hashCode().absoluteValue % AVATAR_COLORS.size]

private fun timeAgo(iso: String): String = try {
    val secs = Duration.between(Instant.parse(iso), Instant.now()).seconds
    when {
        secs < 60     -> "just now"
        secs < 3_600  -> "${secs / 60}m ago"
        secs < 86_400 -> "${secs / 3_600}h ago"
        else          -> "${secs / 86_400}d ago"
    }
} catch (_: Exception) { "" }

private fun authorName(post: PostDto): String =
    post.author?.name?.takeIf { it.isNotBlank() }
        ?: post.author?.username?.takeIf { it.isNotBlank() }
        ?: "User"

private fun techColor(tech: String): Color = TECHNIQUE_COLORS[tech] ?: Color(0xFF94A3B8)
private fun techLabel(tech: String): String = TECHNIQUE_LABELS[tech] ?: tech.replaceFirstChar { it.uppercase() }
private fun techEmoji(tech: String): String = TECHNIQUE_EMOJIS[tech] ?: "🧘"

/** Hex color string for a breathing technique — used for Mapbox circle annotations. */
private fun techHex(tech: String?): String = when (tech) {
    "box"       -> "#3A82F7"
    "4-7-8"     -> "#7AC4FF"
    "wim-hof"   -> "#FF9A5C"
    "coherent"  -> "#4ADE80"
    "belly"     -> "#FFD97D"
    "alternate" -> "#C084FC"
    else        -> "#94A3B8"
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobeScreen(
    colors: AppColors,
    postsVm: PostsViewModel = hiltViewModel(),
    pinsVm: GlobePinsViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 8.dp),
        ) {
            Text(
                text          = "COMMUNITY",
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.primary,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 4.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "Share your journey",
                style      = MaterialTheme.typography.headlineMedium,
                color      = colors.title,
                fontWeight = FontWeight.Light,
            )
        }

        // ── Tab bar ───────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = colors.background,
            contentColor     = colors.primary,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color    = colors.primary,
                )
            },
            divider  = {},
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick  = { selectedTab = 0 },
                text = {
                    Text(
                        text       = "💬 Posts",
                        fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (selectedTab == 0) colors.primary else colors.subtitle,
                    )
                },
            )
            Tab(
                selected = selectedTab == 1,
                onClick  = { selectedTab = 1 },
                text = {
                    Text(
                        text       = "🌍 Spots",
                        fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (selectedTab == 1) colors.primary else colors.subtitle,
                    )
                },
            )
        }

        // ── Tab content ───────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (selectedTab) {
                0 -> PostsTab(colors = colors, viewModel = postsVm)
                1 -> SpotsTab(colors = colors, viewModel = pinsVm)
            }
        }
    }
}

// ── Posts tab ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostsTab(
    colors: AppColors,
    viewModel: PostsViewModel,
) {
    val state     by viewModel.state.collectAsState()
    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()

    val shouldLoadMore by remember {
        derivedStateOf {
            val info        = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= info.totalItemsCount - 4 && info.totalItemsCount > 0
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    var showCompose by remember { mutableStateOf(false) }
    var composeText by remember { mutableStateOf("") }
    var composeCat  by remember { mutableStateOf("experience") }

    // ── Moderation state ──────────────────────────────────────────────────────
    val snackbarHostState          = remember { SnackbarHostState() }
    var reportPostId               by remember { mutableStateOf<String?>(null) }
    var blockUserId                by remember { mutableStateOf<String?>(null) }
    var blockUserName              by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.consumeSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            item {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.padding(bottom = 12.dp, top = 8.dp),
                ) {
                    items(FILTER_CATS) { (key, label) ->
                        val selected = state.selectedCat == key
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(
                                    color = if (selected) colors.primary else colors.surface,
                                    shape = RoundedCornerShape(20.dp),
                                )
                                .clickable { viewModel.setCategory(key) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text       = label,
                                style      = MaterialTheme.typography.labelMedium,
                                color      = if (selected) colors.onPrimary else colors.subtitle,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            when (val status = state.status) {
                is PostsStatus.Loading -> {
                    items(5) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    color = colors.surface.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(18.dp),
                                ),
                        )
                    }
                }
                is PostsStatus.Error -> {
                    item {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text      = status.message,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = colors.subtitle,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = viewModel::load) {
                                Text("Retry", color = colors.primary)
                            }
                        }
                    }
                }
                is PostsStatus.Empty -> {
                    item {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Forum,
                                contentDescription = null,
                                tint               = colors.subtitle,
                                modifier           = Modifier.size(48.dp),
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("No posts yet", style = MaterialTheme.typography.bodyMedium, color = colors.subtitle)
                            Text(
                                text     = "Be the first to share!",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = colors.label,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
                is PostsStatus.Success -> {
                    items(state.posts, key = { it.id }) { post ->
                        PostCard(
                            post     = post,
                            colors   = colors,
                            onLike   = { viewModel.toggleLike(post.id) },
                            onReport = { reportPostId = post.id },
                            onBlock  = {
                                val uid  = post.author?.id
                                val name = post.author?.name?.takeIf { it.isNotBlank() }
                                    ?: post.author?.username ?: "this user"
                                if (uid != null) { blockUserId = uid; blockUserName = name }
                            },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            if (state.isLoadingMore) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color       = colors.primary,
                            strokeWidth = 2.dp,
                            modifier    = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick        = { showCompose = true },
            containerColor = colors.primary,
            contentColor   = colors.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "New post")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            snackbar  = { Snackbar(it) },
        )
    }

    // ── Report confirmation ───────────────────────────────────────────────────
    reportPostId?.let { postId ->
        AlertDialog(
            onDismissRequest = { reportPostId = null },
            title   = { Text("Report post?") },
            text    = { Text("This post will be reviewed by our moderation team.") },
            confirmButton = {
                TextButton(onClick = { viewModel.reportPost(postId); reportPostId = null }) {
                    Text("Report", color = Color(0xFFF87171))
                }
            },
            dismissButton = {
                TextButton(onClick = { reportPostId = null }) { Text("Cancel") }
            },
        )
    }

    // ── Block user confirmation ───────────────────────────────────────────────
    blockUserId?.let { userId ->
        AlertDialog(
            onDismissRequest = { blockUserId = null; blockUserName = null },
            title   = { Text("Block $blockUserName?") },
            text    = { Text("You won't see their posts anymore.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.blockUser(userId)
                    blockUserId   = null
                    blockUserName = null
                }) {
                    Text("Block", color = Color(0xFFF87171))
                }
            },
            dismissButton = {
                TextButton(onClick = { blockUserId = null; blockUserName = null }) { Text("Cancel") }
            },
        )
    }

    if (showCompose) {
        ModalBottomSheet(
            onDismissRequest = {
                showCompose = false
                composeText = ""
                composeCat  = "experience"
            },
            sheetState     = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
            dragHandle     = { BottomSheetDefaults.DragHandle(color = colors.subtitle.copy(alpha = 0.4f)) },
        ) {
            ComposePostSheet(
                colors    = colors,
                text      = composeText,
                onText    = { composeText = it },
                category  = composeCat,
                onCat     = { composeCat = it },
                isPosting = state.isPosting,
                onPost    = {
                    if (composeText.isNotBlank()) {
                        viewModel.createPost(composeText, composeCat)
                        showCompose = false
                        composeText = ""
                        composeCat  = "experience"
                    }
                },
            )
        }
    }
}

// ── Spots tab — Mapbox native map ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpotsTab(
    colors: AppColors,
    viewModel: GlobePinsViewModel,
) {
    val state  by viewModel.state.collectAsState()
    val scope  = rememberCoroutineScope()
    var selectedPin by remember { mutableStateOf<GlobePinDto?>(null) }
    var showAddPin  by remember { mutableStateOf(false) }

    val snackbarHostState   = remember { SnackbarHostState() }
    var reportPinId         by remember { mutableStateOf<String?>(null) }
    var blockPinUserId      by remember { mutableStateOf<String?>(null) }
    var blockPinUserName    by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.consumeSnackbar()
        }
    }

    // Refs held across recompositions
    val mapViewRef         = remember { mutableStateOf<MapView?>(null) }
    val mapboxMapState     = remember { mutableStateOf<MapboxMap?>(null) }
    val circleManagerState = remember { mutableStateOf<CircleAnnotationManager?>(null) }
    // Stable identity map: Mapbox annotation String ID → GlobePinDto
    val annotationIndex    = remember { mutableMapOf<String, GlobePinDto>() }

    // Destroy MapView when this composable leaves composition
    DisposableEffect(Unit) {
        onDispose { mapViewRef.value?.onDestroy() }
    }

    // Sync pins to map whenever the list changes OR manager first becomes ready
    LaunchedEffect(state.pins, circleManagerState.value) {
        val mgr = circleManagerState.value ?: return@LaunchedEffect
        mgr.deleteAll()
        annotationIndex.clear()
        state.pins.forEach { pin ->
            val annotation = mgr.create(
                CircleAnnotationOptions()
                    .withPoint(Point.fromLngLat(pin.lng, pin.lat))
                    .withCircleRadius(8.0)
                    .withCircleColor(techHex(pin.technique))
                    .withCircleStrokeWidth(2.0)
                    .withCircleStrokeColor("#FFFFFF")
                    .withCircleOpacity(0.9)
            )
            annotationIndex[annotation.id] = pin
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Mapbox map view ───────────────────────────────────────────────────
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory  = { ctx ->
                MapView(ctx).also { mv ->
                    mapViewRef.value = mv
                    mv.mapboxMap.loadStyle(Style.DARK) {
                        val mgr = mv.annotations.createCircleAnnotationManager()
                        mgr.addClickListener(OnCircleAnnotationClickListener { annotation ->
                            selectedPin = annotationIndex[annotation.id]
                            true
                        })
                        circleManagerState.value = mgr
                        mapboxMapState.value     = mv.mapboxMap
                    }
                    // Initial world view — broad overview centered on Europe/Africa
                    mv.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(20.0, 20.0))
                            .zoom(1.5)
                            .build()
                    )
                }
            },
        )

        // ── Technique filter chips — overlaid at top ──────────────────────────
        LazyRow(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(colors.background.copy(alpha = 0.82f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(FILTER_TECHS) { (key, label) ->
                val selected = state.filterTech == key
                val tc = if (key == "all") colors.primary else techColor(key)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            color = if (selected) tc else colors.surface.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .clickable { viewModel.setFilterTech(key) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text       = if (key == "all") "All" else "${techEmoji(key)} $label",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = if (selected) Color.White else colors.subtitle,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }

        // ── Stats badges — overlaid below filter row ──────────────────────────
        state.stats?.let { stats ->
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                GlobeStatBadge("📍 ${stats.totalPins}", colors)
                GlobeStatBadge("🌐 ${stats.countries}", colors)
            }
        }

        // ── Loading overlay ────────────────────────────────────────────────────
        if (state.status is PinsStatus.Loading) {
            CircularProgressIndicator(
                color    = colors.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp),
            )
        }

        // ── Error overlay ──────────────────────────────────────────────────────
        if (state.status is PinsStatus.Error) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(colors.surface.copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "Failed to load spots",
                        color      = colors.title,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = viewModel::loadPins) {
                        Text("Retry", color = colors.primary)
                    }
                }
            }
        }

        // ── FAB — add new spot ─────────────────────────────────────────────────
        FloatingActionButton(
            onClick        = { showAddPin = true },
            containerColor = colors.primary,
            contentColor   = colors.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add spot")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            snackbar  = { Snackbar(it) },
        )
    }

    // ── Report confirmation ───────────────────────────────────────────────────
    reportPinId?.let { pinId ->
        AlertDialog(
            onDismissRequest = { reportPinId = null },
            title   = { Text("Report spot?") },
            text    = { Text("This spot will be reviewed by our moderation team.") },
            confirmButton = {
                TextButton(onClick = { viewModel.reportPin(pinId); reportPinId = null }) {
                    Text("Report", color = Color(0xFFF87171))
                }
            },
            dismissButton = {
                TextButton(onClick = { reportPinId = null }) { Text("Cancel") }
            },
        )
    }

    // ── Block user confirmation ───────────────────────────────────────────────
    blockPinUserId?.let { userId ->
        AlertDialog(
            onDismissRequest = { blockPinUserId = null; blockPinUserName = null },
            title   = { Text("Block $blockPinUserName?") },
            text    = { Text("You won't see their spots anymore.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.blockUser(userId)
                    blockPinUserId   = null
                    blockPinUserName = null
                }) {
                    Text("Block", color = Color(0xFFF87171))
                }
            },
            dismissButton = {
                TextButton(onClick = { blockPinUserId = null; blockPinUserName = null }) { Text("Cancel") }
            },
        )
    }

    // ── Add-pin bottom sheet ──────────────────────────────────────────────────
    if (showAddPin) {
        ModalBottomSheet(
            onDismissRequest = { showAddPin = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor   = colors.surface,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = colors.subtitle.copy(alpha = 0.4f)) },
        ) {
            AddPinSheet(
                colors    = colors,
                isAdding  = state.isAddingPin,
                onAdd     = { lat, lng, city, country, title, note, technique ->
                    viewModel.createPin(lat, lng, city, country, title, note, technique)
                    showAddPin = false
                },
                onDismiss = { showAddPin = false },
            )
        }
    }

    // ── Pin detail bottom sheet ───────────────────────────────────────────────
    selectedPin?.let { pin ->
        ModalBottomSheet(
            onDismissRequest = { selectedPin = null },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor   = colors.surface,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = colors.subtitle.copy(alpha = 0.4f)) },
        ) {
            PinDetailSheet(
                pin    = pin,
                colors = colors,
                onLike = {
                    viewModel.likePin(pin.id)
                    selectedPin = null
                },
                onFlyTo = {
                    // Animate Mapbox camera to the selected pin
                    mapViewRef.value?.camera?.flyTo(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(pin.lng, pin.lat))
                            .zoom(10.0)
                            .build()
                    )
                    selectedPin = null
                },
                onReport = {
                    selectedPin = null
                    reportPinId = pin.id
                },
                onBlock = pin.userId?.let { uid ->
                    {
                        selectedPin      = null
                        blockPinUserId   = uid
                        blockPinUserName = pin.username ?: "this user"
                    }
                },
            )
        }
    }
}

// ── Pin detail sheet ──────────────────────────────────────────────────────────

@Composable
private fun PinDetailSheet(
    pin: GlobePinDto,
    colors: AppColors,
    onLike:  () -> Unit,
    onFlyTo: () -> Unit,
    onReport: () -> Unit,
    onBlock:  (() -> Unit)?,
) {
    val tech = pin.technique ?: "other"
    val tc   = techColor(tech)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tc.copy(alpha = 0.15f)),
            ) {
                Text(text = techEmoji(tech), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text       = pin.title ?: "Meditation spot",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = colors.title,
                    fontWeight = FontWeight.SemiBold,
                )
                val location = listOfNotNull(
                    pin.city?.takeIf    { it.isNotBlank() },
                    pin.country?.takeIf { it.isNotBlank() },
                ).joinToString(", ")
                if (location.isNotBlank()) {
                    Text(
                        text  = "📍 $location",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.subtitle,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .background(tc.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text       = techLabel(tech),
                    style      = MaterialTheme.typography.labelSmall,
                    color      = tc,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (!pin.note.isNullOrBlank()) {
            Text(
                text  = pin.note,
                style = MaterialTheme.typography.bodySmall,
                color = colors.title.copy(alpha = 0.85f),
            )
        }

        Text(
            text  = "by ${pin.username ?: "Anonymous"} · ${timeAgo(pin.createdAt ?: "")}",
            style = MaterialTheme.typography.labelSmall,
            color = colors.subtitle,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick  = onFlyTo,
                modifier = Modifier.weight(1f),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor   = colors.onPrimary,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("🗺 Fly there")
            }
            Button(
                onClick  = onLike,
                modifier = Modifier.weight(1f),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF87171).copy(alpha = 0.15f),
                    contentColor   = Color(0xFFF87171),
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("❤️ ${pin.likeCount}")
            }
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = onReport,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Flag, null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Report spot", color = Color(0xFFF87171), style = MaterialTheme.typography.labelMedium)
            }
            if (onBlock != null) {
                TextButton(
                    onClick  = onBlock,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Block, null, tint = colors.subtitle, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Block user", color = colors.subtitle, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ── Post card ─────────────────────────────────────────────────────────────────

@Composable
private fun PostCard(
    post: PostDto,
    colors: AppColors,
    onLike:   () -> Unit,
    onReport: () -> Unit,
    onBlock:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name    = authorName(post)
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val cc      = catColor(post.category)
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(avatarColor(name)),
            ) {
                Text(
                    text       = initial,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text       = name,
                    style      = MaterialTheme.typography.bodySmall,
                    color      = colors.title,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text  = timeAgo(post.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.subtitle,
                )
            }
            Box(
                modifier = Modifier
                    .background(cc.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text       = post.category.replaceFirstChar { it.uppercase() },
                    style      = MaterialTheme.typography.labelSmall,
                    color      = cc,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector        = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint               = colors.subtitle,
                        modifier           = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text        = { Text("Report post") },
                        leadingIcon = { Icon(Icons.Default.Flag, null, tint = Color(0xFFF87171), modifier = Modifier.size(18.dp)) },
                        onClick     = { showMenu = false; onReport() },
                    )
                    DropdownMenuItem(
                        text        = { Text("Block user") },
                        leadingIcon = { Icon(Icons.Default.Block, null, tint = colors.subtitle, modifier = Modifier.size(18.dp)) },
                        onClick     = { showMenu = false; onBlock() },
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text     = post.text,
            style    = MaterialTheme.typography.bodySmall,
            color    = colors.title,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis,
        )

        if (post.tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                post.tags.take(4).forEach { tag ->
                    Text(
                        text  = "#$tag",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary.copy(alpha = 0.8f),
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val heartColor by animateColorAsState(
                targetValue = if (post.likedByMe) Color(0xFFF87171) else colors.subtitle,
                label       = "heart",
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.clickable(onClick = onLike),
            ) {
                Icon(
                    imageVector        = if (post.likedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint               = heartColor,
                    modifier           = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = "${post.likeCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = heartColor,
                )
            }
            if (post.commentCount > 0) {
                Spacer(Modifier.width(16.dp))
                Text(
                    text  = "💬 ${post.commentCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.subtitle,
                )
            }
        }
    }
}

// ── Globe stat badge ──────────────────────────────────────────────────────────

@Composable
private fun GlobeStatBadge(text: String, colors: AppColors) {
    Box(
        modifier = Modifier
            .background(colors.surface.copy(alpha = 0.88f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.labelSmall,
            color      = colors.title,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Compose post sheet ────────────────────────────────────────────────────────

@Composable
private fun ComposePostSheet(
    colors: AppColors,
    text:      String,
    onText:    (String) -> Unit,
    category:  String,
    onCat:     (String) -> Unit,
    isPosting: Boolean,
    onPost:    () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
            .navigationBarsPadding(),
    ) {
        Text(
            text       = "New post",
            style      = MaterialTheme.typography.titleMedium,
            color      = colors.title,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(bottom = 16.dp),
        )

        OutlinedTextField(
            value         = text,
            onValueChange = { if (it.length <= 600) onText(it) },
            placeholder   = { Text("What's on your mind?", color = colors.subtitle) },
            modifier      = Modifier.fillMaxWidth().height(140.dp),
            maxLines      = 6,
            colors        = outlineFieldColors(colors),
        )

        Text(
            text     = "${text.length}/600",
            style    = MaterialTheme.typography.labelSmall,
            color    = colors.subtitle,
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp, bottom = 16.dp),
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(COMPOSE_CATS) { (key, label) ->
                val selected = category == key
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            color = if (selected) colors.primary else Color.Transparent,
                            shape = RoundedCornerShape(20.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) colors.primary else colors.subtitle.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .clickable { onCat(key) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text       = label,
                        style      = MaterialTheme.typography.labelMedium,
                        color      = if (selected) colors.onPrimary else colors.subtitle,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick  = onPost,
            enabled  = text.isNotBlank() && !isPosting,
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = colors.primary,
                contentColor           = colors.onPrimary,
                disabledContainerColor = colors.primary.copy(alpha = 0.4f),
            ),
            shape = RoundedCornerShape(14.dp),
        ) {
            if (isPosting) {
                CircularProgressIndicator(
                    color       = colors.onPrimary,
                    strokeWidth = 2.dp,
                    modifier    = Modifier.size(18.dp),
                )
            } else {
                Text("Post", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Add pin sheet ─────────────────────────────────────────────────────────────

@Composable
private fun AddPinSheet(
    colors: AppColors,
    isAdding:  Boolean,
    onAdd:     (lat: Double, lng: Double, city: String, country: String, title: String, note: String, technique: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var lat       by remember { mutableStateOf<Double?>(null) }
    var lng       by remember { mutableStateOf<Double?>(null) }
    var city      by remember { mutableStateOf("") }
    var country   by remember { mutableStateOf("") }
    var title     by remember { mutableStateOf("") }
    var note      by remember { mutableStateOf("") }
    var technique by remember { mutableStateOf("other") }
    var gpsError  by remember { mutableStateOf("") }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION]   == true ||
                      grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val loc = getLastLocation(context)
            if (loc != null) { lat = loc.first; lng = loc.second; gpsError = "" }
            else gpsError = "Location unavailable"
        } else {
            gpsError = "Permission denied"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text       = "Add meditation spot",
            style      = MaterialTheme.typography.titleMedium,
            color      = colors.title,
            fontWeight = FontWeight.SemiBold,
        )

        // GPS row
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (lat != null && lng != null) {
                Icon(Icons.Default.LocationOn, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text     = "%.4f, %.4f".format(lat, lng),
                    style    = MaterialTheme.typography.labelMedium,
                    color    = colors.title,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Text(
                    text     = if (gpsError.isNotBlank()) gpsError else "No location set",
                    style    = MaterialTheme.typography.labelMedium,
                    color    = if (gpsError.isNotBlank()) Color(0xFFF87171) else colors.subtitle,
                    modifier = Modifier.weight(1f),
                )
            }
            TextButton(onClick = {
                val hasFine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (hasFine || hasCoarse) {
                    val loc = getLastLocation(context)
                    if (loc != null) { lat = loc.first; lng = loc.second; gpsError = "" }
                    else gpsError = "Location unavailable"
                } else {
                    locationLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ))
                }
            }) { Text("Use GPS", color = colors.primary) }
        }

        OutlinedTextField(
            value         = title,
            onValueChange = { title = it },
            label         = { Text("Title (optional)", color = colors.subtitle) },
            placeholder   = { Text("Meditation spot", color = colors.subtitle.copy(alpha = 0.5f)) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = outlineFieldColors(colors),
        )

        OutlinedTextField(
            value         = note,
            onValueChange = { if (it.length <= 300) note = it },
            label         = { Text("Note (optional)", color = colors.subtitle) },
            modifier      = Modifier.fillMaxWidth().height(96.dp),
            maxLines      = 4,
            colors        = outlineFieldColors(colors),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value         = city,
                onValueChange = { city = it },
                label         = { Text("City", color = colors.subtitle) },
                singleLine    = true,
                modifier      = Modifier.weight(1f),
                colors        = outlineFieldColors(colors),
            )
            OutlinedTextField(
                value         = country,
                onValueChange = { country = it },
                label         = { Text("Country", color = colors.subtitle) },
                singleLine    = true,
                modifier      = Modifier.weight(1f),
                colors        = outlineFieldColors(colors),
            )
        }

        Text("Technique", style = MaterialTheme.typography.labelMedium, color = colors.subtitle)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TECHNIQUE_LABELS.entries.toList()) { (key, label) ->
                val selected = technique == key
                val tc       = techColor(key)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            color = if (selected) tc else colors.surface,
                            shape = RoundedCornerShape(20.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) tc else colors.subtitle.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .clickable { technique = key }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text       = "${techEmoji(key)} $label",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = if (selected) Color.White else colors.subtitle,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick  = {
                onAdd(lat ?: 0.0, lng ?: 0.0, city, country, title, note, technique)
            },
            enabled  = (lat != null && lng != null) && !isAdding,
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = colors.primary,
                contentColor           = colors.onPrimary,
                disabledContainerColor = colors.primary.copy(alpha = 0.4f),
            ),
            shape = RoundedCornerShape(14.dp),
        ) {
            if (isAdding) {
                CircularProgressIndicator(
                    color       = colors.onPrimary,
                    strokeWidth = 2.dp,
                    modifier    = Modifier.size(18.dp),
                )
            } else {
                Text("Pin spot", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Internal helpers ──────────────────────────────────────────────────────────

@Composable
private fun outlineFieldColors(colors: AppColors) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = colors.primary,
    unfocusedBorderColor = colors.subtitle.copy(alpha = 0.3f),
    focusedTextColor     = colors.title,
    unfocusedTextColor   = colors.title,
    focusedLabelColor    = colors.primary,
    cursorColor          = colors.primary,
)

private fun getLastLocation(context: Context): Pair<Double, Double>? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
        try {
            @Suppress("MissingPermission")
            val loc = lm.getLastKnownLocation(provider)
            if (loc != null) return loc.latitude to loc.longitude
        } catch (_: Exception) {}
    }
    return null
}
