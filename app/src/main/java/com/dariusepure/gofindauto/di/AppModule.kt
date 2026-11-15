package com.dariusepure.gofindauto.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.dariusepure.gofindauto.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(HttpTimeout) {
            connectTimeoutMillis = 20000
            socketTimeoutMillis = 60000
            requestTimeoutMillis = 90000
        }
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
                allowSpecialFloatingPointValues = true
            })
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = Firebase.firestore
        firestore.firestoreSettings = com.google.firebase.firestore.firestoreSettings {
            setLocalCacheSettings(com.google.firebase.firestore.PersistentCacheSettings.newBuilder().build())
        }
        return firestore
    }

    @Provides
    @Singleton
    fun provideRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf(
            "gemini_api_key" to "",
            "gemini_model_name" to "gemini-2.0-flash",
            "gemini_timeout_seconds" to 30L,
            "gemini_temperature" to 0.7f,
            "gemini_prompt" to """
                You are GoFind Auto, an expert automotive consultant and car maintenance AI. Your primary goal is to provide personalized maintenance plans and reliability advice based on the user's goals.
                
                CONTEXT: {{context}}
                
                USER GOAL: The user wants their car to: {{maintenance_goal}}
                
                TECHNICAL KNOWLEDGE:
                - You understand common issues for specific makes, models, and years.
                - You provide maintenance intervals based on the car's current mileage and history.
                - You suggest preventive fixes before parts fail.
                
                CONSULTATION RULES:
                1. Always prioritize the user's maintenance goal.
                2. Be proactive: if a car has high mileage, suggest specific component checks (e.g., timing belt, water pump, suspension bushings).
                3. Use simple, non-intimidating language, but stay technically accurate.
                4. When the user asks "What should I do next?", give a clear, prioritized list.
                
                IMPORTANT: When talking about technical parts, use natural language. Talk to the user as a trusted expert sitting next to them.
            """.trimIndent(),
            "gemini_persona_instruction" to "Act as GoFind Auto, a professional car mechanic. Use human-friendly terms only. Talk to the user as if you are their trusted advisor in a garage. If you see recent maintenance or relevant history, mention it naturally.",
            "gemini_formatting_instruction" to "STRICTLY PROHIBIT markdown bold (**text**) or italics (_text_). Respond using ONLY plain text. Use simple paragraphs or plain lists with dashes (-) if needed.",
            "gemini_scanning_prompt" to """
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
            """.trimIndent(),
            "gemini_recommendation_prompt" to """
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
                make, model, year, fuelType, engineSize, power, powerUnit, recommendationReason.
                
                CRITICAL: Return valid JSON only.
            """.trimIndent()
        ))
        remoteConfig.fetchAndActivate()
        return remoteConfig
    }
}
