package com.ganeshhosiery.autoreply.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Which messaging channel a template / message belongs to. */
object Channel {
    const val SMS = "SMS"
    const val WHATSAPP = "WHATSAPP"
}

/** What happened to the phone call itself. */
object CallStatus {
    const val RINGING = "RINGING"
    const val ANSWERED = "ANSWERED"
    const val MISSED = "MISSED"
}

/** Status of an SMS or WhatsApp message. Stored as text in the database. */
object MsgStatus {
    const val PENDING = "PENDING"
    const val SENT = "SENT"
    const val FAILED = "FAILED"
    const val OFF = "OFF"
    const val NOT_SENT = "NOT_SENT"
    const val NOT_CONFIGURED = "NOT_CONFIGURED"
}

/** A phone number that must never receive an automatic message. */
@Entity(
    tableName = "excluded_contacts",
    indices = [Index(value = ["matchKey"], unique = true)]
)
data class ExcludedContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Number exactly as the user typed / picked it. */
    val number: String,
    /** Normalised number used for comparing (see PhoneUtils.matchKey). */
    val matchKey: String,
    val enabled: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)

/** One incoming call handled (or deliberately ignored) by the app. */
@Entity(
    tableName = "call_records",
    indices = [Index(value = ["matchKey"]), Index(value = ["timestamp"])]
)
data class CallRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val matchKey: String,
    val callerName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val callStatus: String = CallStatus.RINGING,
    val isExcluded: Boolean = false,
    val smsStatus: String = MsgStatus.NOT_SENT,
    val smsNote: String? = null,
    val whatsappStatus: String = MsgStatus.NOT_SENT,
    val whatsappNote: String? = null,
    val note: String? = null,
    val isTest: Boolean = false
)

/** One SMS / WhatsApp sending attempt, with technical details kept for debugging. */
@Entity(
    tableName = "message_records",
    indices = [Index(value = ["callRecordId"])]
)
data class MessageRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 0 when the message is not linked to a call (for example a test SMS). */
    val callRecordId: Long = 0,
    val channel: String,
    val toNumber: String,
    val body: String,
    val status: String = MsgStatus.PENDING,
    val userMessage: String? = null,
    val technicalDetail: String? = null,
    val partsTotal: Int = 1,
    val partsOk: Int = 0,
    val partsFailed: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/** Shop information. Only one row (id = 1). Address / phone / map link are intentionally blank. */
@Entity(tableName = "shop_details")
data class ShopDetails(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "Ganesh Hosiery",
    val businessType: String = "Wholesale Clothing & Gowns",
    val phone: String = "",
    val address: String = "",
    val mapLink: String = "",
    val description: String = "",
    val businessHours: String = "",
    val website: String = "",
    val instagram: String = "",
    val email: String = ""
)

/** App settings. Only one row (id = 1). The WhatsApp access token is NOT here: it is kept encrypted (SecureStore). */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val autoReplyEnabled: Boolean = true,
    val recordExcludedCalls: Boolean = true,
    /** Same caller + same call within this many seconds = same event, handled only once. */
    val duplicateWindowSeconds: Int = 30,
    /** Do not message the same number again within this many minutes (0 = message every call). */
    val repeatCallerMinutes: Int = 60,
    /** Safety limit for automatic SMS per day (0 = no limit). */
    val dailySmsLimit: Int = 200,
    /** -1 = phone's default SIM for SMS. */
    val smsSubscriptionId: Int = -1,
    val setupCompleted: Boolean = false,
    val waPhoneNumberId: String = "",
    val waBusinessAccountId: String = "",
    val waApiVersion: String = "v25.0",
    val waTemplateName: String = "",
    val waTemplateLanguage: String = "en",
    /** One value per line; each line fills {{1}}, {{2}}... of the WhatsApp template. Placeholders allowed. */
    val waTemplateVariables: String = "",
    /** True after a successful connection check or test message. */
    val waVerified: Boolean = false
)

/** Editable message text for a channel (SMS or WhatsApp) and whether that channel is switched on. */
@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey val channel: String,
    val enabled: Boolean,
    val body: String
)

/** Numbers shown on the dashboard. Not a table: filled from a query. */
data class DayStats(
    val calls: Int = 0,
    val smsSent: Int = 0,
    val whatsappSent: Int = 0,
    val excludedCount: Int = 0,
    val failed: Int = 0
)
