package com.ganeshhosiery.autoreply.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshhosiery.autoreply.core.PhoneUtils
import com.ganeshhosiery.autoreply.core.TimeUtils
import com.ganeshhosiery.autoreply.ui.components.BigActionButton
import com.ganeshhosiery.autoreply.ui.components.StatTile
import com.ganeshhosiery.autoreply.ui.components.StatusBadge
import com.ganeshhosiery.autoreply.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onOpenExcluded: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTestMode: () -> Unit,
    onOpenStatus: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBackgroundGuide: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val stats by viewModel.todayStats.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val recent by viewModel.recentCalls.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Ganesh Hosiery — Auto Reply") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto Reply", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (settings?.autoReplyEnabled == true) "On — replying to callers automatically"
                                else "Off — no automatic replies will be sent",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Switch(
                            checked = settings?.autoReplyEnabled ?: true,
                            onCheckedChange = { viewModel.setAutoReplyEnabled(it) }
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatTile(stats.calls.toString(), "Calls today")
                        StatTile(stats.smsSent.toString(), "SMS sent")
                        StatTile(stats.whatsappSent.toString(), "WhatsApp sent")
                        StatTile(stats.failed.toString(), "Failed")
                    }
                }
            }

            item { Text("Manage", style = MaterialTheme.typography.titleMedium) }
            item { BigActionButton("Excluded Contacts", onClick = onOpenExcluded) }
            item { BigActionButton("Shop Details", onClick = onOpenShop) }
            item { BigActionButton("Message Settings (SMS / WhatsApp text)", onClick = onOpenMessages) }
            item { BigActionButton("WhatsApp Business Setup", onClick = onOpenWhatsApp) }
            item { BigActionButton("Call History", onClick = onOpenHistory) }
            item { BigActionButton("Test Mode", onClick = onOpenTestMode) }
            item { BigActionButton("System Status", onClick = onOpenStatus) }
            item { BigActionButton("App Settings", onClick = onOpenSettings) }
            item { BigActionButton("Keep Working in Background (important!)", onClick = onOpenBackgroundGuide) }

            item { Text("Recent calls", style = MaterialTheme.typography.titleMedium) }
            if (recent.isEmpty()) {
                item { Text("No calls yet.", style = MaterialTheme.typography.bodyMedium) }
            }
            items(recent.take(5)) { call ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "${call.callerName ?: PhoneUtils.display(call.number)}  •  ${TimeUtils.timeText(call.timestamp)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                            if (call.isExcluded) {
                                Text("Excluded — no message sent", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text("SMS:", style = MaterialTheme.typography.bodyMedium)
                                StatusBadge(call.smsStatus)
                                Text("WhatsApp:", style = MaterialTheme.typography.bodyMedium)
                                StatusBadge(call.whatsappStatus)
                            }
                        }
                    }
                }
            }
        }
    }
}
