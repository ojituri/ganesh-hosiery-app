package com.ganeshhosiery.autoreply.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshhosiery.autoreply.core.ContactsHelper
import com.ganeshhosiery.autoreply.core.PhoneUtils
import com.ganeshhosiery.autoreply.ui.components.BigActionButton
import com.ganeshhosiery.autoreply.viewmodel.ExcludedContactsViewModel

@Composable
fun ExcludedContactsScreen(onBack: () -> Unit, viewModel: ExcludedContactsViewModel = viewModel()) {
    val contacts by viewModel.contacts.collectAsState()
    val message by viewModel.message.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val pickContact = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        if (uri != null) {
            val picked = ContactsHelper.readPicked(context, uri)
            if (picked != null) viewModel.addContact(picked.name, picked.number)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Excluded Contacts") }, navigationIcon = { BackIcon(onBack) })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "Numbers on this list will NEVER receive an automatic SMS or WhatsApp message — for example family members, staff, or suppliers.",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BigActionButton("Pick from Contacts", modifier = Modifier.weight(1f)) { pickContact.launch(null) }
                BigActionButton("Add Manually", modifier = Modifier.weight(1f)) { showAddDialog = true }
            }

            if (contacts.isEmpty()) {
                Text("No excluded numbers yet.", style = MaterialTheme.typography.bodyMedium)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(contacts, key = { it.id }) { c ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(c.name, style = MaterialTheme.typography.bodyLarge)
                                Text(PhoneUtils.display(c.number), style = MaterialTheme.typography.bodyMedium)
                            }
                            Row {
                                Switch(checked = c.enabled, onCheckedChange = { viewModel.setEnabled(c.id, it) })
                                IconButton(onClick = { viewModel.delete(c.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExcludedDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, number ->
                viewModel.addContact(name, number)
                showAddDialog = false
            }
        )
    }

    LaunchedEffect(message) {
        // Message is shown inline via a Snackbar-less simple text under the fields is skipped for brevity;
        // the dialog and add-flow already give feedback. Clear after showing once.
        if (message != null) viewModel.clearMessage()
    }
}

@Composable
private fun AddExcludedDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add excluded number") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name (optional)") })
                OutlinedTextField(
                    value = number, onValueChange = { number = it },
                    label = { Text("Phone number") },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name, number) }, enabled = number.isNotBlank()) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun BackIcon(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}
