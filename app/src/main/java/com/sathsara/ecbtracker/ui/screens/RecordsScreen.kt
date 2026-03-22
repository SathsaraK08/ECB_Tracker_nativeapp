package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.ui.components.LoadingSkeleton
import com.sathsara.ecbtracker.ui.components.VerticalSpacer
import com.sathsara.ecbtracker.ui.theme.*
import com.sathsara.ecbtracker.ui.viewmodel.FilterMode
import com.sathsara.ecbtracker.ui.viewmodel.RecordsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        VerticalSpacer(24)

        Text(
            text = "Data Records",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Complete history of meter readings",
            fontSize = 14.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.filterMode == FilterMode.ALL,
                onClick = { viewModel.setFilterMode(FilterMode.ALL) },
                label = { Text("All", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanPrimary,
                    selectedLabelColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                border = null
            )
            FilterChip(
                selected = uiState.filterMode == FilterMode.VERIFIED,
                onClick = { viewModel.setFilterMode(FilterMode.VERIFIED) },
                label = { Text("Verified (Photo)", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanPrimary,
                    selectedLabelColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                border = null
            )
            FilterChip(
                selected = uiState.filterMode == FilterMode.PENDING,
                onClick = { viewModel.setFilterMode(FilterMode.PENDING) },
                label = { Text("Pending Auth", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanPrimary,
                    selectedLabelColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                border = null
            )
        }

        if (uiState.isLoading) {
            LazyColumn {
                items(5) {
                    LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(140.dp).padding(bottom = 12.dp))
                }
            }
        } else if (uiState.entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No records found.", color = TextMuted)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp) // Nav bar padding
            ) {
                items(
                    items = uiState.entries,
                    key = { it.id ?: it.hashCode() }
                ) { entry ->
                    RecordItem(
                        entry = entry,
                        ratePerUnit = uiState.ratePerUnit,
                        onDelete = { entry.id?.let { id -> viewModel.deleteEntry(id) } }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordItem(entry: Entry, ratePerUnit: Double, onDelete: () -> Unit) {
    val estimatedCost = entry.used * ratePerUnit
    val isVerified = entry.imgUrl != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                contentDescription = "Delete",
                tint = RedDanger.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${entry.date} at ${entry.time}",
                        fontSize = 13.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(if (isVerified) GreenDim else AmberDim, RoundedCornerShape(12.dp))
                         .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isVerified) "Verified" else "Pending",
                        color = if (isVerified) GreenSuccess else AmberWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            VerticalSpacer(12)

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reading", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = entry.unit.toString(),
                        fontFamily = DMMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Usage", fontSize = 11.sp, color = TextMuted)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "+${String.format("%.1f", entry.used)}",
                            fontFamily = DMMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CyanPrimary
                        )
                        Text(" kWh", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(bottom = 3.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Est. Cost", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "LKR ${String.format("%,.0f", estimatedCost)}",
                        fontFamily = DMMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (!entry.appliances.isNullOrEmpty() || !entry.note.isNullOrBlank()) {
                VerticalSpacer(12)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                VerticalSpacer(12)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (!entry.appliances.isNullOrEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            entry.appliances.take(3).forEach { app ->
                                Box(modifier = Modifier.background(Color(0xFF1E293B), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text(app, fontSize = 10.sp, color = TextMuted)
                                }
                            }
                            if (entry.appliances.size > 3) {
                                Box(modifier = Modifier.background(Color(0xFF1E293B), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text("+${entry.appliances.size - 3}", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    if (!entry.note.isNullOrBlank()) {
                        Text("📝 Note", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}
