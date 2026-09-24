@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ganeshhosiery.autoreply.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshhosiery.autoreply.core.PhoneUtils
import com.ganeshhosiery.autoreply.core.TimeUtils
import com.ganeshhosiery.autoreply.ui.components.StatusBadge
import com.ganeshhosiery.autoreply.viewmodel.CallHistoryViewModel

@Composable
fun CallHistoryScreen(onBack: () -> Unit, viewModel: CallHistoryViewModel = viewModel()) {
    val calls by viewModel.calls.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }
    val grouped = calls.groupBy { TimeUtils.dayLabel(it.timestamp) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call History") },
                navigationIcon = { BackIcon(onBack) },
                actions = { TextButton(onClick = { showClearConfirm = true }) { Text("Clear") } }
            )
        }
    ) { padding ->
        if (calls.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("No calls recorded yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                grouped.forEach { (day, dayCalls) ->
                    item { Text(day, style = MaterialTheme.typography.titleMedium) }
                    items(dayCalls, key = { it.id }) { call ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        (call.callerName ?: PhoneUtils.display(call.number)) + if (call.isTest) "  (test)" else "",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(TimeUtils.timeText(call.timestamp), style = MaterialTheme.typography.bodyMedium)
                                }
                                if (call.isExcluded) {
                                    Text("Excluded number — no message sent", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                                        Text("SMS:", style = MaterialTheme.typography.bodyMedium)
                                        StatusBadge(call.smsStatus)
                                        Text("WhatsApp:", style = MaterialTheme.typography.bodyMedium)
                                        StatusBadge(call.whatsappStatus)
                                    }
                                    call.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear call history?") },
            text = { Text("This removes all recorded calls and messages from this phone. It cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearHistory(); showClearConfirm = false }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") } }
        )
    }
}
