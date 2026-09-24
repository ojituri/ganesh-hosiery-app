@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ganeshhosiery.autoreply.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshhosiery.autoreply.core.Permissions
import com.ganeshhosiery.autoreply.core.findActivity
import com.ganeshhosiery.autoreply.ui.components.BigActionButton
import com.ganeshhosiery.autoreply.ui.theme.ErrorRed
import com.ganeshhosiery.autoreply.ui.theme.SuccessGreen
import com.ganeshhosiery.autoreply.viewmodel.SystemStatus
import com.ganeshhosiery.autoreply.viewmodel.SystemStatusViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun SystemStatusScreen(onBack: () -> Unit, onOpenBackgroundGuide: () -> Unit, viewModel: SystemStatusViewModel = viewModel()) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(viewModel.currentStatus()) }

    LaunchedEffect(Unit) { status = viewModel.currentStatus() }

    Scaffold(topBar = { TopAppBar(title = { Text("System Status") }, navigationIcon = { BackIcon(onBack) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusRow("Detect incoming calls", status.phoneStateGranted && status.callLogGranted,
                "Needs Phone permission (and Call Log permission on newer Android) so the app can see who is calling.")
            StatusRow("Send SMS", status.smsGranted, "Needs SMS permission.")
            StatusRow("Show caller names", status.contactsGranted, "Optional. Needs Contacts permission.")
            StatusRow("Keeps running in background", status.batteryUnrestricted,
                "Some phones (Xiaomi, Oppo, Vivo, OnePlus...) stop apps in the background unless allowed. See the guide below.")
            StatusRow("Internet connection", status.online, "Needed only for WhatsApp messages.")
            StatusRow("WhatsApp Business API", status.whatsappConfigured, "Optional. Set up under WhatsApp Business Setup.")

            if (!status.phoneStateGranted || !status.callLogGranted || !status.smsGranted || !status.contactsGranted || !status.batteryUnrestricted) {
                BigActionButton("Open App Permission Settings") { Permissions.openAppSettings(context) }
            }
            if (!status.batteryUnrestricted) {
                BigActionButton("Fix Background Reliability") { onOpenBackgroundGuide() }
            }
            BigActionButton("Re-check Now") { status = viewModel.currentStatus() }
        }
    }
}

@Composable
private fun StatusRow(title: String, ok: Boolean, explanation: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(
                if (ok) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = if (ok) SuccessGreen else ErrorRed,
                modifier = Modifier.padding(end = 10.dp)
            )
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(if (ok) "Working" else "Needs attention", style = MaterialTheme.typography.bodyMedium)
                Text(explanation, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
