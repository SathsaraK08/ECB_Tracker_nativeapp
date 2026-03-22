package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sathsara.ecbtracker.R
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.VerticalSpacer
import com.sathsara.ecbtracker.ui.theme.*
import com.sathsara.ecbtracker.ui.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    navController: NavController,
    viewModel: LogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        VerticalSpacer(24)

        Text(
            text = "Log Reading",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Enter current meter reading",
            fontSize = 14.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Previous reading info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Previous Reading:", color = TextMuted, fontSize = 14.sp)
            Text(
                text = "${uiState.previousUnit}",
                fontFamily = DMMonoFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        VerticalSpacer(24)

        // Reading Display (Simulating 7-segment/digital meter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val inputStr = uiState.currentUnitInput.padStart(7, '0')
            // Integer parts (5 digits)
            for (i in 0..4) {
                DigitBox(digit = inputStr[i].toString(), isDecimal = false)
            }
            
            Text(".", fontSize = 36.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 4.dp))
            
            // Decimal parts (2 digits)
            for (i in 5..6) {
                DigitBox(digit = inputStr[i].toString(), isDecimal = true)
            }
        }

        VerticalSpacer(16)

        // ML Kit Camera Scan Button
        Button(
            onClick = { /* TODO: Launch ML Kit Camera Scanner */ },
            colors = ButtonDefaults.buttonColors(containerColor = CyanDim, contentColor = CyanPrimary),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(painterResource(id = R.drawable.ic_camera), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan Meter with ML", fontWeight = FontWeight.SemiBold)
        }

        VerticalSpacer(32)

        Text(
            text = "What was running?",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val appliances = listOf("A/C", "Washing Machine", "Iron", "Oven", "Water Heater", "Pump", "Kettle", "Other")

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(180.dp), // Fixed height to avoid nested scroll issues
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(appliances) { appliance ->
                val isSelected = uiState.selectedAppliances.contains(appliance)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            if (isSelected) CyanDim else SurfaceDark,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) CyanPrimary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.toggleAppliance(appliance) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appliance,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) CyanPrimary else TextMuted
                    )
                }
            }
        }

        VerticalSpacer(24)

        Text(
            text = "Notes (Optional)",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = uiState.note,
            onValueChange = { viewModel.updateNote(it) },
            placeholder = { Text("Add any specifics...", color = TextMuted) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = CyanPrimary,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            )
        )

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        VerticalSpacer(32)

        PrimaryButton(
            text = if (uiState.isLoading) "Saving..." else "Save Reading",
            onClick = { viewModel.submitReading() },
            enabled = !uiState.isLoading
        )

        VerticalSpacer(80) // Navigation bar padding
    }
}

@Composable
fun DigitBox(digit: String, isDecimal: Boolean) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .width(if (isDecimal) 34.dp else 40.dp)
            .height(if (isDecimal) 50.dp else 60.dp)
            .background(if (isDecimal) Color(0xFF1E293B) else Color.Black, RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        // RedDanger color for decimals matching standard analog meters
        val textColor = if (isDecimal) RedDanger else MaterialTheme.colorScheme.onBackground
        
        Text(
            text = digit,
            fontFamily = DMMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = if (isDecimal) 24.sp else 32.sp,
            color = textColor
        )
    }
}
