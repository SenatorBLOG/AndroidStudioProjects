package com.breatheonline.breathe.ui.components

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
                    contentDescription = if (visible) "Hide password" else "Show password",
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
