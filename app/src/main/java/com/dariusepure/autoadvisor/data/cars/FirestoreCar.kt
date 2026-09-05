package com.dariusepure.autoadvisor.data.cars

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.dariusepure.autoadvisor.domain.Car

data class FirestoreCar(
    @DocumentId val id: String = "",
    val make: String = "",
    val model: String = "",
    val vin: String = "",
    val year: Int = 0,
    val engineSize: String = "",
    val fuelType: String = "",
    val power: Int = 0,
    val powerUnit: String = "hp",
    val accentColor: Long? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val activityCount: Int = 0,
    val maintenanceGoal: String = "",
    val recommendationReason: String = ""
)

fun Car.toFirebase() = FirestoreCar(
    id = this.id,
    make = this.make,
    model = this.model,
    vin = this.vin,
    year = this.year,
    engineSize = this.engineSize,
    fuelType = this.fuelType,
    power = this.power,
    powerUnit = this.powerUnit,
    accentColor = this.accentColor,
    createdAt = Timestamp(this.createdAt),
    updatedAt = Timestamp(this.updatedAt),
    activityCount = this.activityCount,
    maintenanceGoal = this.maintenanceGoal,
    recommendationReason = this.recommendationReason
)

fun FirestoreCar.fromFirebase() = Car(
    id = this.id,
    make = this.make,
    model = this.model,
    vin = this.vin,
    year = this.year,
    engineSize = this.engineSize,
    fuelType = this.fuelType,
    power = this.power,
    powerUnit = this.powerUnit,
    accentColor = this.accentColor,
    createdAt = this.createdAt.toDate(),
    updatedAt = this.updatedAt.toDate(),
    activityCount = this.activityCount,
    maintenanceGoal = this.maintenanceGoal,
    recommendationReason = this.recommendationReason
)
