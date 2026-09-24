package com.ganeshhosiery.autoreply

import android.app.Application
import com.ganeshhosiery.autoreply.data.AppDatabase
import com.ganeshhosiery.autoreply.data.SecureStore

/**
 * One database and one secure-storage object for the whole app, created once here
 * so every screen and background receiver talks to the exact same data.
 */
class GaneshApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var secureStore: SecureStore
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.build(this)
        secureStore = SecureStore(this)
    }

    companion object {
        fun from(context: android.content.Context): GaneshApp =
            context.applicationContext as GaneshApp
    }
}
