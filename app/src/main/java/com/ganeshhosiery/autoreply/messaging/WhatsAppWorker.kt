package com.ganeshhosiery.autoreply.messaging

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ganeshhosiery.autoreply.GaneshApp
import com.ganeshhosiery.autoreply.core.Connectivity
import com.ganeshhosiery.autoreply.data.MsgStatus
import com.ganeshhosiery.autoreply.data.SecureKeys
import java.util.concurrent.TimeUnit

/**
 * Sends one WhatsApp message in the background, with automatic retries if the phone is
 * offline or WhatsApp's servers are briefly unavailable.
 *
 * On purpose, this worker does NOT receive the access token as input data (WorkManager
 * keeps its inputs in its own on-disk database). Instead it re-reads the token from
 * [com.ganeshhosiery.autoreply.data.SecureStore] every time it runs.
 */
class WhatsAppWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val messageRecordId = inputData.getLong(KEY_MESSAGE_RECORD_ID, -1L)
        if (messageRecordId < 0) return Result.failure()

        val app = GaneshApp.from(applicationContext)
        val db = app.database
        val message = db.messageDao().get(messageRecordId) ?: return Result.failure()

        if (!Connectivity.isOnline(applicationContext)) {
            return if (runAttemptCount < MAX_ATTEMPTS) {
                markStatus(message.id, message.callRecordId, MsgStatus.PENDING, "Waiting for internet connection.", null)
                Result.retry()
            } else {
                markStatus(message.id, message.callRecordId, MsgStatus.FAILED, "No internet connection was found.", null)
                Result.failure()
            }
        }

        val settings = db.settingsDao().get()
        val token = app.secureStore.getString(SecureKeys.WA_TOKEN)

        if (settings == null || token.isNullOrBlank() || settings.waPhoneNumberId.isBlank() ||
            settings.waTemplateName.isBlank()
        ) {
            markStatus(message.id, message.callRecordId, MsgStatus.NOT_CONFIGURED, "WhatsApp is not fully set up yet.", null)
            return Result.failure()
        }

        val variables = if (message.body.isEmpty()) emptyList() else message.body.split(UNIT_SEPARATOR)

        val config = WhatsAppApi.Config(
            phoneNumberId = settings.waPhoneNumberId,
            accessToken = token,
            apiVersion = settings.waApiVersion.ifBlank { "v25.0" },
            templateName = settings.waTemplateName,
            templateLanguage = settings.waTemplateLanguage.ifBlank { "en" }
        )

        val result = WhatsAppApi.sendTemplateMessage(config, message.toNumber, variables)

        return if (result.success) {
            markStatus(message.id, message.callRecordId, MsgStatus.SENT, null, result.technicalDetail)
            Result.success()
        } else if (runAttemptCount < MAX_ATTEMPTS && isLikelyTemporary(result.technicalDetail)) {
            markStatus(message.id, message.callRecordId, MsgStatus.PENDING, result.userMessage, result.technicalDetail)
            Result.retry()
        } else {
            markStatus(message.id, message.callRecordId, MsgStatus.FAILED, result.userMessage, result.technicalDetail)
            Result.failure()
        }
    }

    private fun isLikelyTemporary(technicalDetail: String?): Boolean {
        val d = technicalDetail.orEmpty()
        return d.contains("HTTP 5") || d.contains("UnknownHostException") || d.contains("SocketTimeoutException")
    }

    private suspend fun markStatus(messageId: Long, callRecordId: Long, status: String, userMessage: String?, technicalDetail: String?) {
        val db = GaneshApp.from(applicationContext).database
        val record = db.messageDao().get(messageId) ?: return
        db.messageDao().update(record.copy(status = status, userMessage = userMessage ?: record.userMessage, technicalDetail = technicalDetail ?: record.technicalDetail))
        if (callRecordId > 0) {
            db.callDao().setWhatsApp(callRecordId, status, userMessage)
        }
    }

    companion object {
        private const val KEY_MESSAGE_RECORD_ID = "message_record_id"
        private const val MAX_ATTEMPTS = 4
        /** Separates WhatsApp template variables when several are stored in one text field. */
        const val UNIT_SEPARATOR = "\u001F"

        fun enqueue(context: Context, messageRecordId: Long) {
            val data = Data.Builder().putLong(KEY_MESSAGE_RECORD_ID, messageRecordId).build()
            val request = OneTimeWorkRequestBuilder<WhatsAppWorker>()
                .setInputData(data)
                .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "whatsapp_send_$messageRecordId",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
