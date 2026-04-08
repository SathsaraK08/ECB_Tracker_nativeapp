package com.sathsara.ecbtracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sathsara.ecbtracker.R
import com.sathsara.ecbtracker.ui.theme.CyanDim
import com.sathsara.ecbtracker.ui.theme.CyanPrimary
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.theme.OutfitFamily
import com.sathsara.ecbtracker.ui.theme.SurfaceDark

@Composable
fun BrandHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = title,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CyanDim),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun SectionHeading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }
    }
}

@Composable
fun StatusBanner(message: String, isError: Boolean, modifier: Modifier = Modifier) {
    val bannerColor = if (isError) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
    } else {
        CyanDim
    }
    val textColor = if (isError) MaterialTheme.colorScheme.error else CyanPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bannerColor)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = message,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    helper: String,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Muted
        )
        Text(
            text = value,
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = helper,
            style = MaterialTheme.typography.bodySmall,
            color = Muted
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Muted
        )
    }
}
