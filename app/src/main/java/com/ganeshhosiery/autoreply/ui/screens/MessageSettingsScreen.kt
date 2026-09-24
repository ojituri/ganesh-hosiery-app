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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshhosiery.autoreply.data.Channel
import com.ganeshhosiery.autoreply.ui.components.BigActionButton
import com.ganeshhosiery.autoreply.viewmodel.MessageSettingsViewModel

@Composable
fun MessageSettingsScreen(onBack: () -> Unit, viewModel: MessageSettingsViewModel = viewModel()) {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(topBar = { TopAppBar(title = { Text("Message Settings") }, navigationIcon = { BackIcon(onBack) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("SMS") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("WhatsApp") })
            }
            if (tab == 0) TemplateEditor(Channel.SMS, viewModel) else TemplateEditor(Channel.WHATSAPP, viewModel)
        }
    }
}

@Composable
private fun TemplateEditor(channel: String, viewModel: MessageSettingsViewModel) {
    val template by if (channel == Channel.SMS) viewModel.smsTemplate.collectAsState() else viewModel.whatsappTemplate.collectAsState()

    var enabled by remember(channel) { mutableStateOf(true) }
    var body by remember(channel) { mutableStateOf("") }
    var loaded by remember(channel) { mutableStateOf(false) }

    LaunchedEffect(template) {
        val t = template ?: return@LaunchedEffect
        if (!loaded) { enabled = t.enabled; body = t.body; loaded = true }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (channel == Channel.SMS) "Send an automatic SMS" else "Send an automatic WhatsApp message",
                style = MaterialTheme.typography.titleMedium
            )
            Switch(checked = enabled, onCheckedChange = { enabled = it })
        }

        if (channel == Channel.WHATSAPP) {
            Text(
                "WhatsApp only works after WhatsApp Business Setup is completed and a template is approved by Meta. This text is used to fill that template's variables.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Message text") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            minLines = 6
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Available words (tap none needed — just type them into the text above):", style = MaterialTheme.typography.bodyMedium)
                Text(
                    viewModel.placeholders.joinToString("   ") { it.first },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Preview — what a customer named Rohit would see:", style = MaterialTheme.typography.titleMedium)
                Text(viewModel.preview(body), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BigActionButton("Save", modifier = Modifier.weight(1f)) { viewModel.save(channel, enabled, body) }
            BigActionButton("Reset to Default", modifier = Modifier.weight(1f)) {
                viewModel.resetToDefault(channel)
                loaded = false
            }
        }
    }
}
