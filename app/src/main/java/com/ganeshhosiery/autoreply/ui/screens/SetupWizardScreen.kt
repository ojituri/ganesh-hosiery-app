package com.ganeshhosiery.autoreply.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshhosiery.autoreply.core.Permissions
import com.ganeshhosiery.autoreply.data.ShopDetails
import com.ganeshhosiery.autoreply.ui.components.BigActionButton
import com.ganeshhosiery.autoreply.viewmodel.SetupWizardViewModel

private const val TOTAL_STEPS = 7

@Composable
fun SetupWizardScreen(onFinished: () -> Unit, viewModel: SetupWizardViewModel = viewModel()) {
    var step by remember { mutableIntStateOf(1) }
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.ensureDefaultsExist() }

    var shopName by remember { mutableStateOf("Ganesh Hosiery") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var mapLink by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    Scaffold(topBar = { TopAppBar(title = { Text("Setup — Step $step of $TOTAL_STEPS") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LinearProgressIndicator(progress = { step / TOTAL_STEPS.toFloat() }, modifier = Modifier.fillMaxWidth())

            when (step) {
                1 -> WizardStep(
                    title = "Welcome to Ganesh Hosiery Auto Reply",
                    body = "This app automatically sends customers an SMS (and optionally a WhatsApp message) when they call your shop, unless the caller is on your excluded list (family, staff, suppliers). Let's set it up in a few short steps."
                )
                2 -> {
                    WizardStep(
                        title = "Allow permissions",
                        body = "The app needs permission to see when the phone rings and to send SMS. Contacts permission is optional but lets the app show caller names."
                    )
                    BigActionButton("Grant Permissions") {
                        permissionLauncher.launch(
                            arrayOf(Permissions.PHONE_STATE, Permissions.CALL_LOG, Permissions.SEND_SMS, Permissions.CONTACTS)
                        )
                    }
                    Text(
                        "If this phone was NOT installed from the Play Store, Android may show \"blocked by restricted settings\" for some permissions. " +
                            "If that happens, open the app's page in Settings > Apps, tap the 3-dot menu, and choose \"Allow restricted settings\" - the full README explains this with pictures.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                3 -> {
                    WizardStep(title = "Your shop details", body = "Fill in what you'd like customers to see. Leave anything blank if you'd rather not mention it - nothing is invented for you.")
                    OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("Shop name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Shop address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone number to show customers") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mapLink, onValueChange = { mapLink = it }, label = { Text("Google Maps link (optional)") }, modifier = Modifier.fillMaxWidth())
                }
                4 -> WizardStep(
                    title = "Your automatic message",
                    body = "A ready-made SMS message is already set up for you. You can review and edit its wording any time from Message Settings on the main screen, with a live preview."
                )
                5 -> WizardStep(
                    title = "Excluded contacts",
                    body = "Add family, staff, or suppliers to the Excluded Contacts list so they never receive an automatic message. You can do this any time from the main screen — it's fine to skip it for now."
                )
                6 -> WizardStep(
                    title = "WhatsApp (optional)",
                    body = "If your shop has an official WhatsApp Business API account, you can connect it later from \"WhatsApp Business Setup\" on the main screen. This is completely optional and off by default."
                )
                7 -> WizardStep(
                    title = "One last thing: keep the app running",
                    body = "Some phone brands stop background apps to save battery. After finishing setup, please open \"Keep Working in Background\" from the main screen and follow the two quick steps shown there."
                )
            }

            Row(step, onBack = { step-- }, onNext = {
                if (step == 3) {
                    viewModel.saveShop(ShopDetails(shopName = shopName.ifBlank { "Ganesh Hosiery" }, address = address, phone = phone, mapLink = mapLink))
                }
                if (step < TOTAL_STEPS) step++ else {
                    viewModel.finishSetup()
                    onFinished()
                }
            })
        }
    }
}

@Composable
private fun WizardStep(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun Row(step: Int, onBack: () -> Unit, onNext: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (step > 1) {
            BigActionButton("Back", modifier = Modifier.weight(1f)) { onBack() }
        }
        BigActionButton(if (step < TOTAL_STEPS) "Next" else "Finish Setup", modifier = Modifier.weight(1f)) { onNext() }
    }
}
