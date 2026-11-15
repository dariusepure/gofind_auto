package com.dariusepure.gofindauto.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.dariusepure.gofindauto.util.DiagnosticUtils
import com.dariusepure.gofindauto.domain.ScannedCarData
import com.dariusepure.gofindauto.ui.cars.europeanBrands
import com.dariusepure.gofindauto.ui.cars.globalBrands
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
        get() = remoteConfig.getString("gemini_model_name").ifBlank { "gemini-2.0-flash" }

    private val temperature: Float
        get() = remoteConfig.getDouble("gemini_temperature").let { if (it == 0.0) 0.7f else it.toFloat() }

    private val systemPrompt: String
        get() = remoteConfig.getString("gemini_prompt")

    private val personaInstruction: String
        get() = remoteConfig.getString("gemini_persona_instruction")

    private val formattingInstruction: String
        get() = remoteConfig.getString("gemini_formatting_instruction")

    private val scanningPrompt: String
        get() = remoteConfig.getString("gemini_scanning_prompt")

    private val recommendationPrompt: String
        get() = remoteConfig.getString("gemini_recommendation_prompt")

    private val geminiApiKey: String
        get() = remoteConfig.getString("gemini_api_key")

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        allowSpecialFloatingPointValues = true
    }

    private val tag = "GeminiRepository"

    private fun buildScanningPrompt(language: String): String = """
        Extract complete vehicle details from this car photo or document.
        Analyze the image and identify:
        - 'make': Brand/Make in Title Case (e.g., "Bmw", "Volkswagen", "Daewoo").
        - 'model': Model name (e.g., "3 Series", "Golf", "Nexia").
        - 'year': Estimated year of the specific car shown (4 digits).
        - 'productionYears': Generation production years range (e.g., "1995 - 2002" or "2012 - 2020").
        - 'bodyType': Body style in $language (e.g., "Hatchback", "Sedan", "SUV", "Combi", "Coupe").
        - 'color': Detected car color in $language (e.g., "Argintiu", "Negru", "Albastru", "Alb", "Rosu", "Verde", "Gri" for Romanian; or "Silver", "Black", "Blue", "White", "Red", "Green", "Gray" for English).
        - 'fuelType': Primary fuel type (Petrol, Diesel, Electric, Hybrid, LPG).
        - 'engineSize': Engine size in cc (e.g., 1395).
        - 'power': Power in hp (e.g., 122).
        - 'availableEngineOptions': A COMPREHENSIVE ARRAY of ALL factory engine options available for this specific generation and model (include ALL common petrol, diesel, hybrid variants, e.g. 5 to 10 engine options).
          Each object must contain:
          - "name": engine commercial name (e.g. "1.2 TSI (85hp)", "1.4 TSI (122hp)", "1.6 TDI (105hp)", "2.0 TDI (150hp)", "2.0 TDI (184hp)", "2.0 TSI (220hp)")
          - "fuelType": "Petrol" or "Diesel" or "Electric" or "Hybrid"
          - "engineSize": numeric cc (e.g. 1395)
          - "power": numeric hp (e.g. 122)
        
        CRITICAL VALIDATION:
        1. Verify Year: must be a realistic year (e.g., 1900-2026).
        2. Verify Engine Size: must be in cubic centimeters (cc).
        3. Brand (make) MUST be returned in Title Case.
        4. NUMERIC FIELDS MUST contain ONLY the raw number, NO units.
        5. You MUST list ALL engine variants in availableEngineOptions. Do NOT truncate or limit to 2 or 3 engines. Include 5-10 engine variants if they exist for this car model!
        6. Respond in $language for color and bodyType!
        
        Return ONLY a JSON ARRAY containing one object with these keys: 
        make, model, year, productionYears, bodyType, color, fuelType, engineSize, power, powerUnit, availableEngineOptions.
    """.trimIndent()

    private suspend fun postGemini(request: GeminiRequest): GeminiResponse {
        val configuredModel = modelName.ifBlank { "gemini-2.0-flash" }
        val candidateModels = listOf(
            configuredModel,
            "gemini-2.0-flash",
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash-lite-preview-02-05",
            "gemini-1.5-pro",
            "gemini-1.5-flash",
            "gemini-1.5-flash-latest"
        ).distinct()

        var lastException: Exception? = null

        for (model in candidateModels) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=$geminiApiKey"
            val sha1 = DiagnosticUtils.getAppSignatureSha1(context, withColons = true)
            val packageName = context.packageName

            try {
                var response: io.ktor.client.statement.HttpResponse = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    header("X-Android-Package", packageName)
                    header("X-Android-Cert", sha1)
                    setBody(request)
                }

                if (response.status.value == 403) {
                    response = httpClient.post(url) {
                        contentType(ContentType.Application.Json)
                        header("X-Android-Package", "com.dariusepure.autoadvisor")
                        header("X-Android-Cert", sha1)
                        setBody(request)
                    }
                }

                if (response.status.value == 403) {
                    response = httpClient.post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                }

                if (response.status.value in 200..299) {
                    return response.body()
                } else {
                    val errorBody = response.body<String>()
                    Log.w(tag, "Model $model returned status ${response.status}: $errorBody")
                    lastException = Exception("Gemini API Error ${response.status.value} ($model): $errorBody")
                }
            } catch (e: Exception) {
                Log.w(tag, "Gemini Post failed for model $model", e)
                lastException = e
            }
        }

        throw lastException ?: Exception("All Gemini models failed.")
    }

    suspend fun scanRegistrationCertificate(bitmap: Bitmap, language: String = "English"): Result<List<ScannedCarData>> {
        return try {
            val prompt = buildScanningPrompt(language)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = Blob(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                ),
                generationConfig = GenerationConfig(temperature = temperature, responseMimeType = "application/json")
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

    suspend fun scanDocument(uri: Uri, mimeType: String, language: String = "English"): Result<List<ScannedCarData>> {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("Could not read file")
            val base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)

            val prompt = buildScanningPrompt(language)

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = Blob(mimeType = mimeType, data = base64Data))
                        )
                    )
                ),
                generationConfig = GenerationConfig(temperature = temperature, responseMimeType = "application/json")
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
        history: List<com.dariusepure.gofindauto.ui.cars.ChatMessage>,
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

        val isFirstMessage = validatedHistory.isEmpty()
        val greetingRule = if (isFirstMessage) {
            "Always start with a brief, friendly mechanic's greeting."
        } else {
            "Do not repeat the greeting or introduction. Get straight to the point and answer the user's question directly."
        }

        val dynamicPersona = "Act as a professional, experienced car mechanic. $greetingRule Use human-friendly terms only; NEVER use internal data names or variable names like 'fuelType', 'vin', or 'make' in your sentences—use natural language like 'tip de combustibil', 'serie sasiu', or 'marca' instead. NEVER mention that you are reading from a 'context' or 'database'. Talk to the user as if you are standing next to their car in a garage. If you see recent maintenance or relevant history, mention it naturally."
        val dynamicFormatting = "STRICTLY PROHIBIT the use of markdown bold (**text**) or italics (_text_). Respond using ONLY plain text. Use simple paragraphs or plain lists with dashes (-) if needed."
        val languageInstruction = "IMPORTANT: Please respond in $language."
        
        val finalizedPrompt = systemPrompt.ifBlank { "You are GoFind Auto, an expert automotive consultant and car mechanic." }
            .replace("{{context}}", carContext)
            .replace("{{maintenance_goal}}", maintenanceGoal.ifBlank { "General reliability and maintenance." })
        val fullPrompt = "$finalizedPrompt\n\nINSTRUCTIONS:\n- $dynamicPersona\n- $dynamicFormatting\n- $languageInstruction\n\nUser: $prompt"

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
            
            val prompt = recommendationPrompt.ifBlank {
                """
                Based on these user criteria: {{criteria}}
                Suggest 3-5 specific car models (make, model).
                
                STRICT BRAND RULES:
                1. You MUST ONLY suggest cars from these lists of brands:
                   European: {{european_brands}}
                   Global/Other: {{global_brands}}
                2. PRIORITIZE European brands if they fit the user's criteria.
                
                For each car, provide a short 'recommendationReason' (max 2 sentences) in the same language as the user's criteria.
                Include technical details for better context.
                
                Return ONLY a JSON ARRAY of objects with these keys:
                make, model, year, productionYears, bodyType, color, fuelType, engineSize, power, powerUnit, availableEngineOptions, recommendationReason.
                
                CRITICAL: Return valid JSON only.
                """.trimIndent()
            }
                .replace("{{criteria}}", criteria)
                .replace("{{european_brands}}", europeanList)
                .replace("{{global_brands}}", globalList)

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                ),
                generationConfig = GenerationConfig(temperature = 0.8f, responseMimeType = "application/json")
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

        val firstBracket = cleanedText.indexOf('[')
        val firstBrace = cleanedText.indexOf('{')
        
        if (firstBracket != -1 && (firstBrace == -1 || firstBracket < firstBrace)) {
            val lastBracket = cleanedText.lastIndexOf(']')
            if (lastBracket > firstBracket) {
                return cleanedText.substring(firstBracket, lastBracket + 1)
            }
        }
        
        if (firstBrace != -1) {
            val lastBrace = cleanedText.lastIndexOf('}')
            if (lastBrace > firstBrace) {
                return cleanedText.substring(firstBrace, lastBrace + 1)
            }
        }

        return cleanedText
    }
}
