package com.example.breathe.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breathe.ui.theme.AppColors
import com.example.breathe.viewmodel.ProfileState

@Composable
internal fun DevicesTab(colors: AppColors, state: ProfileState, onSync: () -> Unit) {
    Column(
        modifier            = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard("Wearables", colors) {
            if (state.integrations.isEmpty()) {
                Text(
                    "No devices linked. Link Fitbit or Google Fit to see health data.",
                    color = colors.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                state.integrations.forEach { dev ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (dev.provider == "fitbit") "💚" else "🔵", fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(dev.provider.uppercase(), fontWeight = FontWeight.Bold, color = colors.title)
                        }
                        Text(
                            text  = if (dev.connected) "Linked" else "Not Linked",
                            color = if (dev.connected) colors.primary else colors.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onSync, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth()) {
                if (state.isSyncing) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                else                 Text("Sync All Devices")
            }
        }
    }
}
