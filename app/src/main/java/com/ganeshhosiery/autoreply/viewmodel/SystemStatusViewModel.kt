package com.ganeshhosiery.autoreply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.core.Connectivity
import com.ganeshhosiery.autoreply.core.Permissions
import com.ganeshhosiery.autoreply.data.AppSettings
import com.ganeshhosiery.autoreply.data.SecureKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class SystemStatus(
    val phoneStateGranted: Boolean,
    val callLogGranted: Boolean,
    val smsGranted: Boolean,
    val contactsGranted: Boolean,
    val batteryUnrestricted: Boolean,
    val online: Boolean,
    val whatsappConfigured: Boolean
)

class SystemStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val app = GaneshApp.from(application)
    private val db = app.database

    val settings: StateFlow<AppSettings?> =
        db.settingsDao().observe()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun currentStatus(): SystemStatus {
        val context = getApplication<Application>()
        val s = settings.value
        val hasToken = app.secureStore.contains(SecureKeys.WA_TOKEN)
        return SystemStatus(
            phoneStateGranted = Permissions.has(context, Permissions.PHONE_STATE),
            callLogGranted = !Permissions.callLogNeededForNumber() || Permissions.has(context, Permissions.CALL_LOG),
            smsGranted = Permissions.has(context, Permissions.SEND_SMS),
            contactsGranted = Permissions.has(context, Permissions.CONTACTS),
            batteryUnrestricted = Permissions.isBatteryUnrestricted(context),
            online = Connectivity.isOnline(context),
            whatsappConfigured = hasToken && !s?.waPhoneNumberId.isNullOrBlank() && !s?.waTemplateName.isNullOrBlank()
        )
    }
}
