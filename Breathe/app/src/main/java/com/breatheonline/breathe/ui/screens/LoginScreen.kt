package com.breatheonline.breathe.ui.screens

import android.content.Context
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.components.AuthTextField
import com.breatheonline.breathe.ui.components.PasswordTextField
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.AuthUiState
import com.breatheonline.breathe.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    colors: AppColors,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val loginState by viewModel.loginState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError    by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(loginState) {
        if (loginState is AuthUiState.Success) {
            navController.navigate(Route.HOME) {
                popUpTo(Route.LOGIN) { inclusive = true }
            }
            viewModel.resetLoginState()
        }
    }

    val isLoading = loginState is AuthUiState.Loading
    val emailErrorMsg    = stringResource(R.string.login_email_error)
    val passwordErrorMsg = stringResource(R.string.login_password_error)

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
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .drawBehind {
                            val radius = size.width / 2 + 72.dp.toPx()
                            drawCircle(
                                brush = Brush.radialGradient(
                                    0f to colors.glowOuter,
                                    1f to Color.Transparent,
                                    center = Offset(size.width / 2, size.height / 2),
                                    radius = radius,
                                ),
                                radius = radius,
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displaySmall,
                            color = colors.title,
                        )
                        Text(
                            text  = stringResource(R.string.tagline),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.subtitle,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                AuthTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null; viewModel.resetLoginState() },
                    label = stringResource(R.string.login_email),
                    error = emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors = colors,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                PasswordTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null; viewModel.resetLoginState() },
                    label = stringResource(R.string.login_password),
                    error = passwordError,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            submitLogin(
                                email, password,
                                emailErr    = emailErrorMsg,
                                passwordErr = passwordErrorMsg,
                                onEmailError = { emailError = it },
                                onPasswordError = { passwordError = it },
                                onValid = { viewModel.login(email, password) },
                            )
                        }
                    ),
                    colors = colors,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        submitLogin(email, password,
                            emailErr        = emailErrorMsg,
                            passwordErr     = passwordErrorMsg,
                            onEmailError    = { emailError    = it },
                            onPasswordError = { passwordError = it },
                            onValid         = { viewModel.login(email, password) },
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
                            text  = stringResource(R.string.login_sign_in),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.onPrimary,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.subtitle.copy(alpha = 0.2f))
                    Text(
                        text = " ${stringResource(R.string.login_or_divider)} ",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.subtitle.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.subtitle.copy(alpha = 0.2f))
                }

                Spacer(Modifier.height(24.dp))

                GoogleSignInButton(
                    onClick = {
                        scope.launch {
                            performGoogleSignIn(context, viewModel)
                        }
                    },
                    enabled = !isLoading,
                    colors = colors
                )

                if (loginState is AuthUiState.Error) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text      = (loginState as AuthUiState.Error).message,
                        color     = MaterialTheme.colorScheme.error,
                        style     = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.weight(1f))

                // ── Language picker ───────────────────────────────────────────
                var selectedLang by remember {
                    val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                    mutableStateOf(when {
                        tags.contains("ru") -> "ru"
                        tags.contains("es") -> "es"
                        else -> "en"
                    })
                }
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    listOf(
                        "en" to R.string.language_english,
                        "ru" to R.string.language_russian,
                        "es" to R.string.language_spanish,
                    ).forEach { (code, labelRes) ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (selectedLang == code) colors.primary.copy(alpha = 0.12f) else Color.Transparent)
                                .border(1.dp, if (selectedLang == code) colors.primary else colors.subtitle.copy(alpha = 0.25f), RoundedCornerShape(50))
                                .clickable {
                                    selectedLang = code
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            val label = when (code) {
                                "en" -> "🇬🇧 ${stringResource(labelRes)}"
                                "ru" -> "🇷🇺 ${stringResource(labelRes)}"
                                else -> "🇪🇸 ${stringResource(labelRes)}"
                            }
                            Text(
                                text  = label,
                                color = if (selectedLang == code) colors.primary else colors.subtitle.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }

                TextButton(
                    onClick  = { navController.navigate(Route.REGISTER) },
                    modifier = Modifier.padding(bottom = 24.dp),
                ) {
                    Row {
                        Text(
                            text  = "${stringResource(R.string.login_no_account)} ",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.subtitle,
                        )
                        Text(
                            text  = stringResource(R.string.login_register_link),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean,
    colors: AppColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter           = painterResource(R.drawable.ic_google),
                contentDescription = null,
                tint              = Color.Unspecified,
                modifier          = Modifier.size(20.dp),
            )
            Text(
                text  = stringResource(R.string.login_continue_google),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = colors.title,
            )
        }
    }
}

private suspend fun performGoogleSignIn(context: Context, viewModel: AuthViewModel) {
    val credentialManager = CredentialManager.create(context)
    val webClientId = com.breatheonline.breathe.BuildConfig.GOOGLE_WEB_CLIENT_ID

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(context = context, request = request)
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        val idToken = googleIdTokenCredential.idToken
        viewModel.loginWithGoogle(idToken)
    } catch (e: GetCredentialException) {
        viewModel.setLoginError(e.message ?: "Google Sign-In failed. Please try again.")
    } catch (e: Exception) {
        viewModel.setLoginError("Unexpected error. Please try again.")
    }
}

private fun submitLogin(
    email: String,
    password: String,
    emailErr: String,
    passwordErr: String,
    onEmailError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit,
    onValid: () -> Unit,
) {
    var ok = true
    if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError(emailErr)
        ok = false
    }
    if (password.length < 6) {
        onPasswordError(passwordErr)
        ok = false
    }
    if (ok) onValid()
}
