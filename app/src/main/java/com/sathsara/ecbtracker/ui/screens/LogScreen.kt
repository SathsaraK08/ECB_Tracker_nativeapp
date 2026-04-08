package com.sathsara.ecbtracker.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sathsara.ecbtracker.logic.MeterReadingParser
import com.sathsara.ecbtracker.logic.CurrencyFormatter
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.SectionCard
import com.sathsara.ecbtracker.ui.components.SectionHeading
import com.sathsara.ecbtracker.ui.components.SecondaryOutlineButton
import com.sathsara.ecbtracker.ui.components.StatusBanner
import com.sathsara.ecbtracker.ui.theme.CyanDim
import com.sathsara.ecbtracker.ui.theme.CyanPrimary
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.viewmodel.LogViewModel
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogScreen(
    onSaved: () -> Unit,
    viewModel: LogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val previewReading = MeterReadingParser.parse(uiState.currentUnitInput)
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val tempFile = uri?.let { copyUriToCache(context, it) }
        if (tempFile != null) {
            viewModel.setPhoto(tempFile)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSaved()
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
            SectionHeading(
                title = "Log a reading",
                subtitle = "Use the current meter value so the dashboard and history stay accurate."
            )

            if (uiState.error != null) {
                StatusBanner(message = uiState.error!!, isError = true)
            }

            SectionCard {
                Text(
                    text = "Previous reading",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted
                )
                Text(
                    text = String.format("%.2f", uiState.previousUnit),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (uiState.usagePreview != null) {
                    Text(
                        text = "Planned usage gap: ${String.format("%.2f", uiState.usagePreview)} units",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }

            SectionCard {
                SectionHeading(
                    title = "Reading moment",
                    subtitle = "You can log older readings like yesterday morning or evening."
                )

                OutlinedTextField(
                    value = uiState.dateInput,
                    onValueChange = viewModel::updateDateInput,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("YYYY-MM-DD") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                OutlinedTextField(
                    value = uiState.timeInput,
                    onValueChange = viewModel::updateTimeInput,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("HH:mm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Text(
                    text = "The app blocks duplicate entries for the same date and time on the shared account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }

            SectionCard {
                SectionHeading(
                    title = "Current meter value",
                    subtitle = "Enter 7 digits. The last two digits are treated as decimals."
                )

                OutlinedTextField(
                    value = uiState.currentUnitInput,
                    onValueChange = viewModel::updateUnitInput,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Example: 1234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Text(
                    text = if (previewReading == null) {
                        "Preview will appear when all 7 digits are entered."
                    } else {
                        "Parsed reading: ${String.format("%.2f", previewReading)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )

                uiState.usagePreview?.let { usagePreview ->
                    Text(
                        text = "Estimated cost for this gap: ${
                            CurrencyFormatter.format(
                                uiState.currencyCode,
                                usagePreview * uiState.ratePerUnit
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }

            SectionCard {
                SectionHeading(
                    title = "What was running?",
                    subtitle = "Optional context to help you spot patterns later."
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        "A/C",
                        "Washing Machine",
                        "Iron",
                        "Oven",
                        "Water Heater",
                        "Pump",
                        "Kettle",
                        "Other"
                    ).forEach { appliance ->
                        val isSelected = appliance in uiState.selectedAppliances
                        Text(
                            text = appliance,
                            modifier = Modifier
                                .background(
                                    if (isSelected) CyanDim else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(999.dp)
                                )
                                .clickable { viewModel.toggleAppliance(appliance) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            SectionCard {
                SectionHeading(
                    title = "Meter proof image",
                    subtitle = "Attach a meter photo when you want image proof saved to Supabase storage."
                )

                SecondaryOutlineButton(
                    text = if (uiState.photoFile == null) "Attach meter photo" else "Replace meter photo",
                    onClick = { photoPicker.launch("image/*") }
                )

                uiState.photoFile?.let { photoFile ->
                    AsyncImage(
                        model = photoFile,
                        contentDescription = "Attached meter proof",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    Text(
                        text = photoFile.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }

            SectionCard {
                SectionHeading(
                    title = "Notes",
                    subtitle = "Optional comments about unusual usage, outages, or appliances."
                )

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::updateNote,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add anything worth remembering") },
                    minLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            PrimaryButton(
                text = if (uiState.isLoading) "Saving..." else "Save reading",
                onClick = viewModel::submitReading,
                enabled = !uiState.isLoading && uiState.currentUnitInput.isNotBlank()
            )
        }
    }
}

private fun copyUriToCache(context: Context, uri: Uri): File? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val targetFile = File(context.cacheDir, "meter_${System.currentTimeMillis()}.jpg")
    inputStream.use { input ->
        targetFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return targetFile
}
