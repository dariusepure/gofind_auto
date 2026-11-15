package com.dariusepure.gofindauto.domain

import com.dariusepure.gofindauto.util.FlexibleDoubleSerializer
import kotlinx.serialization.Serializable

@Serializable
data class ScannedCarData(
    val make: String? = null,
    val model: String? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    val year: Double? = null,
    val productionYears: String? = null,
    val bodyType: String? = null,
    val color: String? = null,
    val fuelType: String? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    val engineSize: Double? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    val power: Double? = null,
    val powerUnit: String? = "hp",
    val availableEngines: String? = null,
    val recommendationReason: String? = null
)
