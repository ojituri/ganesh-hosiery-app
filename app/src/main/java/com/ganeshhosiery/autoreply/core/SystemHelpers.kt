package com.ganeshhosiery.autoreply.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SubscriptionManager

/** A contact from the phone book (one phone number). */
data class DeviceContact(val name: String, val number: String, val key: String)

object ContactsHelper {

    /** Name of the saved contact for this number, or null (also null when contacts permission is off). */
    fun lookupName(context: Context, number: String): String? {
        if (!Permissions.has(context, Permissions.CONTACTS)) return null
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Every phone number in the phone book, sorted by name, one entry per distinct number. */
    fun loadAll(context: Context): List<DeviceContact> {
        if (!Permissions.has(context, Permissions.CONTACTS)) return emptyList()
        val result = ArrayList<DeviceContact>()
        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC"
            )?.use { c ->
                while (c.moveToNext()) {
                    val name = c.getString(0)?.trim().orEmpty()
                    val number = c.getString(1)?.trim().orEmpty()
                    val key = PhoneUtils.matchKey(number)
                    if (key.length >= 7) {
                        result.add(DeviceContact(name.ifEmpty { "No name" }, number, key))
                    }
                }
            }
        } catch (e: Exception) {
            // Contacts unavailable: return what we have (possibly nothing).
        }
        return result.distinctBy { it.key }
    }

    /**
     * Reads one contact chosen in Android's own contact chooser.
     * That chooser gives temporary access, so this works even without the contacts permission.
     */
    fun readPicked(context: Context, uri: Uri): DeviceContact? {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null, null
            )?.use { c ->
                if (c.moveToFirst()) {
                    val name = c.getString(0)?.trim().orEmpty()
                    val number = c.getString(1)?.trim().orEmpty()
                    val key = PhoneUtils.matchKey(number)
                    if (key.length >= 7) DeviceContact(name.ifEmpty { "No name" }, number, key) else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}

object Connectivity {
    /** True when the phone has a working internet connection right now. */
    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            false
        }
    }
}

/** One SIM card slot of the phone. */
data class SimInfo(val subscriptionId: Int, val label: String)

object SimHelper {
    /** SIM cards currently in the phone. Empty if unknown or the Phone permission is off. */
    fun list(context: Context): List<SimInfo> {
        if (!Permissions.has(context, Permissions.PHONE_STATE)) return emptyList()
        return try {
            val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                ?: return emptyList()
            @Suppress("MissingPermission")
            val infos = sm.activeSubscriptionInfoList ?: return emptyList()
            infos.map { info ->
                val carrier = info.carrierName?.toString().orEmpty()
                val label = "SIM ${info.simSlotIndex + 1}" + if (carrier.isNotBlank()) " ($carrier)" else ""
                SimInfo(info.subscriptionId, label)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
