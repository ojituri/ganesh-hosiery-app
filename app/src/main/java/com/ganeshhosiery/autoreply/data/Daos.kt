package com.ganeshhosiery.autoreply.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observe(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun get(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettings)
}

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop_details WHERE id = 1")
    fun observe(): Flow<ShopDetails?>

    @Query("SELECT * FROM shop_details WHERE id = 1")
    suspend fun get(): ShopDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(shop: ShopDetails)
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM message_templates WHERE channel = :channel")
    fun observe(channel: String): Flow<MessageTemplate?>

    @Query("SELECT * FROM message_templates WHERE channel = :channel")
    suspend fun get(channel: String): MessageTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(template: MessageTemplate)
}

@Dao
interface ExcludedContactDao {
    @Query("SELECT * FROM excluded_contacts ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ExcludedContact>>

    @Query("SELECT COUNT(*) FROM excluded_contacts WHERE matchKey = :key AND enabled = 1")
    suspend fun countEnabled(key: String): Int

    /** Returns -1 when the number is already in the list. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: ExcludedContact): Long

    @Query("UPDATE excluded_contacts SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM excluded_contacts WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface CallRecordDao {
    @Insert
    suspend fun insert(record: CallRecord): Long

    @Query("SELECT * FROM call_records WHERE id = :id")
    suspend fun get(id: Long): CallRecord?

    @Query("SELECT * FROM call_records WHERE id = :id")
    fun observe(id: Long): Flow<CallRecord?>

    @Query("SELECT * FROM call_records ORDER BY timestamp DESC LIMIT 500")
    fun observeRecent(): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records WHERE isTest = 0 ORDER BY timestamp DESC LIMIT 1")
    fun observeLastReal(): Flow<CallRecord?>

    /** Same number seen recently? (duplicate call-event protection) */
    @Query(
        "SELECT COUNT(*) FROM call_records " +
            "WHERE matchKey = :key AND matchKey != '' AND isTest = 0 AND timestamp >= :since"
    )
    suspend fun countRecent(key: String, since: Long): Int

    /** Did we already send (or start sending) a message to this number recently? */
    @Query(
        "SELECT COUNT(*) FROM call_records " +
            "WHERE matchKey = :key AND matchKey != '' AND isTest = 0 AND timestamp >= :since " +
            "AND (smsStatus IN ('SENT','PENDING') OR whatsappStatus IN ('SENT','PENDING'))"
    )
    suspend fun countMessagedSince(key: String, since: Long): Int

    @Query(
        "SELECT COUNT(*) FROM call_records " +
            "WHERE isTest = 0 AND timestamp >= :since AND smsStatus IN ('SENT','PENDING')"
    )
    suspend fun countSmsSince(since: Long): Int

    @Query(
        "SELECT COUNT(*) AS calls, " +
            "COALESCE(SUM(CASE WHEN smsStatus = 'SENT' THEN 1 ELSE 0 END), 0) AS smsSent, " +
            "COALESCE(SUM(CASE WHEN whatsappStatus = 'SENT' THEN 1 ELSE 0 END), 0) AS whatsappSent, " +
            "COALESCE(SUM(CASE WHEN isExcluded = 1 THEN 1 ELSE 0 END), 0) AS excludedCount, " +
            "COALESCE(SUM(CASE WHEN smsStatus = 'FAILED' OR whatsappStatus = 'FAILED' THEN 1 ELSE 0 END), 0) AS failed " +
            "FROM call_records WHERE isTest = 0 AND timestamp >= :since"
    )
    fun observeStats(since: Long): Flow<DayStats>

    @Query("UPDATE call_records SET smsStatus = :status, smsNote = :note WHERE id = :id")
    suspend fun setSms(id: Long, status: String, note: String?)

    @Query("UPDATE call_records SET whatsappStatus = :status, whatsappNote = :note WHERE id = :id")
    suspend fun setWhatsApp(id: Long, status: String, note: String?)

    @Query("UPDATE call_records SET callStatus = :status WHERE id = :id")
    suspend fun setCallStatus(id: Long, status: String)

    @Query("DELETE FROM call_records")
    suspend fun clear()

    @Query("DELETE FROM call_records WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}

@Dao
interface MessageRecordDao {
    @Insert
    suspend fun insert(record: MessageRecord): Long

    @Update
    suspend fun update(record: MessageRecord)

    @Query("SELECT * FROM message_records WHERE id = :id")
    suspend fun get(id: Long): MessageRecord?

    @Query("SELECT * FROM message_records WHERE id = :id")
    fun observe(id: Long): Flow<MessageRecord?>

    @Query("DELETE FROM message_records")
    suspend fun clear()

    @Query("DELETE FROM message_records WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
