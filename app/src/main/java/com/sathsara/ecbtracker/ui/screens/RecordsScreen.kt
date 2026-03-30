package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.ui.components.EmptyState
import com.sathsara.ecbtracker.ui.components.LoadingSkeleton
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.SectionCard
import com.sathsara.ecbtracker.ui.components.SectionHeading
import com.sathsara.ecbtracker.ui.components.StatusBanner
import com.sathsara.ecbtracker.logic.CurrencyFormatter
import com.sathsara.ecbtracker.ui.theme.AmberDim
import com.sathsara.ecbtracker.ui.theme.AmberWarning
import com.sathsara.ecbtracker.ui.theme.CyanPrimary
import com.sathsara.ecbtracker.ui.theme.DMMonoFamily
import com.sathsara.ecbtracker.ui.theme.GreenDim
import com.sathsara.ecbtracker.ui.theme.GreenSuccess
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.viewmodel.FilterMode
import com.sathsara.ecbtracker.ui.viewmodel.RecordsViewModel

@Composable
fun RecordsScreen(
    onLogReading: () -> Unit,
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeading(
            title = "History",
            subtitle = "Review saved readings and clean up entries that should not stay in your records."
        )

        if (uiState.error != null) {
            StatusBanner(message = uiState.error!!, isError = true)
        }

        SectionCard {
            SectionHeading(
                title = "Usage summary",
                subtitle = "Newest readings stay at the top because the meter value only increases."
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryMetric(
                    label = "Today",
                    value = String.format("%.1f kWh", uiState.todayKwh),
                    modifier = Modifier.weight(1f)
                )
                HistoryMetric(
                    label = "Week",
                    value = String.format("%.1f kWh", uiState.weeklyKwh),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryMetric(
                    label = "Month",
                    value = String.format("%.1f kWh", uiState.monthlyKwh),
                    modifier = Modifier.weight(1f)
                )
                HistoryMetric(
                    label = "Cost so far",
                    value = CurrencyFormatter.format(
                        uiState.currencyCode,
                        uiState.monthlyKwh * uiState.ratePerUnit
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HistoryFilterChip(
                label = "All",
                selected = uiState.filterMode == FilterMode.ALL,
                onClick = { viewModel.setFilterMode(FilterMode.ALL) }
            )
            HistoryFilterChip(
                label = "With photo",
                selected = uiState.filterMode == FilterMode.VERIFIED,
                onClick = { viewModel.setFilterMode(FilterMode.VERIFIED) }
            )
            HistoryFilterChip(
                label = "Without photo",
                selected = uiState.filterMode == FilterMode.PENDING,
                onClick = { viewModel.setFilterMode(FilterMode.PENDING) }
            )
        }

        if (uiState.isLoading) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(4) {
                    LoadingSkeleton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(124.dp)
                            .padding(bottom = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        } else if (uiState.entries.isEmpty()) {
            EmptyState(
                title = "No readings to show",
                description = "Once you save a meter reading, it will appear here with the estimated cost."
            )
            PrimaryButton(
                text = "Log your first reading",
                onClick = onLogReading
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.entries, key = { it.id }) { entry ->
                    HistoryItem(
                        entry = entry,
                        ratePerUnit = uiState.ratePerUnit,
                        currencyCode = uiState.currencyCode,
                        onDelete = { viewModel.deleteEntry(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyanPrimary.copy(alpha = 0.16f),
            selectedLabelColor = CyanPrimary
        )
    )
}

@Composable
private fun HistoryItem(
    entry: Entry,
    ratePerUnit: Double,
    currencyCode: String,
    onDelete: () -> Unit
) {
    val estimatedCost = entry.used * ratePerUnit
    val hasPhoto = !entry.imgUrl.isNullOrBlank()

    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = entry.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete reading",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HistoryMetric(label = "Meter", value = String.format("%.2f", entry.unit), modifier = Modifier.weight(1f))
            HistoryMetric(label = "Usage", value = "+${String.format("%.1f", entry.used)} kWh", modifier = Modifier.weight(1f))
            HistoryMetric(
                label = "Cost",
                value = CurrencyFormatter.format(currencyCode, estimatedCost),
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .background(
                    if (hasPhoto) GreenDim else AmberDim,
                    androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (hasPhoto) "Photo attached" else "No photo attached",
                color = if (hasPhoto) GreenSuccess else AmberWarning,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (!entry.note.isNullOrBlank()) {
            Text(
                text = entry.note.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }

        if (!entry.appliances.isNullOrEmpty()) {
            Text(
                text = "Appliances: ${entry.appliances.joinToString()}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }
    }
}

@Composable
private fun HistoryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Muted
        )
        Text(
            text = value,
            fontFamily = DMMonoFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
