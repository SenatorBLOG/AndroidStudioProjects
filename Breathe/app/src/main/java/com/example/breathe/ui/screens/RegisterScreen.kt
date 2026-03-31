package com.example.breathe.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.breathe.ui.components.AuthTextField
import com.example.breathe.ui.components.PasswordTextField
import com.example.breathe.ui.theme.AppColors
import com.example.breathe.viewmodel.AuthUiState
import com.example.breathe.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    colors: AppColors,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val registerState by viewModel.registerState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError    by remember { mutableStateOf<String?>(null) }
    var emailError   by remember { mutableStateOf<String?>(null) }
    var passError    by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(registerState) {
        if (registerState is AuthUiState.Success) {
            navController.navigate(Route.HOME) {
                popUpTo(Route.LOGIN) { inclusive = true }
            }
            viewModel.resetRegisterState()
        }
    }

    val isLoading = registerState is AuthUiState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(500)) + slideInVertically(
                animationSpec  = tween(500),
                initialOffsetY = { it / 14 },
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ── Top bar ───────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = colors.primary,
                        )
                    }
                    Text(
                        text     = "Create Account",
                        style    = MaterialTheme.typography.headlineSmall,
                        color    = colors.title,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }

                // ── Subtitle ──────────────────────────────────────────────────
                Text(
                    text      = "Start your mindfulness journey",
                    style     = MaterialTheme.typography.titleMedium,
                    color     = colors.subtitle,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                )

                // ── Name ──────────────────────────────────────────────────────
                AuthTextField(
                    value         = name,
                    onValueChange = { name = it; nameError = null },
                    label         = "Full Name",
                    error         = nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction      = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors   = colors,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                // ── Email ─────────────────────────────────────────────────────
                AuthTextField(
                    value         = email,
                    onValueChange = { email = it; emailError = null },
                    label         = "Email",
                    error         = emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors   = colors,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                // ── Password ──────────────────────────────────────────────────
                PasswordTextField(
                    value         = password,
                    onValueChange = { password = it; passError = null },
                    label         = "Password",
                    error         = passError,
                    imeAction     = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors   = colors,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                // ── Confirm password ──────────────────────────────────────────
                PasswordTextField(
                    value         = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmError = null },
                    label         = "Confirm Password",
                    error         = confirmError,
                    imeAction     = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            submitRegister(name, email, password, confirmPassword,
                                onNameError    = { nameError    = it },
                                onEmailError   = { emailError   = it },
                                onPassError    = { passError    = it },
                                onConfirmError = { confirmError = it },
                                onValid        = { viewModel.register(name, email, password) },
                            )
                        }
                    ),
                    colors   = colors,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(6.dp))

                // ── API / server error ────────────────────────────────────────
                if (registerState is AuthUiState.Error) {
                    Text(
                        text      = (registerState as AuthUiState.Error).message,
                        color     = MaterialTheme.colorScheme.error,
                        style     = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    )
                } else {
                    Spacer(Modifier.height(20.dp))
                }

                // ── Register button ───────────────────────────────────────────
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        submitRegister(name, email, password, confirmPassword,
                            onNameError    = { nameError    = it },
                            onEmailError   = { emailError   = it },
                            onPassError    = { passError    = it },
                            onConfirmError = { confirmError = it },
                            onValid        = { viewModel.register(name, email, password) },
                        )
                    },
                    enabled  = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = colors.primary,
                        disabledContainerColor = colors.primary.copy(alpha = 0.40f),
                    ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color       = colors.onPrimary,
                        )
                    } else {
                        Text(
                            text  = "Create Account",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onPrimary,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Sign-in link ──────────────────────────────────────────────
                TextButton(
                    onClick  = { navController.popBackStack() },
                    modifier = Modifier.padding(bottom = 24.dp),
                ) {
                    Row {
                        Text(
                            text  = "Already have an account? ",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.subtitle,
                        )
                        Text(
                            text  = "Sign In",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary,
                        )
                    }
                }
            }
        }
    }
}

// ── Validation helper ─────────────────────────────────────────────────────────

private fun submitRegister(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    onNameError: (String?) -> Unit,
    onEmailError: (String?) -> Unit,
    onPassError: (String?) -> Unit,
    onConfirmError: (String?) -> Unit,
    onValid: () -> Unit,
) {
    var ok = true
    if (name.isBlank()) {
        onNameError("Name cannot be empty")
        ok = false
    }
    if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError("Enter a valid email address")
        ok = false
    }
    if (password.length < 6) {
        onPassError("Password must be at least 6 characters")
        ok = false
    }
    if (confirmPassword != password) {
        onConfirmError("Passwords do not match")
        ok = false
    }
    if (ok) onValid()
}
