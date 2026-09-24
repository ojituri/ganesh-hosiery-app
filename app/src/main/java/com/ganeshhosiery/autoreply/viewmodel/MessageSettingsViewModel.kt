package com.ganeshhosiery.autoreply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.core.TemplateEngine
import com.ganeshhosiery.autoreply.data.Channel
import com.ganeshhosiery.autoreply.data.Defaults
import com.ganeshhosiery.autoreply.data.MessageTemplate
import com.ganeshhosiery.autoreply.data.ShopDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessageSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GaneshApp.from(application).database

    val smsTemplate: StateFlow<MessageTemplate?> =
        db.templateDao().observe(Channel.SMS)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val whatsappTemplate: StateFlow<MessageTemplate?> =
        db.templateDao().observe(Channel.WHATSAPP)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val shop: StateFlow<ShopDetails?> =
        db.shopDao().observe()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Renders any draft text against the shop's real details, for a live "what the customer sees" preview. */
    fun preview(draftText: String): String {
        val s = shop.value ?: ShopDetails()
        return TemplateEngine.render(draftText, TemplateEngine.valuesFor(s, "Rohit"))
    }

    fun save(channel: String, enabled: Boolean, body: String) {
        viewModelScope.launch {
            db.templateDao().upsert(MessageTemplate(channel = channel, enabled = enabled, body = body))
        }
    }

    fun resetToDefault(channel: String) {
        viewModelScope.launch { db.templateDao().upsert(Defaults.template(channel)) }
    }

    val placeholders = Defaults.PLACEHOLDERS
}
