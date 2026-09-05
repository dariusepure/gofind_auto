package com.dariusepure.autoadvisor.di

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
import com.dariusepure.autoadvisor.BuildConfig
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
            "gemini_model_name" to "gemini-3.5-flash-lite",
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
            """.trimIndent()
        ))
        remoteConfig.fetchAndActivate()
        return remoteConfig
    }
}

