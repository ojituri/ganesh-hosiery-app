package com.ganeshhosiery.autoreply.messaging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.data.MsgStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Android calls this once per SMS part, with a result code telling us if that part
 * actually left the phone. Once every part has reported in, the message (and the
 * call record it belongs to) is marked Sent or Failed - never left as "Pending" forever.
 */
class SmsStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SmsSender.ACTION_SMS_SENT) return

        val messageRecordId = intent.getLongExtra(SmsSender.EXTRA_MESSAGE_RECORD_ID, -1L)
        if (messageRecordId < 0) return

        val ok = resultCode == android.app.Activity.RESULT_OK
        val technicalDetail = if (ok) null else describeError(resultCode)

        val pendingResult = goAsync()
        val app = GaneshApp.from(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = app.database
                val record = db.messageDao().get(messageRecordId)
                if (record != null) {
                    val newOk = record.partsOk + if (ok) 1 else 0
                    val newFailed = record.partsFailed + if (ok) 0 else 1
                    val allReported = (newOk + newFailed) >= record.partsTotal
                    val finalStatus = when {
                        !allReported -> MsgStatus.PENDING
                        newFailed == 0 -> MsgStatus.SENT
                        else -> MsgStatus.FAILED
                    }
                    val finalUserMessage = if (allReported && newFailed > 0) {
                        "Some parts of the SMS could not be delivered."
                    } else record.userMessage

                    db.messageDao().update(
                        record.copy(
                            partsOk = newOk,
                            partsFailed = newFailed,
                            status = finalStatus,
                            userMessage = finalUserMessage,
                            technicalDetail = technicalDetail ?: record.technicalDetail
                        )
                    )

                    if (allReported && record.callRecordId > 0) {
                        db.callDao().setSms(
                            record.callRecordId,
                            finalStatus,
                            if (finalStatus == MsgStatus.FAILED) "Could not deliver the SMS." else null
                        )
                    }
                }
            } catch (e: Exception) {
                // Never crash: worst case the message stays "Pending" and is visible as such.
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun describeError(resultCode: Int): String = when (resultCode) {
        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "RESULT_ERROR_GENERIC_FAILURE"
        SmsManager.RESULT_ERROR_NO_SERVICE -> "RESULT_ERROR_NO_SERVICE (no signal)"
        SmsManager.RESULT_ERROR_NULL_PDU -> "RESULT_ERROR_NULL_PDU"
        SmsManager.RESULT_ERROR_RADIO_OFF -> "RESULT_ERROR_RADIO_OFF (airplane mode?)"
        else -> "SMS result code $resultCode"
    }
}
