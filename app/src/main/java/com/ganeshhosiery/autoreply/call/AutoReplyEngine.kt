package com.ganeshhosiery.autoreply.call

import android.content.Context
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.core.ContactsHelper
import com.ganeshhosiery.autoreply.core.Permissions
import com.ganeshhosiery.autoreply.core.PhoneUtils
import com.ganeshhosiery.autoreply.core.TemplateEngine
import com.ganeshhosiery.autoreply.core.TimeUtils
import com.ganeshhosiery.autoreply.data.AppSettings
import com.ganeshhosiery.autoreply.data.CallRecord
import com.ganeshhosiery.autoreply.data.Channel
import com.ganeshhosiery.autoreply.data.MessageRecord
import com.ganeshhosiery.autoreply.data.MsgStatus
import com.ganeshhosiery.autoreply.data.SecureKeys
import com.ganeshhosiery.autoreply.messaging.SmsSender
import com.ganeshhosiery.autoreply.messaging.WhatsAppWorker

/**
 * Decides what should happen for one incoming call and does it: check the excluded list,
 * decide SMS / WhatsApp, write everything to the local database. Used both by the real
 * call detector and by Test Mode (with [isTest] = true so test calls never count towards
 * limits or appear in real statistics).
 *
 * Every step is wrapped so that a single failure (bad number, missing permission, no
 * internet) only affects that one step - it never stops the rest of the app from working,
 * and it never crashes.
 */
object AutoReplyEngine {

    suspend fun processIncomingCall(context: Context, rawNumber: String?, isTest: Boolean = false): Long {
        val app = GaneshApp.from(context)
        val db = app.database

        val settings = db.settingsDao().get() ?: AppSettings()
        val matchKey = PhoneUtils.matchKey(rawNumber)
        val now = System.currentTimeMillis()

        if (!isTest && !settings.autoReplyEnabled) return -1L

        // ---- Duplicate call-event protection (the same ring can be broadcast more than once) ----
        if (!isTest && matchKey.isNotEmpty() && settings.duplicateWindowSeconds > 0) {
            val since = now - settings.duplicateWindowSeconds * 1000L
            if (db.callDao().countRecent(matchKey, since) > 0) return -1L
        }

        val callerName = if (Permissions.has(context, Permissions.CONTACTS)) {
            ContactsHelper.lookupName(context, rawNumber.orEmpty())
        } else null

        // ---- Excluded contacts: do absolutely nothing further for these numbers ----
        val isExcluded = matchKey.isNotEmpty() && db.excludedDao().countEnabled(matchKey) > 0
        if (isExcluded) {
            if (settings.recordExcludedCalls) {
                db.callDao().insert(
                    CallRecord(
                        number = rawNumber.orEmpty(),
                        matchKey = matchKey,
                        callerName = callerName,
                        timestamp = now,
                        isExcluded = true,
                        smsStatus = MsgStatus.OFF,
                        whatsappStatus = MsgStatus.OFF,
                        note = "This number is on the excluded list. No message was sent.",
                        isTest = isTest
                    )
                )
            }
            return -1L
        }

        // ---- Repeat-caller suppression: avoid messaging the same person too often ----
        var repeatNote: String? = null
        if (!isTest && matchKey.isNotEmpty() && settings.repeatCallerMinutes > 0) {
            val since = now - settings.repeatCallerMinutes * 60_000L
            if (db.callDao().countMessagedSince(matchKey, since) > 0) {
                repeatNote = "Already messaged this number recently, so no message was sent this time."
            }
        }

        val callRecordId = db.callDao().insert(
            CallRecord(
                number = rawNumber.orEmpty(),
                matchKey = matchKey,
                callerName = callerName,
                timestamp = now,
                isExcluded = false,
                note = repeatNote,
                isTest = isTest
            )
        )

        if (repeatNote != null) {
            db.callDao().setSms(callRecordId, MsgStatus.OFF, repeatNote)
            db.callDao().setWhatsApp(callRecordId, MsgStatus.OFF, repeatNote)
            return callRecordId
        }

        val shop = db.shopDao().get() ?: com.ganeshhosiery.autoreply.data.ShopDetails()
        val placeholderValues = TemplateEngine.valuesFor(shop, callerName)

        handleSms(context, callRecordId, rawNumber, placeholderValues, settings, isTest)
        handleWhatsApp(context, callRecordId, rawNumber, placeholderValues, settings, isTest)

        return callRecordId
    }

