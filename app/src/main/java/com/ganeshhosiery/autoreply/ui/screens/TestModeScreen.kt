package com.ganeshhosiery.autoreply.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.ganeshhosiery.autoreply.ui.components.BigActionButton
import com.ganeshhosiery.autoreply.viewmodel.TestModeViewModel

@Composable
fun TestModeScreen(onBack: () -> Unit, viewModel: TestModeViewModel = viewModel()) {
    var number by remember { mutableStateOf("") }
    val busy by viewModel.busy.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Test Mode") }, navigationIcon = { BackIcon(onBack) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Use a real phone number you can check (like your own second phone) to make sure everything works before relying on it.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = number, onValueChange = { number = it },
                label = { Text("Test phone number") },
                modifier = Modifier.fillMaxWidth()
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Send a test SMS", style = MaterialTheme.typography.titleMedium)
                    Text("Sends the current SMS message text to the number above right now.", style = MaterialTheme.typography.bodyMedium)
                    BigActionButton("Send Test SMS", enabled = !busy) { viewModel.sendTestSms(number) }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Send a test WhatsApp message", style = MaterialTheme.typography.titleMedium)
                    Text("Requires WhatsApp Business Setup to be completed first.", style = MaterialTheme.typography.bodyMedium)
                    BigActionButton("Send Test WhatsApp", enabled = !busy) { viewModel.sendTestWhatsApp(number) }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Simulate a full incoming call", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Runs the exact same steps as a real call from this number (excluded-list check, SMS, WhatsApp) " +
                            "and adds a marked \"test\" entry to Call History, without affecting daily limits.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    BigActionButton("Simulate Call", enabled = !busy) { viewModel.simulateIncomingCall(number) }
                }
            }

            if (busy) CircularProgressIndicator()
            resultMessage?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
        }
    }
}
