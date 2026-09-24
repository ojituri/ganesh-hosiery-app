package com.ganeshhosiery.autoreply.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ExcludedContact::class,
        CallRecord::class,
        MessageRecord::class,
        ShopDetails::class,
        AppSettings::class,
        MessageTemplate::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun shopDao(): ShopDao
    abstract fun templateDao(): TemplateDao
    abstract fun excludedDao(): ExcludedContactDao
    abstract fun callDao(): CallRecordDao
    abstract fun messageDao(): MessageRecordDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ganesh_autoreply.db")
                .build()
    }
}
