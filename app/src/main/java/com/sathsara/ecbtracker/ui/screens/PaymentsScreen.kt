package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sathsara.ecbtracker.data.model.Payment
import com.sathsara.ecbtracker.ui.components.LoadingSkeleton
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.SecondaryOutlineButton
import com.sathsara.ecbtracker.ui.components.VerticalSpacer
import com.sathsara.ecbtracker.ui.theme.*
import com.sathsara.ecbtracker.ui.viewmodel.PaymentsViewModel

@Composable
fun PaymentsScreen(
    viewModel: PaymentsViewModel = hiltViewModel()
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
            text = "Payments",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Manage your CEB bills and history",
            fontSize = 14.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Current Bill Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isLoading) {
                        LoadingSkeleton(modifier = Modifier.width(100.dp).height(20.dp))
                    } else {
                        Text(
                            text = "${uiState.currentMonthLabel} Bill",
                            fontFamily = OutfitFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(AmberDim, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Due in 12 days", // hardcoded mock for UI
                            color = AmberWarning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                VerticalSpacer(12)

                if (uiState.isLoading) {
                    LoadingSkeleton(modifier = Modifier.width(150.dp).height(40.dp))
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("LKR ", fontSize = 16.sp, color = TextMuted, modifier = Modifier.padding(bottom = 6.dp))
                        Text(
                            text = String.format("%,.0f", uiState.currentBillAmount),
                            fontFamily = DMMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                VerticalSpacer(8)
                
                if (uiState.isLoading) {
                    LoadingSkeleton(modifier = Modifier.width(180.dp).height(16.dp))
                } else {
                    Text(
                        text = "Based on ${String.format("%.1f", uiState.lastUnits)} kWh usage. Acc: ${uiState.accountNumber}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                VerticalSpacer(24)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PrimaryButton(text = "Pay Now", onClick = { /* TODO */ }, modifier = Modifier.weight(1f))
                    SecondaryOutlineButton(text = "View Invoice", onClick = { /* TODO */ }, modifier = Modifier.weight(1f))
                }
            }
        }

        VerticalSpacer(32)

        Text(
            text = "Payment History",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            LazyColumn {
                items(3) {
                    LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(80.dp).padding(bottom = 12.dp))
                }
            }
        } else if (uiState.paymentHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center) {
                Text("No payment history yet.", color = TextMuted)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                items(uiState.paymentHistory) { payment ->
                    PaymentItem(payment)
                }
            }
        }
    }
}

@Composable
fun PaymentItem(payment: Payment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (payment.paid) GreenDim else AmberDim, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (payment.paid) "✓" else "!", fontSize = 18.sp, color = if (payment.paid) GreenSuccess else AmberWarning)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "${payment.month} Bill",
                    fontFamily = OutfitFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (payment.paid) "Paid via ${payment.bank ?: "Card"}" else "Payment Pending",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "LKR ${String.format("%,.0f", payment.billAmount)}",
                fontFamily = DMMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Optional: View receipt button could go here
        }
    }
}
