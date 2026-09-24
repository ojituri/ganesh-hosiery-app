package com.ganeshhosiery.autoreply.data

/** Factory settings. Nothing about the shop's address, phone or map link is invented here. */
object Defaults {

    val DEFAULT_MESSAGE: String = """
        🙏 Namaskar!

        Thank you for contacting {SHOP_NAME}.

        We deal in quality wholesale clothing and gowns.

        📍 Shop Location:
        {SHOP_ADDRESS}

        🗺 Google Maps:
        {MAP_LINK}

        📞 Contact:
        {SHOP_PHONE}

        We look forward to serving you. 😊
    """.trimIndent()

    fun template(channel: String): MessageTemplate = MessageTemplate(
        channel = channel,
        // SMS is on from the start. WhatsApp stays off until the shop configures it.
        enabled = channel == Channel.SMS,
        body = DEFAULT_MESSAGE
    )

    /** Words that can be typed inside a message. They are replaced automatically. */
    val PLACEHOLDERS: List<Pair<String, String>> = listOf(
        "{SHOP_NAME}" to "Shop name",
        "{SHOP_PHONE}" to "Shop phone",
        "{SHOP_ADDRESS}" to "Shop address",
        "{MAP_LINK}" to "Google Maps link",
        "{CUSTOMER_NAME}" to "Customer name",
        "{BUSINESS_TYPE}" to "Business type",
        "{BUSINESS_HOURS}" to "Business hours",
        "{WEBSITE}" to "Website",
        "{INSTAGRAM}" to "Instagram",
        "{EMAIL}" to "Email"
    )
}
