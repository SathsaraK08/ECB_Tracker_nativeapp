package com.sathsara.ecbtracker.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sathsara.ecbtracker.BuildConfig
import com.sathsara.ecbtracker.logic.TrackerDateTimeParser
import com.sathsara.ecbtracker.ui.components.BrandHeader
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.SectionCard
import com.sathsara.ecbtracker.ui.components.SectionHeading
import com.sathsara.ecbtracker.ui.components.SecondaryOutlineButton
import com.sathsara.ecbtracker.ui.components.StatusBanner
import com.sathsara.ecbtracker.ui.theme.CyanDim
import com.sathsara.ecbtracker.ui.theme.CyanPrimary
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val billReminders by viewModel.billReminders.collectAsStateWithLifecycle()
    val usageAlerts by viewModel.usageAlerts.collectAsStateWithLifecycle()
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    var displayName by remember(uiState.profile?.username) { mutableStateOf(uiState.profile?.username.orEmpty()) }
    var accountNumber by remember(uiState.settings?.accountNumber) { mutableStateOf(uiState.settings?.accountNumber.orEmpty()) }
    var ratePerUnit by remember(uiState.settings?.lkrPerUnit) {
        mutableStateOf((uiState.settings?.lkrPerUnit?.takeIf { it > 0.0 } ?: 32.0).toString())
    }
    var currencyCode by remember(uiState.currencyCode) { mutableStateOf(uiState.currencyCode) }
    var geminiApiKey by remember(uiState.geminiApiKey) { mutableStateOf(uiState.geminiApiKey) }
    var reminderTime by remember(uiState.reminderTime) { mutableStateOf(uiState.reminderTime) }

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onLogout()
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BrandHeader(
                title = "Settings",
                subtitle = uiState.email.ifBlank { "Manage your account and tracker preferences" }
            )

            if (uiState.error != null) {
                StatusBanner(message = uiState.error!!, isError = true)
            }

            if (uiState.saveMessage != null) {
                StatusBanner(message = uiState.saveMessage!!, isError = false)
            }

            SectionCard {
                SectionHeading(
                    title = "Account details",
                    subtitle = "These values personalize the dashboard and saved billing estimates."
                )

                SettingsField(
                    label = "Display name",
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        viewModel.clearMessages()
                    }
                )

                SettingsField(
                    label = "CEB account number",
                    value = accountNumber,
                    onValueChange = {
                        accountNumber = it
                        viewModel.clearMessages()
                    }
                )

                SettingsField(
                    label = "Electricity rate (per unit)",
                    value = ratePerUnit,
                    onValueChange = {
                        ratePerUnit = it
                        viewModel.clearMessages()
                    }
                )
            }

            SectionCard {
                SectionHeading(
                    title = "Billing and forecast",
                    subtitle = "Control the unit price, currency label, and optional Gemini enhancement."
                )

                SettingsField(
                    label = "Currency code",
                    value = currencyCode,
                    onValueChange = {
                        currencyCode = it.uppercase()
                        viewModel.clearMessages()
                    }
                )

                SettingsField(
                    label = "Gemini API key (optional)",
                    value = geminiApiKey,
                    onValueChange = {
                        geminiApiKey = it
                        viewModel.clearMessages()
                    }
                )

                Text(
                    text = "If the Gemini key is empty, the app falls back to local usage-pattern forecasting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }

            SectionCard {
                SectionHeading(
                    title = "Preferences",
                    subtitle = "Choose how the tracker behaves on your device."
                )

                PreferenceRow(
                    title = "Dark mode",
                    subtitle = "Use the darker theme across the app.",
                    checked = isDarkMode,
                    onCheckedChange = viewModel::toggleDarkMode
                )
                PreferenceRow(
                    title = "Bill reminders",
                    subtitle = "Show a daily reminder so one of you can log the latest reading.",
                    checked = billReminders,
                    onCheckedChange = { enabled ->
                        if (
                            enabled &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            notificationPermissionState?.status?.isGranted == false
                        ) {
                            notificationPermissionState?.launchPermissionRequest()
                        }
                        viewModel.toggleBillReminders(enabled)
                    }
                )
                PreferenceRow(
                    title = "High usage alerts",
                    subtitle = "Warn when your logged usage starts climbing.",
                    checked = usageAlerts,
                    onCheckedChange = viewModel::toggleUsageAlerts
                )

                SettingsField(
                    label = "Daily reminder time (HH:mm)",
                    value = reminderTime,
                    onValueChange = {
                        reminderTime = TrackerDateTimeParser.sanitizeTime(it)
                        viewModel.clearMessages()
                    }
                )

                Text(
                    text = "Example: 20:00 for an end-of-day reminder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }

            SectionCard {
                SectionHeading(
                    title = "Security",
                    subtitle = "Account recovery is handled through Supabase email recovery."
                )
                SecondaryOutlineButton(
                    text = "Send password reset email",
                    onClick = viewModel::sendPasswordReset
                )
            }

            PrimaryButton(
                text = "Save changes",
                onClick = {
                    viewModel.saveAccountSettings(
                        displayName = displayName,
                        accountNumber = accountNumber,
                        rateText = ratePerUnit,
                        currencyCode = currencyCode,
                        geminiApiKey = geminiApiKey,
                        reminderTime = reminderTime
                    )
                },
                enabled = !uiState.isLoading
            )

            SectionCard {
                SectionHeading(
                    title = "About this build",
                    subtitle = "Built for shared daily electricity tracking and month-end bill prediction."
                )
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Developed by Sathsara Karunarathne",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
            }

            SecondaryOutlineButton(
                text = "Sign out",
                onClick = viewModel::signOut
            )
        }
    }
}

@Composable
private fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun PreferenceRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanPrimary,
                checkedTrackColor = CyanDim
            )
        )
    }
}
