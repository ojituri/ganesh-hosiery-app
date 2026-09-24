package com.ganeshhosiery.autoreply.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ganeshhosiery.autoreply.core.Permissions
import com.ganeshhosiery.autoreply.ui.components.BigActionButton

@Composable
fun BackgroundReliabilityGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = { TopAppBar(title = { Text("Keep Working in Background") }, navigationIcon = { BackIcon(onBack) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Many Indian phone brands (Xiaomi/Redmi, Oppo, Vivo, OnePlus, Realme and others) aggressively stop apps running in the background " +
                    "to save battery. If that happens to this app, it will miss calls and stop replying. Doing the two steps below fixes this on almost every phone.",
                style = MaterialTheme.typography.bodyMedium
            )

            StepCard("1. Allow the app to ignore battery optimization") {
                BigActionButton("Open Battery Settings") { Permissions.requestIgnoreBatteryOptimizations(context) }
            }

            StepCard("2. Allow the app to Auto-start / run in background") {
                Text(
                    "On Xiaomi (MIUI): Security app → Permissions → Autostart → turn ON for this app.\n" +
                        "On Oppo/Realme (ColorOS): Settings → Battery → App Battery Management → find this app → allow background activity.\n" +
                        "On Vivo (FuntouchOS): iManager → App Manager → Autostart Manager → turn ON.\n" +
                        "On OnePlus: Settings → Battery → Battery Optimization → this app → Don't optimize.\n" +
                        "On Samsung: Settings → Apps → this app → Battery → Unrestricted.",
                    style = MaterialTheme.typography.bodyMedium
                )
                BigActionButton("Try to Open Auto-start Settings") { Permissions.openAutoStartSettings(context) }
                Text(
                    "If that button does nothing, please open the step manually using the phone's own Settings app as described above — every phone brand names this screen a little differently.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            StepCard("3. Don't force-stop or swipe away the app") {
                Text(
                    "Swiping the app away from Recent Apps can also stop it on some phones. If possible, avoid closing it from Recent Apps; " +
                        "just leave it running in the background.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun StepCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
