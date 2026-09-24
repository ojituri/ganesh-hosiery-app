package com.ganeshhosiery.autoreply.messaging

import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Talks ONLY to the official Meta WhatsApp Business Cloud API
 * (https://developers.facebook.com/docs/whatsapp/cloud-api).
 *
 * This app never automates the consumer WhatsApp app and never uses an unofficial library.
 * If WhatsApp is not configured, callers of this object simply never invoke it.
 */
object WhatsAppApi {

    data class Config(
        val phoneNumberId: String,
        val accessToken: String,
        val apiVersion: String,
        val templateName: String,
        val templateLanguage: String
    )

    data class WhatsAppResult(
        val success: Boolean,
        val userMessage: String?,
        val technicalDetail: String?
    )

    /** Sends the approved template message [templateName] with [variables] filling {{1}}, {{2}}... */
    fun sendTemplateMessage(
        config: Config,
        toNumber: String,
        variables: List<String>
    ): WhatsAppResult {
        return try {
            val url = URL(
                "https://graph.facebook.com/${config.apiVersion}/${config.phoneNumberId}/messages"
            )
            val body = buildTemplateBody(toNumber, config.templateName, config.templateLanguage, variables)
            postJson(url, config.accessToken, body)
        } catch (e: Exception) {
            WhatsAppResult(false, "Could not send the WhatsApp message.", e.toString())
        }
    }

    /** A very small "is this token/phone-number-id valid?" check, used by Test Mode / setup wizard. */
    fun verifyConnection(phoneNumberId: String, accessToken: String, apiVersion: String): WhatsAppResult {
        return try {
            val url = URL("https://graph.facebook.com/$apiVersion/$phoneNumberId?fields=verified_name")
            val connection = openGet(url, accessToken)
            val code = connection.responseCode
            val text = readBody(connection)
            connection.disconnect()
            if (code in 200..299) {
                WhatsAppResult(true, "WhatsApp connection looks good.", text)
            } else {
                WhatsAppResult(false, friendlyErrorFor(code, text), "HTTP $code: $text")
            }
        } catch (e: java.net.UnknownHostException) {
            WhatsAppResult(false, "No internet connection right now.", e.toString())
        } catch (e: Exception) {
            WhatsAppResult(false, "Could not reach WhatsApp. Please check the details and your internet.", e.toString())
        }
    }

    private fun buildTemplateBody(
        toNumber: String,
        templateName: String,
        language: String,
        variables: List<String>
    ): JSONObject {
        val root = JSONObject()
        root.put("messaging_product", "whatsapp")
        root.put("to", toNumber)
        root.put("type", "template")

        val template = JSONObject()
        template.put("name", templateName)
        template.put("language", JSONObject().put("code", language))

        if (variables.isNotEmpty()) {
            val parameters = JSONArray()
            for (v in variables) {
                parameters.put(JSONObject().put("type", "text").put("text", v))
            }
            val components = JSONArray()
            components.put(JSONObject().put("type", "body").put("parameters", parameters))
            template.put("components", components)
        }

        root.put("template", template)
        return root
    }

    private fun postJson(url: URL, accessToken: String, body: JSONObject): WhatsAppResult {
        val connection = (url.openConnection() as HttpsURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Content-Type", "application/json")
        }
        return try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
            val code = connection.responseCode
            val text = readBody(connection)
            if (code in 200..299) {
                WhatsAppResult(true, null, text)
            } else {
                WhatsAppResult(false, friendlyErrorFor(code, text), "HTTP $code: $text")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun openGet(url: URL, accessToken: String): HttpURLConnection {
        return (url.openConnection() as HttpsURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Authorization", "Bearer $accessToken")
        }
    }

    private fun readBody(connection: HttpURLConnection): String {
        return try {
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        } catch (e: Exception) {
            ""
        }
    }

    private fun friendlyErrorFor(code: Int, rawBody: String): String {
        val metaMessage = try {
            JSONObject(rawBody).optJSONObject("error")?.optString("message")
        } catch (e: Exception) {
            null
        }
        return when {
            code == 401 || code == 403 -> "WhatsApp access token is invalid or expired. Please update it in WhatsApp Setup."
            code == 400 && metaMessage?.contains("template", ignoreCase = true) == true ->
                "The WhatsApp message template name/language is not correct or not approved yet."
            code == 404 -> "WhatsApp Phone Number ID is not correct."
            code in 500..599 -> "WhatsApp's servers are having trouble right now. It will be tried again later."
            !metaMessage.isNullOrBlank() -> "WhatsApp error: $metaMessage"
            else -> "Could not send the WhatsApp message (code $code)."
        }
    }
}
