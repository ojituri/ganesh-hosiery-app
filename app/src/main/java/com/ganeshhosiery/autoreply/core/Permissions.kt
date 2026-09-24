package com.ganeshhosiery.autoreply.core

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/** Everything about Android permissions and system settings screens, in one place. */
object Permissions {

    const val PHONE_STATE = Manifest.permission.READ_PHONE_STATE
    const val CALL_LOG = Manifest.permission.READ_CALL_LOG
    const val SEND_SMS = Manifest.permission.SEND_SMS
    const val CONTACTS = Manifest.permission.READ_CONTACTS

    private const val PREFS = "permission_prefs"

    fun has(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /** Since Android 9 the caller's number is only given to apps that also have the call-log permission. */
    fun callLogNeededForNumber(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    /** Remember that we already showed the Android permission pop-up once. */
    fun markAsked(context: Context, permission: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("asked_$permission", true).apply()
    }

    private fun wasAsked(context: Context, permission: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("asked_$permission", false)

    /**
     * True when the permission is off and Android will no longer show the pop-up
     * (the user chose "Don't allow" twice, or Android blocks it for apps installed from a file).
     * In that case the only way is the app's settings page.
     */
    fun isPermanentlyDenied(activity: Activity?, permission: String): Boolean {
        if (activity == null) return false
        if (has(activity, permission)) return false
        if (!wasAsked(activity, permission)) return false
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    // ---------- Battery ----------

    fun isBatteryUnrestricted(context: Context): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } catch (e: Exception) {
            false
        }
    }

    // ---------- Settings screens ----------

    private fun startSafely(context: Context, intent: Intent): Boolean {
        return try {
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        startSafely(context, intent)
    }

    /** Shows Android's own "Allow app to always run in background?" question. */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        val direct = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        )
        if (!startSafely(context, direct)) {
            if (!startSafely(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))) {
                openAppSettings(context)
            }
        }
    }

    /** Tries the "auto-start" screens of common phone brands. Returns false if none could be opened. */
    fun openAutoStartSettings(context: Context): Boolean {
        val candidates = listOf(
            // Xiaomi / Redmi / POCO
            "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
            // Oppo / Realme (ColorOS)
            "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
            "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
            "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
            // Vivo
            "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
            // OnePlus
            "com.oneplus.security" to "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
            // Huawei / Honor
            "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
        )
        for ((pkg, cls) in candidates) {
            val intent = Intent().setComponent(ComponentName(pkg, cls))
            if (startSafely(context, intent)) return true
        }
        return false
    }
}

/** Finds the Activity behind a Compose LocalContext (needed to ask permissions). */
fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
