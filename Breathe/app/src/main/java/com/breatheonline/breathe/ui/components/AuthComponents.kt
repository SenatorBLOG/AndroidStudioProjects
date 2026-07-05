package com.breatheonline.breathe.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors

/**
 * Styled OutlinedTextField for auth screens.
 * Focuses on readability on dark backgrounds using the current [AppColors].
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: AppColors,
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onValueChange,
        label          = { Text(label) },
        singleLine     = true,
        isError        = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions  = keyboardOptions,
        keyboardActions  = keyboardActions,
        textStyle      = MaterialTheme.typography.bodyMedium,
        colors         = authFieldColors(colors),
        modifier       = modifier,
    )
}

/**
 * OutlinedTextField with integrated show/hide toggle for password fields.
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: AppColors,
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value               = value,
        onValueChange       = onValueChange,
        label               = { Text(label) },
        singleLine          = true,
        isError             = error != null,
        supportingText      = error?.let { { Text(it) } },
        visualTransformation = if (visible) VisualTransformation.None
                               else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector  = if (visible) Icons.Filled.Visibility
                                   else Icons.Filled.VisibilityOff,
                    contentDescription = if (visible) stringResource(R.string.cd_hide_password)
                                       else stringResource(R.string.cd_show_password),
                    tint         = colors.subtitle,
                )
            }
        },
        keyboardOptions  = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction    = imeAction,
        ),
        keyboardActions  = keyboardActions,
        textStyle        = MaterialTheme.typography.bodyMedium,
        colors           = authFieldColors(colors),
        modifier         = modifier,
    )
}

// ── Shimmer ───────────────────────────────────────────────────────────────────

/**
 * Single shimmer rectangle — use as a placeholder for any loading UI.
 * Width/height controlled via [modifier].
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue  = -600f,
        targetValue   =  600f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label         = "shimmerX",
    )
    val brush = Brush.linearGradient(
        colors      = listOf(
            Color.White.copy(alpha = 0.04f),
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.04f),
        ),
        start = Offset(x - 300f, 0f),
        end   = Offset(x + 300f, 0f),
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush),
    )
}

/**
 * Shimmer skeleton that mimics a list of stat/journal cards.
 * Pass [rows] to control how many placeholder rows appear.
 */
@Composable
fun ShimmerCardList(
    rows: Int = 4,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(horizontal = 20.dp),
    ) {
        repeat(rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Icon placeholder
                ShimmerBox(
                    modifier     = Modifier.width(40.dp).height(40.dp),
                    cornerRadius = 12.dp,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    // Title line
                    ShimmerBox(
                        modifier     = Modifier.fillMaxWidth(0.55f).height(14.dp),
                        cornerRadius = 6.dp,
                    )
                    // Subtitle line
                    ShimmerBox(
                        modifier     = Modifier.fillMaxWidth(0.80f).height(10.dp),
                        cornerRadius = 6.dp,
                    )
                }
            }
        }
    }
}

/**
 * Shimmer skeleton for full-screen stat blocks (header + body).
 */
@Composable
fun ShimmerStatScreen(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(horizontal = 20.dp),
    ) {
        // Chart placeholder
        ShimmerBox(
            modifier     = Modifier.fillMaxWidth().height(180.dp),
            cornerRadius = 20.dp,
        )
        Spacer(Modifier.height(4.dp))
        // Stat tiles row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerBox(modifier = Modifier.weight(1f).height(90.dp), cornerRadius = 16.dp)
            ShimmerBox(modifier = Modifier.weight(1f).height(90.dp), cornerRadius = 16.dp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerBox(modifier = Modifier.weight(1f).height(90.dp), cornerRadius = 16.dp)
            ShimmerBox(modifier = Modifier.weight(1f).height(90.dp), cornerRadius = 16.dp)
        }
        // List items
        repeat(2) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(64.dp), cornerRadius = 16.dp)
        }
    }
}

/** Shared field colour scheme so all auth inputs look identical. */
@Composable
fun authFieldColors(colors: AppColors) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor        = colors.primary,
    unfocusedBorderColor      = colors.subtitle.copy(alpha = 0.35f),
    cursorColor               = colors.primary,
    focusedLabelColor         = colors.primary,
    unfocusedLabelColor       = colors.subtitle,
    focusedTextColor          = colors.title,
    unfocusedTextColor        = colors.title,
    errorBorderColor          = MaterialTheme.colorScheme.error,
    errorLabelColor           = MaterialTheme.colorScheme.error,
    errorSupportingTextColor  = MaterialTheme.colorScheme.error,
    errorCursorColor          = MaterialTheme.colorScheme.error,
)
