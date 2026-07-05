package com.breatheonline.breathe.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.data.models.ChallengeDto
import com.breatheonline.breathe.data.models.ChallengeRecommendationDto
import com.breatheonline.breathe.data.models.UserChallengeDto
import androidx.compose.ui.res.stringResource
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.icons.LucideAppIcons
import com.breatheonline.breathe.ui.theme.AppColors

@Composable
internal fun ChallengesTab(
    colors: AppColors,
    availableChallenges: List<ChallengeDto>,
    myChallenges: List<UserChallengeDto>,
    recommendation: ChallengeRecommendationDto?,
    onJoin: (slug: String) -> Unit,
    onCheckIn: (id: String) -> Unit,
    onAbandon: (id: String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (recommendation?.challenge != null) {
            SectionCard(stringResource(R.string.challenges_ai_recommended), colors) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(LucideAppIcons.Bot, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Text(recommendation.challenge.name, fontWeight = FontWeight.Bold, color = colors.primary)
                }
                Text(recommendation.reason.orEmpty(), color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
                Button(onClick = { onJoin(recommendation.challenge.slug) }, modifier = Modifier.padding(top = 10.dp)) {
                    Text(stringResource(R.string.challenges_join_now))
                }
            }
        }

        if (myChallenges.isNotEmpty()) {
            SectionCard(stringResource(R.string.challenges_my_active), colors) {
                myChallenges.forEach { uc ->
                    val challenge = uc.challenge
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = challengeIcon(challenge?.icon, challenge?.slug, challenge?.name),
                            contentDescription = challenge?.name,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(challenge?.name ?: "", fontWeight = FontWeight.Bold, color = colors.title)
                            Text(
                                stringResource(R.string.challenges_days_progress, uc.completedDays.size, challenge?.duration?.toString() ?: "?"),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.subtitle,
                            )
                        }
                        IconButton(onClick = { onCheckIn(uc.id) }) {
                            Icon(Icons.Default.CheckCircle, null, tint = colors.primary)
                        }
                    }
                }
            }
        }

        SectionCard(stringResource(R.string.challenges_available), colors) {
            availableChallenges.forEach { ch ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = challengeIcon(ch.icon, ch.slug, ch.name),
                        contentDescription = ch.name,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(ch.name, Modifier.weight(1f), color = colors.title)
                    Button(
                        onClick = { onJoin(ch.slug) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary.copy(alpha = 0.10f),
                            contentColor = colors.primary,
                        ),
                    ) {
                        Text(stringResource(R.string.challenges_join), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

private fun challengeIcon(icon: String?, slug: String?, name: String?): ImageVector {
    val key = listOfNotNull(icon, slug, name).joinToString(" ").lowercase()
    return when {
        "sleep" in key || "moon" in key || "night" in key -> LucideAppIcons.MoonStar
        "focus" in key || "target" in key -> LucideAppIcons.Target
        "energy" in key || "zap" in key -> LucideAppIcons.Zap
        "calm" in key || "stress" in key || "wave" in key -> LucideAppIcons.Waves
        "breath" in key || "wind" in key || "air" in key -> LucideAppIcons.Wind
        "heart" in key || "health" in key -> LucideAppIcons.HeartPulse
        "leaf" in key || "nature" in key -> LucideAppIcons.Leaf
        "trophy" in key || "award" in key || "challenge" in key -> LucideAppIcons.Trophy
        else -> LucideAppIcons.Wind
    }
}
