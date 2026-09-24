package com.ganeshhosiery.autoreply.core

/**
 * Phone number helpers, tuned for India (+91) but safe for other countries.
 *
 * The same person can appear as +919876543210, 09876543210, 98765 43210 or 91-98765-43210.
 * [matchKey] turns all of these into the same text so they can be compared.
 */
object PhoneUtils {

    private const val COUNTRY_CODE = "91"

    /** Keeps only digits and a leading "+". */
    private fun clean(raw: String): String {
        val trimmed = raw.trim()
        val digits = trimmed.filter { it.isDigit() }
        return if (trimmed.startsWith("+")) "+$digits" else digits
    }

    /**
     * Canonical text for comparing numbers. Empty string means "no usable number".
     *  - Indian numbers become their 10 digits: 9876543210
     *  - Other international numbers keep their country code: +14155550100
     */
    fun matchKey(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var c = clean(raw)
        if (c.startsWith("00")) c = "+" + c.drop(2)
        if (c.isEmpty() || c == "+") return ""

        if (c.startsWith("+")) {
            val d = c.drop(1)
            if (d.isEmpty()) return ""
            return if (d.startsWith(COUNTRY_CODE) && d.length == 12) d.substring(2) else "+$d"
        }

        return when {
            c.length == 13 && c.startsWith("0$COUNTRY_CODE") -> c.substring(3)
            c.length == 12 && c.startsWith(COUNTRY_CODE) -> c.substring(2)
            c.length == 11 && c.startsWith("0") -> c.substring(1)
            else -> c
        }
    }

    /** True for a normal Indian mobile number (10 digits starting with 6, 7, 8 or 9). */
    fun isIndianMobile(key: String): Boolean =
        key.length == 10 && key.all { it.isDigit() } && key[0] in '6'..'9'

    /** Number in +91XXXXXXXXXX form, or null if an SMS cannot be sent to it (landline, too short...). */
    fun toSmsAddress(raw: String?): String? {
        val key = matchKey(raw)
        if (key.isEmpty()) return null
        if (isIndianMobile(key)) return "+$COUNTRY_CODE$key"
        if (key.startsWith("+") && key.length in 9..16) return key
        return null
    }

    /** Digits with country code and without "+" (format required by WhatsApp), or null. */
    fun toWhatsAppNumber(raw: String?): String? = toSmsAddress(raw)?.removePrefix("+")

    /** Friendly text for screens: "+91 98765 43210". */
    fun display(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown number"
        val key = matchKey(raw)
        return if (isIndianMobile(key)) "+91 ${key.substring(0, 5)} ${key.substring(5)}" else raw.trim()
    }
}
