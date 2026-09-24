package com.ganeshhosiery.autoreply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.core.SimHelper
import com.ganeshhosiery.autoreply.data.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GaneshApp.from(application).database

    val settings: StateFlow<AppSettings?> =
        db.settingsDao().observe()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun sims() = SimHelper.list(getApplication<Application>())

    fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = db.settingsDao().get() ?: AppSettings()
            db.settingsDao().upsert(transform(current))
        }
    }
}
