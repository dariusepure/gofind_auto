package com.dariusepure.autoadvisor.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.dariusepure.autoadvisor.util.DiagnosticUtils
import com.dariusepure.autoadvisor.domain.ScannedCarData
import com.dariusepure.autoadvisor.ui.cars.europeanBrands
import com.dariusepure.autoadvisor.ui.cars.globalBrands
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig,
    private val httpClient: HttpClient
) {

    init {
        remoteConfig.fetchAndActivate()
    }

    private val modelName: String
        get() = remoteConfig.getString("gemini_model_name").ifBlank { "gemini-1.5-flash" }

    private val temperature: Float
        get() = remoteConfig.getDouble("gemini_temperature").toFloat()

    private val systemPrompt: String
        get() = remoteConfig.getString("gemini_prompt")

    private val geminiApiKey: String
        get() = remoteConfig.getString("gemini_api_key")

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        allowSpecialFloatingPointValues = true
    }

    private val tag = "GeminiRepository"

    private suspend fun postGemini(request: GeminiRequest): GeminiResponse {
        val model = modelName
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=$geminiApiKey"
        val sha1 = DiagnosticUtils.getAppSignatureSha1(context, withColons = true)
        val packageName = context.packageName

        try {
            val response: io.ktor.client.statement.HttpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                header("X-Android-Package", packageName)
                header("X-Android-Cert", sha1)
                setBody(request)
            }
            
            if (response.status.value !in 200..299) {
                val errorBody = response.body<String>()
                Log.e(tag, "Gemini API Error (${response.status}): $errorBody")
                throw Exception("Gemini API Error ${response.status}: $errorBody")
            }

            return response.body()
        } catch (e: Exception) {
            Log.e(tag, "Gemini Post failed", e)
            throw e
        }
    }

    suspend fun scanDocument(uri: Uri, mimeType: String): Result<List<ScannedCarData>> {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("Could not read file")
            val base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)

            val prompt = """
                Extract technical details from this vehicle document.
                Analyze the document and look for these fields:
                - make, model, year (4 digits), fuelType, engineSize (cc), power (hp or kW).
                
                CRITICAL VALIDATION:
                1. Verify Year: must be a realistic year (e.g., 1900-2026).
                2. Verify Engine Size: must be in cubic centimeters (cc).
                3. Brand (make) MUST be returned in Title Case (e.g., "Bmw", "Volkswagen").
                4. NUMERIC FIELDS MUST contain ONLY the raw number, NO units.
                5. If a value is unreadable, illogical, or not found, return null for that field.
                
                Return ONLY a JSON ARRAY containing one object with these keys: 
                make, model, year, fuelType, engineSize, power, powerUnit.
                
                Standard fuelType: Petrol, Diesel, Electric, Hybrid, LPG.
                Standard powerUnit: 'hp'. If kW is found, convert to hp (kW * 1.36).
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = Blob(mimeType = mimeType, data = base64Data))
                        )
                    )
                ),
                generationConfig = GenerationConfig(temperature = temperature)
            )

            val response = postGemini(request)
            val fullText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                ?: throw Exception("Empty response from AI")

            val jsonText = extractJson(fullText)
            val data = if (jsonText.trim().startsWith("[")) {
                json.decodeFromString<List<ScannedCarData>>(jsonText)
            } else {
                listOf(json.decodeFromString<ScannedCarData>(jsonText))
            }
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDiagnosisResponse(
        prompt: String,
        carContext: String,
        maintenanceGoal: String,
        history: List<com.dariusepure.autoadvisor.ui.cars.ChatMessage>,
        language: String = "English"
    ): GeminiResponse {
        val validatedHistory = history
            .dropWhile { !it.isUser }
            .let { h ->
                if (h.isNotEmpty() && h.size % 2 != 0) h.dropLast(1) else h
            }
            .map {
                Content(
                    role = if (it.isUser) "user" else "model",
                    parts = listOf(Part(text = it.text))
                )
            }

        val greetingRule = if (validatedHistory.isEmpty()) {
            "Always start with a brief, friendly mechanic's greeting."
        } else {
            "Do not repeat the greeting. Answer directly."
        }

        val personaInstruction = "Act as GoFind Auto, a professional car mechanic. $greetingRule Use human-friendly terms only. Talk to the user as if you are their trusted advisor in a garage. If you see recent maintenance or relevant history, mention it naturally."
        val formattingInstruction = "STRICTLY PROHIBIT markdown bold (**text**) or italics (_text_). Respond using ONLY plain text. Use simple paragraphs or plain lists with dashes (-) if needed."
        val languageInstruction = "IMPORTANT: Please respond in $language."
        
        val finalizedPrompt = systemPrompt.replace("{{context}}", carContext)
            .replace("{{maintenance_goal}}", maintenanceGoal.ifBlank { "General reliability and maintenance." })
        val fullPrompt = "$finalizedPrompt\n\nINSTRUCTIONS:\n- $personaInstruction\n- $formattingInstruction\n- $languageInstruction\n\nUser: $prompt"

        val userContent = Content(
            role = "user",
            parts = listOf(Part(text = fullPrompt))
        )

        val request = GeminiRequest(
            contents = validatedHistory + userContent,
            generationConfig = GenerationConfig(temperature = temperature)
        )

        return postGemini(request)
    }

    suspend fun getCarRecommendations(criteria: String): Result<List<ScannedCarData>> {
        return try {
            val europeanList = europeanBrands.joinToString(", ")
            val globalList = globalBrands.joinToString(", ")
            
            val prompt = """
                Based on these user criteria: $criteria
                Suggest 3-5 specific car models (make, model).
                
                STRICT BRAND RULES:
                1. You MUST ONLY suggest cars from these lists of brands:
                   European: $europeanList
                   Global/Other: $globalList
                2. PRIORITIZE European brands if they fit the user's criteria.
                
                For each car, provide a short 'recommendationReason' (max 2 sentences) in the same language as the user's criteria.
                
                Return ONLY a JSON ARRAY of objects with these keys:
                make, model, recommendationReason.
                
                CRITICAL: Return valid JSON only.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                ),
                generationConfig = GenerationConfig(temperature = 0.8f)
            )

            val response = postGemini(request)
            val fullText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                ?: throw Exception("Empty response from AI")

            val jsonText = extractJson(fullText)
            val data = json.decodeFromString<List<ScannedCarData>>(jsonText)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractJson(text: String): String {
        val cleanedText = text.replace("```json", "").replace("```", "").trim()
        val startBrace = cleanedText.indexOf('{')
        val startBracket = cleanedText.indexOf('[')
        
        val start = when {
            startBrace != -1 && startBracket != -1 -> minOf(startBrace, startBracket)
            startBrace != -1 -> startBrace
            startBracket != -1 -> startBracket
            else -> -1
        }
        
        val endBrace = cleanedText.lastIndexOf('}')
        val endBracket = cleanedText.lastIndexOf(']')
        
        val end = when {
            endBrace != -1 && endBracket != -1 -> maxOf(endBrace, endBracket)
            endBrace != -1 -> endBrace
            endBracket != -1 -> endBracket
            else -> -1
        }

        if (start != -1 && end != -1 && end > start) {
            return cleanedText.substring(start, end + 1)
        }
        return cleanedText
    }
}
