package com.ganeshhosiery.autoreply.ui.screens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshhosiery.autoreply.ui.components.BigActionButton
import com.ganeshhosiery.autoreply.viewmodel.WhatsAppSetupViewModel

@Composable
fun WhatsAppSetupScreen(onBack: () -> Unit, viewModel: WhatsAppSetupViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val checking by viewModel.checking.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()

    var phoneNumberId by remember { mutableStateOf("") }
    var businessAccountId by remember { mutableStateOf("") }
    var accessToken by remember { mutableStateOf("") }
    var apiVersion by remember { mutableStateOf("v25.0") }
    var templateName by remember { mutableStateOf("") }
    var templateLanguage by remember { mutableStateOf("en") }
    var templateVariables by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        val s = settings ?: return@LaunchedEffect
        if (!loaded) {
            phoneNumberId = s.waPhoneNumberId
            businessAccountId = s.waBusinessAccountId
            apiVersion = s.waApiVersion
            templateName = s.waTemplateName
            templateLanguage = s.waTemplateLanguage
            templateVariables = s.waTemplateVariables
            loaded = true
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("WhatsApp Business Setup") }, navigationIcon = { BackIcon(onBack) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("WhatsApp is optional.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "This app only uses Meta's official WhatsApp Business Cloud API. It never automates the WhatsApp app on this phone. " +
                            "You need a Meta Business account, a WhatsApp Business Phone Number ID, an Access Token, and one approved message template. " +
                            "See the README that came with this app for the full step-by-step setup on Meta's website.",
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Field("Phone Number ID", phoneNumberId) { phoneNumberId = it }
            Field("Business Account ID (optional)", businessAccountId) { businessAccountId = it }
            OutlinedTextField(
                value = accessToken,
                onValueChange = { accessToken = it },
                label = { Text(if (viewModel.hasSavedToken()) "Access Token (already saved — leave blank to keep it)" else "Access Token") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Field("Graph API version", apiVersion) { apiVersion = it }
            Field("Approved template name", templateName) { templateName = it }
            Field("Template language code (e.g. en)", templateLanguage) { templateLanguage = it }
            OutlinedTextField(
                value = templateVariables, onValueChange = { templateVariables = it },
                label = { Text("Template variables note (optional — for your own reference)") },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BigActionButton("Test Connection", modifier = Modifier.weight(1f)) {
                    viewModel.testConnection(phoneNumberId, accessToken, apiVersion)
                }
                BigActionButton("Remove Saved Token", modifier = Modifier.weight(1f)) { viewModel.clearToken() }
            }
            if (checking) CircularProgressIndicator()

            BigActionButton("Save WhatsApp Settings and Enable") {
                viewModel.save(phoneNumberId, businessAccountId, accessToken.ifBlank { null }, apiVersion, templateName, templateLanguage, templateVariables, enabled = true)
            }

            resultMessage?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
}
