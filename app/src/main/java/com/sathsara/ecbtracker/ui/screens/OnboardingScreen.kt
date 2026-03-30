package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sathsara.ecbtracker.ui.components.BrandHeader
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.SectionCard
import com.sathsara.ecbtracker.ui.components.SectionHeading
import com.sathsara.ecbtracker.ui.components.StatusBanner
import com.sathsara.ecbtracker.ui.theme.CyanPrimary
import com.sathsara.ecbtracker.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onComplete()
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
                title = "Finish setup",
                subtitle = "A few details help the tracker show accurate names, account info, and bill estimates."
            )

            if (uiState.error != null) {
                StatusBanner(message = uiState.error!!, isError = true)
            }

            SectionCard {
                SectionHeading(
                    title = "Your details",
                    subtitle = "You can change these later in Settings."
                )

                OnboardingField(
                    label = "Display name",
                    value = uiState.displayName,
                    onValueChange = viewModel::updateDisplayName
                )
                OnboardingField(
                    label = "CEB account number",
                    value = uiState.accountNumber,
                    onValueChange = viewModel::updateAccountNumber
                )
                OnboardingField(
                    label = "Electricity rate (LKR per unit)",
                    value = uiState.ratePerUnit,
                    onValueChange = viewModel::updateRatePerUnit
                )

                PrimaryButton(
                    text = if (uiState.isSaving) "Saving..." else "Continue to dashboard",
                    onClick = viewModel::save,
                    enabled = !uiState.isSaving
                )
            }
        }
    }
}

@Composable
private fun OnboardingField(
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
