package com.ganeshhosiery.autoreply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.data.AppSettings
import com.ganeshhosiery.autoreply.data.Channel
import com.ganeshhosiery.autoreply.data.Defaults
import com.ganeshhosiery.autoreply.data.ShopDetails
import kotlinx.coroutines.launch

class SetupWizardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GaneshApp.from(application).database

    fun ensureDefaultsExist() {
        viewModelScope.launch {
            if (db.shopDao().get() == null) db.shopDao().upsert(ShopDetails())
            if (db.settingsDao().get() == null) db.settingsDao().upsert(AppSettings())
            if (db.templateDao().get(Channel.SMS) == null) db.templateDao().upsert(Defaults.template(Channel.SMS))
            if (db.templateDao().get(Channel.WHATSAPP) == null) db.templateDao().upsert(Defaults.template(Channel.WHATSAPP))
        }
    }

    fun saveShop(shop: ShopDetails) {
        viewModelScope.launch { db.shopDao().upsert(shop) }
    }

    fun finishSetup() {
        viewModelScope.launch {
            val current = db.settingsDao().get() ?: AppSettings()
            db.settingsDao().upsert(current.copy(setupCompleted = true, autoReplyEnabled = true))
        }
    }
}
