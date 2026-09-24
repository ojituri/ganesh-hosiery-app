package com.ganeshhosiery.autoreply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.call.AutoReplyEngine
import com.ganeshhosiery.autoreply.core.Permissions
import com.ganeshhosiery.autoreply.core.PhoneUtils
import com.ganeshhosiery.autoreply.core.TemplateEngine
import com.ganeshhosiery.autoreply.data.Channel
import com.ganeshhosiery.autoreply.data.MessageRecord
import com.ganeshhosiery.autoreply.data.SecureKeys
import com.ganeshhosiery.autoreply.data.ShopDetails
import com.ganeshhosiery.autoreply.messaging.SmsSender
import com.ganeshhosiery.autoreply.messaging.WhatsAppApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestModeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = GaneshApp.from(application)
    private val db = app.database

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage

    fun clearResult() { _resultMessage.value = null }

    fun sendTestSms(rawNumber: String) {
        val address = PhoneUtils.toSmsAddress(rawNumber)
        if (address == null) {
            _resultMessage.value = "That doesn't look like a number an SMS can be sent to."
            return
        }
        if (!Permissions.has(getApplication<Application>(), Permissions.SEND_SMS)) {
            _resultMessage.value = "SMS permission is not granted yet. Please grant it first."
            return
        }
        _busy.value = true
        viewModelScope.launch {
            val shop = db.shopDao().get() ?: ShopDetails()
            val template = db.templateDao().get(Channel.SMS)
            val body = TemplateEngine.render(
                template?.body ?: com.ganeshhosiery.autoreply.data.Defaults.DEFAULT_MESSAGE,
                TemplateEngine.valuesFor(shop, "Test Customer")
            )
            val messageId = db.messageDao().insert(
                MessageRecord(callRecordId = 0, channel = Channel.SMS, toNumber = address, body = body)
            )
            val result = withContext(Dispatchers.IO) {
                SmsSender.send(getApplication<Application>(), messageId, address, body, -1)
            }
            _busy.value = false
            _resultMessage.value = if (result.started) {
                "Test SMS sent to $address. Check that phone for the message."
            } else {
                result.userMessage ?: "The test SMS could not be sent."
            }
        }
    }

    fun sendTestWhatsApp(rawNumber: String) {
        val number = PhoneUtils.toWhatsAppNumber(rawNumber)
        if (number == null) {
            _resultMessage.value = "That doesn't look like a valid WhatsApp number."
            return
        }
        _busy.value = true
        viewModelScope.launch {
            val settings = db.settingsDao().get()
            val token = app.secureStore.getString(SecureKeys.WA_TOKEN)
            if (settings == null || token.isNullOrBlank() || settings.waPhoneNumberId.isBlank() || settings.waTemplateName.isBlank()) {
                _busy.value = false
                _resultMessage.value = "WhatsApp is not fully set up yet. Please finish WhatsApp Setup first."
                return@launch
            }
            val shop = db.shopDao().get() ?: ShopDetails()
            val template = db.templateDao().get(Channel.WHATSAPP)
            val body = TemplateEngine.render(
                template?.body ?: com.ganeshhosiery.autoreply.data.Defaults.DEFAULT_MESSAGE,
                TemplateEngine.valuesFor(shop, "Test Customer")
            )
            val variable = TemplateEngine.sanitizeForWhatsAppVariable(body)
            val config = WhatsAppApi.Config(
                phoneNumberId = settings.waPhoneNumberId,
                accessToken = token,
                apiVersion = settings.waApiVersion.ifBlank { "v25.0" },
                templateName = settings.waTemplateName,
                templateLanguage = settings.waTemplateLanguage.ifBlank { "en" }
            )
            val result = withContext(Dispatchers.IO) {
                WhatsAppApi.sendTemplateMessage(config, number, listOf(variable))
            }
            _busy.value = false
            _resultMessage.value = result.userMessage
                ?: if (result.success) "Test WhatsApp message sent." else "The test WhatsApp message could not be sent."
        }
    }

    /** Runs the exact same logic as a real incoming call, marked as a test so it never affects real stats/limits. */
    fun simulateIncomingCall(rawNumber: String) {
        _busy.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                AutoReplyEngine.processIncomingCall(getApplication<Application>(), rawNumber, isTest = true)
            }
            _busy.value = false
            _resultMessage.value = "Simulated call handled. Check Call History (test entries are marked) to see the result."
        }
    }
}
