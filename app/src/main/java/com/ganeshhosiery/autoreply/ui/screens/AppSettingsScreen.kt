package com.ganeshhosiery.autoreply.ui.screens

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshhosiery.autoreply.ui.components.BigActionButton
import com.ganeshhosiery.autoreply.viewmodel.AppSettingsViewModel

@Composable
fun AppSettingsScreen(onBack: () -> Unit, viewModel: AppSettingsViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()

    var recordExcluded by remember { mutableStateOf(true) }
    var duplicateWindow by remember { mutableStateOf("30") }
    var repeatMinutes by remember { mutableStateOf("60") }
    var dailyLimit by remember { mutableStateOf("200") }
    var loaded by remember { mutableStateOf(false) }
    var simMenuOpen by remember { mutableStateOf(false) }
    var chosenSimLabel by remember { mutableStateOf("Phone's default SIM") }
    var chosenSimId by remember { mutableStateOf(-1) }

    val sims = remember { viewModel.sims() }

    LaunchedEffect(settings) {
        val s = settings ?: return@LaunchedEffect
        if (!loaded) {
            recordExcluded = s.recordExcludedCalls
            duplicateWindow = s.duplicateWindowSeconds.toString()
            repeatMinutes = s.repeatCallerMinutes.toString()
            dailyLimit = s.dailySmsLimit.toString()
            chosenSimId = s.smsSubscriptionId
            chosenSimLabel = sims.firstOrNull { it.subscriptionId == s.smsSubscriptionId }?.label ?: "Phone's default SIM"
            loaded = true
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("App Settings") }, navigationIcon = { BackIcon(onBack) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text("Record excluded calls in history", style = MaterialTheme.typography.titleMedium)
                        Text("Off means excluded numbers won't appear in Call History at all.", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = recordExcluded, onCheckedChange = { recordExcluded = it })
                }
            }

            OutlinedTextField(
                value = duplicateWindow, onValueChange = { duplicateWindow = it.filter(Char::isDigit) },
                label = { Text("Treat the same call as one event within (seconds)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = repeatMinutes, onValueChange = { repeatMinutes = it.filter(Char::isDigit) },
                label = { Text("Don't message the same number again for (minutes, 0 = always message)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dailyLimit, onValueChange = { dailyLimit = it.filter(Char::isDigit) },
                label = { Text("Maximum automatic SMS per day (0 = no limit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (sims.size > 1) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("SIM card used to send SMS", style = MaterialTheme.typography.titleMedium)
                        Text(chosenSimLabel, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                        BigActionButton("Choose SIM") { simMenuOpen = true }
                        DropdownMenu(expanded = simMenuOpen, onDismissRequest = { simMenuOpen = false }) {
                            DropdownMenuItem(text = { Text("Phone's default SIM") }, onClick = {
                                chosenSimId = -1; chosenSimLabel = "Phone's default SIM"; simMenuOpen = false
                            })
                            sims.forEach { sim ->
                                DropdownMenuItem(text = { Text(sim.label) }, onClick = {
                                    chosenSimId = sim.subscriptionId; chosenSimLabel = sim.label; simMenuOpen = false
                                })
                            }
                        }
                    }
                }
            }

            BigActionButton("Save Settings") {
                viewModel.update {
                    it.copy(
                        recordExcludedCalls = recordExcluded,
                        duplicateWindowSeconds = duplicateWindow.toIntOrNull() ?: 30,
                        repeatCallerMinutes = repeatMinutes.toIntOrNull() ?: 60,
                        dailySmsLimit = dailyLimit.toIntOrNull() ?: 200,
                        smsSubscriptionId = chosenSimId
                    )
                }
                onBack()
            }
        }
    }
}
