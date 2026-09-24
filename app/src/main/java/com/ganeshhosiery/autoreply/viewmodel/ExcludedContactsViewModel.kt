package com.ganeshhosiery.autoreply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.core.PhoneUtils
import com.ganeshhosiery.autoreply.data.ExcludedContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExcludedContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GaneshApp.from(application).database

    val contacts: StateFlow<List<ExcludedContact>> =
        db.excludedDao().observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() { _message.value = null }

    fun addContact(name: String, number: String) {
        val key = PhoneUtils.matchKey(number)
        if (key.length < 7) {
            _message.value = "That doesn't look like a valid phone number."
            return
        }
        viewModelScope.launch {
            val id = db.excludedDao().insert(
                ExcludedContact(name = name.ifBlank { "Unnamed" }, number = number.trim(), matchKey = key)
            )
            _message.value = if (id == -1L) "This number is already on the list." else "Added to excluded list."
        }
    }

    fun setEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch { db.excludedDao().setEnabled(id, enabled) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { db.excludedDao().delete(id) }
    }
}
