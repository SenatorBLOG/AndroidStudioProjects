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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.breatheonline.breathe.ui.theme.AppColors

private const val PRIVACY_LAST_UPDATED = "March 23, 2026"
private const val PRIVACY_EMAIL = "privacy@breatheapp.co"

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.title)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────────────
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
                Text("Privacy Policy", style = MaterialTheme.typography.headlineLarge, color = colors.title, fontWeight = FontWeight.Bold)
                Text("Last updated: $PRIVACY_LAST_UPDATED", style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
                Text(
                    "Breathe is built on a simple principle: your health data belongs to you. This policy explains exactly what we collect, why, and how you can control it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.subtitle,
                )
            }

            // ── Section 1: Information We Collect ─────────────────────────────────
            PolicySection(colors = colors, icon = Icons.Default.Storage, title = "Information We Collect") {
                PText(colors, "We collect only what is necessary to provide Breathe's features:")
                BulletItem(colors) { BoldRest(colors, "Account data", " — email address and display name, provided voluntarily when you sign up. Google OAuth provides only your name and email.") }
                BulletItem(colors) { BoldRest(colors, "Session data", " — breathing technique used, session duration, phase timings, and optional notes you write.") }
                BulletItem(colors) { BoldRest(colors, "Biometric data (opt-in)", " — heart rate readings from your device, collected only with your explicit consent via the Data Consent screen.") }
                BulletItem(colors) { BoldRest(colors, "Globe pins", " — latitude, longitude, and optional public message if you choose to drop a pin on the community globe.") }
                BulletItem(colors) { BoldRest(colors, "Usage data", " — anonymous analytics such as page views and feature interactions. No personally identifiable information is attached.") }
                PText(colors, "You can use all core breathing features without creating an account. Account creation is optional.")
            }

            // ── Section 2: How We Use Your Data ───────────────────────────────────
            PolicySection(colors = colors, icon = Icons.Default.Visibility, title = "How We Use Your Data") {
                PText(colors, "Your data is used solely to operate and improve Breathe:")
                BulletItem(colors) { PText(colors, "Saving and displaying your session history and streaks.") }
                BulletItem(colors) { PText(colors, "Personalising your breathing statistics and progress charts.") }
                BulletItem(colors) { PText(colors, "Powering the community globe (only pins you explicitly create are shown publicly).") }
                BulletItem(colors) { PText(colors, "Sending optional push notifications for streak reminders (only if you enable them).") }
                BulletItem(colors) { PText(colors, "Improving app performance and fixing bugs using anonymised usage analytics.") }
                BoldRest(colors, "We never", " sell your data, use it for advertising, or share it with third parties for their own purposes.")
            }

            // ── Section 3: Data Storage & Security ────────────────────────────────
            PolicySection(colors = colors, icon = Icons.Default.Lock, title = "Data Storage & Security") {
                BulletItem(colors) { PText(colors, "All data is encrypted in transit via HTTPS/TLS and encrypted at rest in our database.") }
                BulletItem(colors) { PText(colors, "Passwords are hashed using bcrypt. We never store plaintext passwords.") }
                BulletItem(colors) { PText(colors, "Biometric (heart rate) data is stored separately and access-controlled so only you can read it.") }
                BulletItem(colors) { PText(colors, "We use industry-standard practices to protect against unauthorised access, alteration, and deletion.") }
                PText(colors, "Despite our best efforts, no system is 100% secure. If you discover a security issue, please contact us at $PRIVACY_EMAIL.")
            }

            // ── Section 4: Third-Party Services ───────────────────────────────────
            PolicySection(colors = colors, icon = Icons.Default.Language, title = "Third-Party Services") {
                PText(colors, "Breathe uses a small number of third-party services:")
                BulletItem(colors) { BoldRest(colors, "Google OAuth", " — for social sign-in. Google receives only the authentication request. We store only your name and email.") }
                BulletItem(colors) { BoldRest(colors, "MongoDB Atlas", " — our database host. Data is stored in encrypted clusters with strict access controls.") }
                BulletItem(colors) { BoldRest(colors, "Web Speech API", " — voice guidance runs entirely in your browser. No audio is sent to any server.") }
                PText(colors, "No data is shared with advertising networks, data brokers, or social media platforms.")
            }

            // ── Section 5: Your Rights ─────────────────────────────────────────────
            PolicySection(colors = colors, icon = Icons.Default.HowToReg, title = "Your Rights") {
                PText(colors, "You have full control over your data:")
                BulletItem(colors) { BoldRest(colors, "Access", " — view all your session history and stats from your Profile page at any time.") }
                BulletItem(colors) { BoldRest(colors, "Delete sessions", " — remove individual sessions from your session history.") }
                BulletItem(colors) { BoldRest(colors, "Delete biometric data", " — revoke heart-rate consent and erase all stored readings from Profile → Data & Privacy.") }
                BulletItem(colors) { BoldRest(colors, "Delete account", " — permanently erase your account and all associated data. Contact us at the email below.") }
                BulletItem(colors) { BoldRest(colors, "Export", " — request a copy of your data at any time by emailing us.") }
                PText(colors, "If you are in the EU/EEA, you also have rights under GDPR including the right to object to processing and to lodge a complaint with your supervisory authority.")
            }

            // ── Section 6: Data Retention ─────────────────────────────────────────
            PolicySection(colors = colors, icon = Icons.Default.Delete, title = "Data Retention") {
                BulletItem(colors) { PText(colors, "Session data is retained for as long as your account is active or until you delete it.") }
                BulletItem(colors) { PText(colors, "If you delete your account, all personal data is permanently removed within 30 days.") }
                BulletItem(colors) { PText(colors, "Anonymised, aggregated analytics data (no personal identifiers) may be retained indefinitely to improve the service.") }
                BulletItem(colors) { PText(colors, "Globe pins you have posted are removed immediately when you delete them or your account.") }
            }

            // ── Section 7: Children's Privacy ─────────────────────────────────────
            PolicySection(colors = colors, icon = Icons.Default.Security, title = "Children's Privacy") {
                PText(colors, "Breathe is not directed at children under 13. We do not knowingly collect personal information from children under 13. If you believe a child has provided us personal data, please contact us and we will delete it promptly.")
            }

            // ── Section 8: Contact Us ──────────────────────────────────────────────
            PolicySection(colors = colors, icon = Icons.Default.Email, title = "Contact Us") {
                PText(colors, "For privacy questions, data requests, or to report a concern:")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Email: ", style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
                    Text(
                        PRIVACY_EMAIL,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = colors.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { uriHandler.openUri("mailto:$PRIVACY_EMAIL") },
                    )
                }
                PText(colors, "We aim to respond to all privacy-related requests within 5 business days.")
            }

            // ── Footer note ───────────────────────────────────────────────────────
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
                    Text("Questions? Email ", style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
                    Text(
                        PRIVACY_EMAIL,
                        style      = MaterialTheme.typography.bodySmall,
                        color      = colors.primary,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.clickable { uriHandler.openUri("mailto:$PRIVACY_EMAIL") },
                    )
                    Text(".", style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

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
