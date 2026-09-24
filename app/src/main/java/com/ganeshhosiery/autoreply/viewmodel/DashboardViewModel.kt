package com.ganeshhosiery.autoreply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.core.TimeUtils
import com.ganeshhosiery.autoreply.data.AppSettings
import com.ganeshhosiery.autoreply.data.CallRecord
import com.ganeshhosiery.autoreply.data.DayStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GaneshApp.from(application).database

    val todayStats: StateFlow<DayStats> =
        db.callDao().observeStats(TimeUtils.startOfToday())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DayStats())

    val recentCalls: StateFlow<List<CallRecord>> =
        db.callDao().observeRecent()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings?> =
        db.settingsDao().observe()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setAutoReplyEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = db.settingsDao().get() ?: AppSettings()
            db.settingsDao().upsert(current.copy(autoReplyEnabled = enabled))
        }
    }
}
