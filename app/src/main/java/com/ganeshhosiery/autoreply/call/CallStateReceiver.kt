package com.ganeshhosiery.autoreply.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Android sends this broadcast whenever a call starts ringing, is answered, or ends.
 * We act only on "ringing" - that is the one moment a caller's number is available.
 *
 * A BroadcastReceiver must finish quickly, so all the real work (checking the excluded
 * list, sending SMS, etc.) happens on a background coroutine started with goAsync(),
 * and every possible failure is caught so this receiver can never crash the phone's
 * dialer or the app.
 */
class CallStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state != TelephonyManager.EXTRA_STATE_RINGING) return

            // Only present when the app also holds READ_CALL_LOG (required since Android 9).
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            val pendingResult = goAsync()
            val appContext = context.applicationContext
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    AutoReplyEngine.processIncomingCall(appContext, number, isTest = false)
                } catch (e: Exception) {
                    // Whatever went wrong, the app must not crash because a phone call came in.
                } finally {
                    pendingResult.finish()
                }
            }
        } catch (e: Exception) {
            // Defensive outer guard: never let a phone-state broadcast crash the app.
        }
    }
}
