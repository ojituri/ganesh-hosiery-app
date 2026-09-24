package com.ganeshhosiery.autoreply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.data.ShopDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GaneshApp.from(application).database

    val shop: StateFlow<ShopDetails?> =
        db.shopDao().observe()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(shop: ShopDetails) {
        viewModelScope.launch { db.shopDao().upsert(shop) }
    }
}
