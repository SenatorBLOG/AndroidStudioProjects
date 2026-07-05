package com.breatheonline.breathe.ui.screens

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors
import java.net.URLDecoder

private const val ALLOWED_HOST = "breatheonline.app"

private fun isAllowedUrl(url: String): Boolean {
    return try {
        val host = Uri.parse(url).host ?: return false
        host == ALLOWED_HOST || host.endsWith(".$ALLOWED_HOST")
    } catch (_: Exception) { false }
}

@Composable
fun ArticleScreen(
    navController: NavController,
    encodedUrl:    String,
    colors: AppColors,
) {
    val rawUrl = try { URLDecoder.decode(encodedUrl, "UTF-8") } catch (_: Exception) { encodedUrl }
    // Only load URLs from the allowed domain; fall back to a blank page if untrusted
    val url = if (isAllowedUrl(rawUrl)) rawUrl else "about:blank"

    var isLoading  by remember { mutableStateOf(true) }
    var hasError   by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { navController.popBackStack() },
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint               = colors.title,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text  = stringResource(R.string.article_breatheonline),
                style = MaterialTheme.typography.labelSmall,
                color = colors.subtitle,
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, pageUrl: String, favicon: android.graphics.Bitmap?) {
                                isLoading = true
                                hasError  = false
                            }
                            override fun onPageFinished(view: WebView, pageUrl: String) {
                                isLoading = false
                            }
                            @Suppress("OVERRIDE_DEPRECATION")
                            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                                isLoading = false
                                hasError  = true
                            }
                            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                                if (request.isForMainFrame) {
                                    isLoading = false
                                    hasError  = true
                                }
                            }
                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest,
                            ): Boolean {
                                val target = request.url.toString()
                                return if (isAllowedUrl(target)) {
                                    false
                                } else {
                                    try {
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                                    } catch (_: Exception) {}
                                    true
                                }
                            }
                        }
                        with(settings) {
                            javaScriptEnabled        = true   // required for breatheonline.app content
                            domStorageEnabled        = true
                            allowFileAccess          = false
                            allowContentAccess       = false
                            @Suppress("DEPRECATION")
                            allowFileAccessFromFileURLs = false
                            @Suppress("DEPRECATION")
                            allowUniversalAccessFromFileURLs = false
                        }
                        webViewRef = this
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            // Loading indicator
            if (isLoading && !hasError) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = colors.primary,
                )
            }

            // Error state
            if (hasError) {
                Column(
                    modifier            = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text       = stringResource(R.string.article_could_not_load),
                        color      = colors.title,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = stringResource(R.string.article_check_internet),
                        color     = colors.subtitle,
                        style     = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            hasError  = false
                            isLoading = true
                            webViewRef?.loadUrl(url)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    ) {
                        Text(stringResource(R.string.article_retry), color = colors.onPrimary)
                    }
                }
            }
        }
    }
}