    private suspend fun handleSms(
        context: Context,
        callRecordId: Long,
        rawNumber: String?,
        values: Map<String, String>,
        settings: AppSettings,
        isTest: Boolean
    ) {
        val app = GaneshApp.from(context)
        val db = app.database
        val template = db.templateDao().get(Channel.SMS)

        if (template == null || !template.enabled) {
            db.callDao().setSms(callRecordId, MsgStatus.OFF, null)
            return
        }

        if (!Permissions.has(context, Permissions.SEND_SMS)) {
            db.callDao().setSms(callRecordId, MsgStatus.FAILED, "SMS permission is not granted.")
            return
        }

        val address = PhoneUtils.toSmsAddress(rawNumber)
        if (address == null) {
            db.callDao().setSms(callRecordId, MsgStatus.FAILED, "This number cannot receive an SMS.")
            return
        }

        if (!isTest && settings.dailySmsLimit > 0) {
            val sentToday = db.callDao().countSmsSince(TimeUtils.startOfToday())
            if (sentToday >= settings.dailySmsLimit) {
                db.callDao().setSms(callRecordId, MsgStatus.FAILED, "Today's automatic SMS limit was reached.")
                return
            }
        }

        val body = TemplateEngine.render(template.body, values)
        val messageId = db.messageDao().insert(
            MessageRecord(callRecordId = callRecordId, channel = Channel.SMS, toNumber = address, body = body)
        )

        val result = SmsSender.send(context, messageId, address, body, settings.smsSubscriptionId)
        db.messageDao().update(
            db.messageDao().get(messageId)!!.copy(
                status = result.status,
                partsTotal = result.partsTotal,
                userMessage = result.userMessage,
                technicalDetail = result.technicalDetail
            )
        )
        db.callDao().setSms(callRecordId, result.status, result.userMessage)
    }

    private suspend fun handleWhatsApp(
        context: Context,
        callRecordId: Long,
        rawNumber: String?,
        values: Map<String, String>,
        settings: AppSettings,
        isTest: Boolean
    ) {
        val app = GaneshApp.from(context)
        val db = app.database
        val template = db.templateDao().get(Channel.WHATSAPP)

        if (template == null || !template.enabled) {
            db.callDao().setWhatsApp(callRecordId, MsgStatus.OFF, null)
            return
        }

        val token = app.secureStore.getString(SecureKeys.WA_TOKEN)
        val configured = !token.isNullOrBlank() &&
            settings.waPhoneNumberId.isNotBlank() &&
            settings.waTemplateName.isNotBlank()

        if (!configured) {
            db.callDao().setWhatsApp(callRecordId, MsgStatus.NOT_CONFIGURED, "WhatsApp Business API is not fully set up yet.")
            return
        }

        val number = PhoneUtils.toWhatsAppNumber(rawNumber)
        if (number == null) {
            db.callDao().setWhatsApp(callRecordId, MsgStatus.FAILED, "This number cannot receive a WhatsApp message.")
            return
        }

        // Variables: same message text as SMS, rendered then split into WhatsApp-safe template variables.
        val bodyText = TemplateEngine.render(template.body, values)
        val variable = TemplateEngine.sanitizeForWhatsAppVariable(bodyText)

        val messageId = db.messageDao().insert(
            MessageRecord(callRecordId = callRecordId, channel = Channel.WHATSAPP, toNumber = number, body = variable)
        )
        db.callDao().setWhatsApp(callRecordId, MsgStatus.PENDING, null)
        WhatsAppWorker.enqueue(context, messageId)
    }
}
