package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sathsara.ecbtracker.BuildConfig
import com.sathsara.ecbtracker.ui.components.BrandHeader
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.SecondaryOutlineButton
import com.sathsara.ecbtracker.ui.components.SectionCard
import com.sathsara.ecbtracker.ui.components.SectionHeading
import com.sathsara.ecbtracker.ui.components.StatusBanner
import com.sathsara.ecbtracker.ui.theme.CyanPrimary
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onAuthSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isCreateMode by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            BrandHeader(
                title = "ECB Tracker",
                subtitle = "Electricity monitoring made practical"
            )

            SectionCard {
                SectionHeading(
                    title = if (isCreateMode) "Create your account" else "Welcome back",
                    subtitle = if (isCreateMode) {
                        "Set up your tracker account so you can save readings and personalize the dashboard."
                    } else {
                        "Sign in to log readings, review history, and manage your account."
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryOutlineButton(
                        text = "Sign In",
                        onClick = {
                            isCreateMode = false
                            viewModel.clearFeedback()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    )
                    SecondaryOutlineButton(
                        text = "Create",
                        onClick = {
                            isCreateMode = true
                            viewModel.clearFeedback()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    )
                }

                if (uiState.error != null) {
                    StatusBanner(message = uiState.error!!, isError = true)
                }

                if (uiState.infoMessage != null) {
                    StatusBanner(message = uiState.infoMessage!!, isError = false)
                }

                AuthField(
                    label = "Email address",
                    value = email,
                    onValueChange = {
                        email = it.trim()
                        viewModel.clearFeedback()
                    },
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email
                )

                AuthField(
                    label = "Password",
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearFeedback()
                    },
                    placeholder = if (isCreateMode) "Use at least 6 characters" else "Enter your password",
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                Text(
                    text = if (isCreateMode) {
                        "After signing up, you may need to confirm your email before the app lets you in."
                    } else {
                        "If the buttons stay disabled, check that both fields are filled in."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )

                PrimaryButton(
                    text = when {
                        uiState.isLoading -> "Please wait..."
                        isCreateMode -> "Create Account"
                        else -> "Sign In"
                    },
                    onClick = {
                        if (isCreateMode) {
                            viewModel.signUp(email, password)
                        } else {
                            viewModel.signIn(email, password)
                        }
                    },
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                )

                if (!isCreateMode) {
                    SecondaryOutlineButton(
                        text = "Forgot password?",
                        onClick = { viewModel.sendPasswordReset(email) },
                        enabled = !uiState.isLoading
                    )
                }
            }

            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = CyanPrimary
            )
        }
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
