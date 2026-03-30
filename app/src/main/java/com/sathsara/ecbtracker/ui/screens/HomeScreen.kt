package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.ui.components.BrandHeader
import com.sathsara.ecbtracker.ui.components.EmptyState
import com.sathsara.ecbtracker.ui.components.LoadingSkeleton
import com.sathsara.ecbtracker.ui.components.MetricTile
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.SectionCard
import com.sathsara.ecbtracker.ui.components.SectionHeading
import com.sathsara.ecbtracker.logic.CurrencyFormatter
import com.sathsara.ecbtracker.ui.theme.CyanPrimary
import com.sathsara.ecbtracker.ui.theme.DMMonoFamily
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onLogReading: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 96.dp)
    ) {
        item {
            BrandHeader(
                title = "Hi, ${uiState.username}",
                subtitle = "Account ${uiState.accountNumber}"
            )
        }

        item {
            SectionCard {
                SectionHeading(
                    title = "This month at a glance",
                    subtitle = "Track electricity use before the bill surprises you."
                )

                if (uiState.isLoading) {
                    LoadingSkeleton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                } else {
                    Text(
                        text = CurrencyFormatter.format(uiState.currencyCode, uiState.projectedBill),
                        fontFamily = DMMonoFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Projected month-end bill. Current usage cost: ${CurrencyFormatter.format(uiState.currencyCode, uiState.currentBill)} using ${uiState.currencyCode} ${String.format("%.2f", uiState.ratePerUnit)} per unit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }

                PrimaryButton(
                    text = "Log a new reading",
                    onClick = onLogReading,
                    enabled = !uiState.isLoading
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = "Today",
                    value = String.format("%.1f", uiState.todayKwh),
                    helper = "kWh used today",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Week",
                    value = String.format("%.1f", uiState.weeklyKwh),
                    helper = "kWh over 7 days",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = "Month",
                    value = String.format("%.1f", uiState.monthlyKwh),
                    helper = "kWh this month",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Avg day",
                    value = String.format("%.1f", uiState.averageDailyKwh),
                    helper = "kWh per day",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SectionCard {
                SectionHeading(
                    title = "Forecast and reduction tips",
                    subtitle = if (uiState.peakHours.isBlank()) {
                        "The app will highlight patterns after a few saved readings."
                    } else {
                        "Higher usage is currently clustering in the ${uiState.peakHours.lowercase()}."
                    }
                )
                if (uiState.forecastTips.isEmpty()) {
                    Text(
                        text = "Log readings with appliance notes like A/C, rice cooker, water heater, and kettle so the advice becomes more specific.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.forecastTips.take(3).forEach { tip ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = tip.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = tip.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Muted
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionHeading(
                title = "Recent readings",
                subtitle = "Your newest saved meter entries"
            )
        }

        if (uiState.isLoading) {
            items(3) {
                LoadingSkeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                )
            }
        } else if (uiState.recentActivity.isEmpty()) {
            item {
                EmptyState(
                    title = "No readings yet",
                    description = "Log your first meter reading to start building your usage history."
                )
            }
        } else {
            items(uiState.recentActivity) { entry ->
                RecentReadingCard(entry = entry)
            }
        }
    }
}

@Composable
private fun RecentReadingCard(entry: Entry) {
    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
            Text(
                text = "+${String.format("%.1f", entry.used)} kWh",
                style = MaterialTheme.typography.titleMedium,
                color = CyanPrimary
            )
        }

        Text(
            text = "Meter reading ${String.format("%.2f", entry.unit)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (!entry.note.isNullOrBlank()) {
            Text(
                text = entry.note.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }
    }
}
