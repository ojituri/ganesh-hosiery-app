package com.ganeshhosiery.autoreply.core

import com.ganeshhosiery.autoreply.data.ShopDetails

/**
 * Replaces placeholders such as {SHOP_NAME} with the real shop details.
 * Both {SHOP_NAME} and [SHOP_NAME] styles are understood.
 *
 * Empty details are tidied away: a line whose placeholders are all empty is removed,
 * and a heading left with nothing below it (for example "Shop Location:") is removed too.
 * This way the customer never receives a message with empty gaps.
 */
object TemplateEngine {

    private val PLACEHOLDER = Regex("""[\{\[]([A-Z_]+)[\}\]]""")
    private val BLOCK_SPLIT = Regex("""\n[ \t]*\n""")

    fun valuesFor(shop: ShopDetails, customerName: String?): Map<String, String> = mapOf(
        "SHOP_NAME" to shop.shopName.trim(),
        "SHOP_PHONE" to shop.phone.trim(),
        "SHOP_ADDRESS" to shop.address.trim(),
        "MAP_LINK" to shop.mapLink.trim(),
        "CUSTOMER_NAME" to (customerName?.trim().takeUnless { it.isNullOrEmpty() } ?: "Customer"),
        "BUSINESS_TYPE" to shop.businessType.trim(),
        "BUSINESS_HOURS" to shop.businessHours.trim(),
        "WEBSITE" to shop.website.trim(),
        "INSTAGRAM" to shop.instagram.trim(),
        "EMAIL" to shop.email.trim()
    )

    fun render(template: String, values: Map<String, String>): String {
        val text = template.replace("\r\n", "\n")
        val outBlocks = ArrayList<String>()

        for (block in text.split(BLOCK_SPLIT)) {
            val keptLines = ArrayList<String>()
            var droppedAnyLine = false

            for (line in block.split("\n")) {
                var hasPlaceholder = false
                var allEmpty = true
                val rendered = PLACEHOLDER.replace(line) { m ->
                    val key = m.groupValues[1]
                    if (values.containsKey(key)) {
                        hasPlaceholder = true
                        val v = values[key].orEmpty()
                        if (v.isNotEmpty()) allEmpty = false
                        v
                    } else {
                        m.value // unknown word: leave exactly as typed
                    }
                }
                if (hasPlaceholder && allEmpty) {
                    droppedAnyLine = true
                } else {
                    keptLines.add(rendered.trimEnd())
                }
            }

            val meaningful = keptLines.filter { it.isNotBlank() }
            if (meaningful.isEmpty()) continue
            // A heading like "Shop Location:" with nothing left under it is useless: drop it.
            if (droppedAnyLine && meaningful.all { it.trimEnd().endsWith(":") }) continue

            outBlocks.add(keptLines.joinToString("\n").trim('\n'))
        }
        return outBlocks.joinToString("\n\n").trim()
    }

    /** Text for one WhatsApp template variable: WhatsApp does not allow line breaks or long runs of spaces. */
    fun sanitizeForWhatsAppVariable(text: String): String =
        text.replace(Regex("[\\r\\n\\t]+"), " ")
            .replace(Regex(" {2,}"), " ")
            .trim()
            .take(1000)
}
