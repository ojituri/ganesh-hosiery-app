package com.ganeshhosiery.autoreply.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ganeshhosiery.autoreply.data.MsgStatus
import com.ganeshhosiery.autoreply.ui.theme.ErrorRed
import com.ganeshhosiery.autoreply.ui.theme.SuccessGreen
import com.ganeshhosiery.autoreply.ui.theme.WarningAmber

/** A card that groups one section of a settings screen, with a title. */
@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScopeAlias.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Column(modifier = Modifier.padding(top = 10.dp)) { content() }
        }
    }
}

typealias ColumnScopeAlias = androidx.compose.foundation.layout.ColumnScope

/** A big, easy-to-tap primary action button - this app is used by non-technical people. */
@Composable
fun BigActionButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 6.dp))
    }
}

/** Small colored pill showing a message/call status such as Sent, Failed, Pending, Off. */
@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (color, label, icon) = when (status) {
        MsgStatus.SENT -> Triple(SuccessGreen, "Sent", Icons.Filled.CheckCircle)
        MsgStatus.FAILED -> Triple(ErrorRed, "Failed", Icons.Filled.Error)
        MsgStatus.PENDING -> Triple(WarningAmber, "Sending…", Icons.Filled.HourglassEmpty)
        MsgStatus.NOT_CONFIGURED -> Triple(WarningAmber, "Not set up", Icons.Filled.Warning)
        MsgStatus.OFF -> Triple(Color.Gray, "Off", Icons.Filled.Warning)
        else -> Triple(Color.Gray, "Not sent", Icons.Filled.Warning)
    }
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(0.dp))
        Text(label, color = color, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

/** One number + label, used on the Dashboard's stat row. */
@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
