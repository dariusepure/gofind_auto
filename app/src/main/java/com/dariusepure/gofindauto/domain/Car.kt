package com.dariusepure.gofindauto.domain

import androidx.compose.runtime.Stable
import java.util.Date

@Stable
data class Car(
    val id: String = "",
    val make: String = "",
    val model: String = "",
    val vin: String = "",
    val year: Int = 0,
    val engineSize: String = "",
    val fuelType: String = "",
    val power: Int = 0,
    val powerUnit: String = "hp",
    val accentColor: Long? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val activityCount: Int = 0,
    val maintenanceGoal: String = "",
    val recommendationReason: String = ""
)

val Car.displayName: String
    get() = "$make $model".trim().ifBlank { "Unnamed car" }

val Car.bodyType: String
    get() {
        val regex = Regex("""(?:Body Type|Caroserie):\s*([^.\n]+)""", RegexOption.IGNORE_CASE)
        return regex.find(recommendationReason)?.groupValues?.get(1)?.trim() ?: ""
    }
