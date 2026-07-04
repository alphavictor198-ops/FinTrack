package com.example.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"

    // OkHttpClient with 60-second timeouts as mandated by the skill instructions
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    suspend fun parseTransaction(
        inputText: String?,
        bitmap: Bitmap?,
        userApiKey: String? = null
    ): ParsedTransaction? = withContext(Dispatchers.IO) {
        val apiKey = when {
            !userApiKey.isNullOrBlank() -> userApiKey
            BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (apiKey.isEmpty()) {
            Log.e(TAG, "Gemini API key is empty.")
            return@withContext null
        }

        val systemPrompt = """
            You are a financial receipt and SMS transaction parser. Analyze the provided text or image of a receipt, invoice, bill, or debit/credit SMS alert.
            Extract the transaction details and return them strictly as a valid JSON object with the following fields:
            {
              "title": "A short, clean merchant/transaction name (e.g., 'Target', 'Starbucks', 'Employer Name')",
              "amount": 12.34,
              "category": "One of: Food, Shopping, Utilities, Entertainment, Transport, Other",
              "isIncome": false,
              "notes": "A brief, friendly summary of what was purchased or extracted (e.g. 'Lunch and drinks', 'Monthly electricity bill', 'Weekly payroll deposit')"
            }
            Do NOT include any markdown block fences, conversational text, or explanations. Only return the raw JSON object.
        """.trimIndent()

        try {
            // Build request parts JSON
            val partsArray = JSONArray()

            // Text part
            val textPrompt = if (!inputText.isNullOrBlank()) {
                "Please parse this transaction text:\n$inputText"
            } else {
                "Please parse the attached transaction image."
            }
            partsArray.put(JSONObject().put("text", textPrompt))

            // Image part if present
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                val inlineDataObj = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Image)
                }
                partsArray.put(JSONObject().put("inlineData", inlineDataObj))
            }

            // Request body JSON
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().put("parts", partsArray)))
                put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: ${response.code} - ${response.message}")
                    return@withContext null
                }

                val responseBody = response.body?.string() ?: return@withContext null
                Log.d(TAG, "Response: $responseBody")

                // Parse the response
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                val content = candidates?.optJSONObject(0)?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val textResponse = parts?.optJSONObject(0)?.optString("text") ?: return@withContext null

                // Parse extracted JSON
                val resultJson = JSONObject(textResponse.trim())
                return@withContext ParsedTransaction(
                    title = resultJson.optString("title", "Unknown Merchant"),
                    amount = resultJson.optDouble("amount", 0.0),
                    category = resultJson.optString("category", "Other"),
                    isIncome = resultJson.optBoolean("isIncome", false),
                    notes = resultJson.optString("notes", "")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
            return@withContext null
        }
    }
}

data class ParsedTransaction(
    val title: String,
    val amount: Double,
    val category: String,
    val isIncome: Boolean,
    val notes: String
)
