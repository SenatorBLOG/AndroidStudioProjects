package com.breatheonline.breathe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors

// Section content lives in PrivacyPolicyContent.kt

@Composable
fun PrivacyPolicyScreen(colors: AppColors, navController: NavController) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = colors.title)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.primary.copy(alpha = 0.13f))
                        .border(1.dp, colors.primary.copy(alpha = 0.27f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
                }
                Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.headlineLarge, color = colors.title, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.privacy_last_updated, PRIVACY_LAST_UPDATED), style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
                Text(
                    "Breathe is built on a simple principle: your health data belongs to you. This policy explains exactly what we collect, why, and how you can control it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.subtitle,
                )
            }

            // ── Sections (driven by PRIVACY_SECTIONS data) ────────────────────
            PRIVACY_SECTIONS.forEach { section ->
                PolicySection(colors = colors, icon = section.icon.toImageVector(), title = section.title) {
                    section.items.forEach { item -> RenderPolicyItem(colors, item) }

                    // Special case: Contact Us section gets the email row
                    if (section.icon == PolicyIcon.EMAIL) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.privacy_email_label), style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
                            Text(
                                PRIVACY_CONTACT_EMAIL,
                                style      = MaterialTheme.typography.bodySmall,
                                color      = colors.primary,
                                fontWeight = FontWeight.Medium,
                                modifier   = Modifier.clickable { uriHandler.openUri("mailto:$PRIVACY_CONTACT_EMAIL") },
                            )
                        }
                    }
                }
            }

            // ── Footer note ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary.copy(alpha = 0.06f))
                    .border(1.dp, colors.primary.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PText(colors, "This policy may be updated from time to time. We will notify you of significant changes via the app or email. By continuing to use Breathe after changes take effect, you accept the revised policy.")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.privacy_questions_email), style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
                    Text(
                        PRIVACY_CONTACT_EMAIL,
                        style      = MaterialTheme.typography.bodySmall,
                        color      = colors.primary,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.clickable { uriHandler.openUri("mailto:$PRIVACY_CONTACT_EMAIL") },
                    )
                    Text(".", style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

// ── Render helpers ────────────────────────────────────────────────────────────

@Composable
private fun RenderPolicyItem(colors: AppColors, item: PolicyItem) {
    when (item) {
        is PolicyItem.Plain      -> PText(colors, item.text)
        is PolicyItem.BoldPrefix -> BoldRest(colors, item.bold, item.rest)
        is PolicyItem.Bullet     -> BulletItem(colors) { RenderPolicyItem(colors, item.inner) }
    }
}

private fun PolicyIcon.toImageVector(): ImageVector = when (this) {
    PolicyIcon.STORAGE    -> Icons.Default.Storage
    PolicyIcon.VISIBILITY -> Icons.Default.Visibility
    PolicyIcon.LOCK       -> Icons.Default.Lock
    PolicyIcon.LANGUAGE   -> Icons.Default.Language
    PolicyIcon.HOW_TO_REG -> Icons.Default.HowToReg
    PolicyIcon.DELETE     -> Icons.Default.Delete
    PolicyIcon.SECURITY   -> Icons.Default.Security
    PolicyIcon.EMAIL      -> Icons.Default.Email
}

// ── Composable building blocks ────────────────────────────────────────────────

@Composable
private fun PolicySection(
    colors: AppColors,
    icon:    ImageVector,
    title:   String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.primary.copy(alpha = 0.13f))
                    .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
            }
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.title, fontWeight = FontWeight.SemiBold)
        }
        content()
    }
}

@Composable
private fun PText(colors: AppColors, text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
}

@Composable
private fun BoldRest(colors: AppColors, bold: String, rest: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.title)) { append(bold) }
            withStyle(SpanStyle(color = colors.subtitle)) { append(rest) }
        },
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun BulletItem(colors: AppColors, content: @Composable () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Spacer(Modifier.width(4.dp))
        Box(
            Modifier
                .padding(top = 5.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(colors.primary),
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) { content() }
    }
}
