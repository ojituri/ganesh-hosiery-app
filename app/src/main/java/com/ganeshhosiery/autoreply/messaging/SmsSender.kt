package com.ganeshhosiery.autoreply.messaging

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import com.ganeshhosiery.autoreply.data.MsgStatus

/**
 * Sends a plain SMS through the phone's own SIM. This does NOT require the app to be the
 * phone's default SMS app - any app holding the SEND_SMS permission can call this.
 *
 * Long messages are split into parts automatically. We track how many parts succeeded /
 * failed so the history screen can show "Sent" only when every part actually went out.
 */
object SmsSender {

    const val EXTRA_MESSAGE_RECORD_ID = "extra_message_record_id"
    const val EXTRA_PART_INDEX = "extra_part_index"
    const val ACTION_SMS_SENT = "com.ganeshhosiery.autoreply.ACTION_SMS_SENT"

    /**
     * Attempts to send [body] to [address]. Returns a user-friendly error immediately if
     * sending could not even be started (e.g. bad number, no telephony, SmsManager missing).
     * The final SENT / FAILED status arrives later via [SmsStatusReceiver].
     */
    fun send(
        context: Context,
        messageRecordId: Long,
        address: String,
        body: String,
        subscriptionId: Int
    ): SendAttemptResult {
        return try {
            val smsManager = getManager(context, subscriptionId)
                ?: return SendAttemptResult(
                    started = false,
                    partsTotal = 1,
                    userMessage = "This phone has no way to send SMS (no SIM / telephony found).",
                    technicalDetail = "SmsManager unavailable"
                )

            val parts = smsManager.divideMessage(body)
            val partsTotal = parts.size.coerceAtLeast(1)
            val pendingIntents = ArrayList<PendingIntent>(partsTotal)

            for (i in 0 until partsTotal) {
                val intent = Intent(context, SmsStatusReceiver::class.java).apply {
                    action = ACTION_SMS_SENT
                    putExtra(EXTRA_MESSAGE_RECORD_ID, messageRecordId)
                    putExtra(EXTRA_PART_INDEX, i)
                }
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
                // requestCode mixes the message id and part index so every part gets its own PendingIntent.
                val requestCode = ((messageRecordId % 100000) * 10 + i).toInt()
                pendingIntents.add(
                    PendingIntent.getBroadcast(context, requestCode, intent, flags)
                )
            }

            smsManager.sendMultipartTextMessage(address, null, parts, pendingIntents, null)
            SendAttemptResult(started = true, partsTotal = partsTotal, userMessage = null, technicalDetail = null)
        } catch (e: SecurityException) {
            SendAttemptResult(
                started = false, partsTotal = 1,
                userMessage = "SMS permission is not allowed. Please grant SMS permission in the app.",
                technicalDetail = e.toString()
            )
        } catch (e: IllegalArgumentException) {
            SendAttemptResult(
                started = false, partsTotal = 1,
                userMessage = "The phone number could not be used to send an SMS.",
                technicalDetail = e.toString()
            )
        } catch (e: Exception) {
            SendAttemptResult(
                started = false, partsTotal = 1,
                userMessage = "Could not send the SMS. Please check SMS permission and try again.",
                technicalDetail = e.toString()
            )
        }
    }

    private fun getManager(context: Context, subscriptionId: Int): SmsManager? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val base = context.getSystemService(SmsManager::class.java) ?: return null
                if (subscriptionId >= 0) base.createForSubscriptionId(subscriptionId) else base
            } else {
                @Suppress("DEPRECATION")
                if (subscriptionId >= 0) SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                else SmsManager.getDefault()
            }
        } catch (e: Exception) {
            null
        }
    }

    data class SendAttemptResult(
        val started: Boolean,
        val partsTotal: Int,
        val userMessage: String?,
        val technicalDetail: String?
    ) {
        val status: String get() = if (started) MsgStatus.PENDING else MsgStatus.FAILED
    }
}
