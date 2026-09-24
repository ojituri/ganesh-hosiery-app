@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ganeshhosiery.autoreply.ui.screens

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshhosiery.autoreply.data.ShopDetails
import com.ganeshhosiery.autoreply.ui.components.BigActionButton
import com.ganeshhosiery.autoreply.viewmodel.ShopDetailsViewModel

@Composable
fun ShopDetailsScreen(onBack: () -> Unit, viewModel: ShopDetailsViewModel = viewModel()) {
    val shop by viewModel.shop.collectAsState()

    var shopName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var mapLink by remember { mutableStateOf("") }
    var businessHours by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(shop) {
        val s = shop ?: return@LaunchedEffect
        if (!loaded) {
            shopName = s.shopName; businessType = s.businessType; phone = s.phone; address = s.address
            mapLink = s.mapLink; businessHours = s.businessHours; website = s.website
            instagram = s.instagram; email = s.email
            loaded = true
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Shop Details") }, navigationIcon = { BackIcon(onBack) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "These details are used to fill in the automatic message. Leave anything blank if you don't want it mentioned.",
                style = MaterialTheme.typography.bodyMedium
            )
            Field("Shop name", shopName) { shopName = it }
            Field("Business type", businessType) { businessType = it }
            Field("Phone number to show customers", phone) { phone = it }
            Field("Shop address", address, singleLine = false) { address = it }
            Field("Google Maps link", mapLink) { mapLink = it }
            Field("Business hours", businessHours) { businessHours = it }
            Field("Website (optional)", website) { website = it }
            Field("Instagram (optional)", instagram) { instagram = it }
            Field("Email (optional)", email) { email = it }

            BigActionButton("Save") {
                viewModel.save(
                    ShopDetails(
                        shopName = shopName.ifBlank { "Ganesh Hosiery" },
                        businessType = businessType.ifBlank { "Wholesale Clothing & Gowns" },
                        phone = phone, address = address, mapLink = mapLink,
                        businessHours = businessHours, website = website, instagram = instagram, email = email
                    )
                )
                onBack()
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, singleLine: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth()
    )
}
