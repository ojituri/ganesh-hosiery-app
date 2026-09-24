package com.ganeshhosiery.autoreply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.data.AppSettings
import com.ganeshhosiery.autoreply.data.SecureKeys
import com.ganeshhosiery.autoreply.messaging.WhatsAppApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WhatsAppSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val app = GaneshApp.from(application)
    private val db = app.database

    val settings: StateFlow<AppSettings?> =
        db.settingsDao().observe()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _checking = MutableStateFlow(false)
    val checking: StateFlow<Boolean> = _checking

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage

    fun clearResult() { _resultMessage.value = null }

    fun hasSavedToken(): Boolean = app.secureStore.contains(SecureKeys.WA_TOKEN)

    fun save(
        phoneNumberId: String,
        businessAccountId: String,
        accessToken: String?,
        apiVersion: String,
        templateName: String,
        templateLanguage: String,
        templateVariables: String,
        enabled: Boolean
    ) {
        viewModelScope.launch {
            val current = db.settingsDao().get() ?: AppSettings()
            db.settingsDao().upsert(
                current.copy(
                    waPhoneNumberId = phoneNumberId.trim(),
                    waBusinessAccountId = businessAccountId.trim(),
                    waApiVersion = apiVersion.trim().ifBlank { "v25.0" },
                    waTemplateName = templateName.trim(),
                    waTemplateLanguage = templateLanguage.trim().ifBlank { "en" },
                    waTemplateVariables = templateVariables
                )
            )
            if (!accessToken.isNullOrBlank()) {
                app.secureStore.putString(SecureKeys.WA_TOKEN, accessToken.trim())
            }
            val template = db.templateDao().get(com.ganeshhosiery.autoreply.data.Channel.WHATSAPP)
            db.templateDao().upsert(
                (template ?: com.ganeshhosiery.autoreply.data.Defaults.template(com.ganeshhosiery.autoreply.data.Channel.WHATSAPP))
                    .copy(enabled = enabled)
            )
            _resultMessage.value = "WhatsApp settings saved."
        }
    }

    fun clearToken() {
        app.secureStore.remove(SecureKeys.WA_TOKEN)
        viewModelScope.launch {
            val current = db.settingsDao().get() ?: return@launch
            db.settingsDao().upsert(current.copy(waVerified = false))
        }
        _resultMessage.value = "The saved WhatsApp access token was removed."
    }

    fun testConnection(phoneNumberId: String, accessToken: String?, apiVersion: String) {
        val token = accessToken?.takeIf { it.isNotBlank() } ?: app.secureStore.getString(SecureKeys.WA_TOKEN)
        if (phoneNumberId.isBlank() || token.isNullOrBlank()) {
            _resultMessage.value = "Please fill in the Phone Number ID and Access Token first."
            return
        }
        _checking.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                WhatsAppApi.verifyConnection(phoneNumberId.trim(), token, apiVersion.trim().ifBlank { "v25.0" })
            }
            _checking.value = false
            _resultMessage.value = result.userMessage
            if (result.success) {
                val current = db.settingsDao().get() ?: AppSettings()
                db.settingsDao().upsert(current.copy(waVerified = true))
            }
        }
    }
}
