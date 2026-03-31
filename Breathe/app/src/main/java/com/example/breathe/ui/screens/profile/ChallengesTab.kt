package com.example.breathe.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.breathe.ui.theme.AppColors
import com.example.breathe.viewmodel.ChallengesViewModel

@Composable
internal fun ChallengesTab(colors: AppColors) {
    val vm        = hiltViewModel<ChallengesViewModel>()
    val available = vm.availableChallenges
    val myActive  = vm.myChallenges
    val rec       = vm.recommendation

    Column(
        modifier            = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // AI recommendation
        if (rec?.challenge != null) {
            SectionCard("🤖 AI Recommended", colors) {
                Text(rec.challenge.name, fontWeight = FontWeight.Bold, color = colors.primary)
                Text(rec.reason.orEmpty(), color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
                Button(onClick = { vm.joinChallenge(rec.challenge.slug) }, modifier = Modifier.padding(top = 10.dp)) {
                    Text("Join Now")
                }
            }
        }

        // My active challenges
        if (myActive.isNotEmpty()) {
            SectionCard("✅ My Active Challenges", colors) {
                myActive.forEach { uc ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(uc.challenge?.icon ?: "🧘", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(uc.challenge?.name ?: "",                                                            fontWeight = FontWeight.Bold, color = colors.title)
                            Text("${uc.completedDays.size}/${uc.challenge?.duration ?: "?"} days", style = MaterialTheme.typography.labelSmall,   color = colors.subtitle)
                        }
                        IconButton(onClick = { vm.checkIn(uc.id) }) {
                            Icon(Icons.Default.CheckCircle, null, tint = colors.primary)
                        }
                    }
                }
            }
        }

        // Available to join
        SectionCard("🏆 Available Challenges", colors) {
            available.forEach { ch ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(ch.icon ?: "🧘", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(ch.name, Modifier.weight(1f), color = colors.title)
                    Button(
                        onClick = { vm.joinChallenge(ch.slug) },
                        shape   = RoundedCornerShape(8.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = colors.primary.copy(alpha = 0.10f),
                            contentColor   = colors.primary,
                        ),
                    ) {
                        Text("Join", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
