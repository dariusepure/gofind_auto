package com.dariusepure.gofindauto.ui.cars

import com.dariusepure.gofindauto.domain.Car
import com.dariusepure.gofindauto.domain.ScannedCarData

sealed interface AddCarState {
    data object Idle : AddCarState
    data object Pending : AddCarState
    data object Success : AddCarState
    data object Scanning : AddCarState
    data class EditMode(val car: Car) : AddCarState
    data class Recommendations(val cars: List<ScannedCarData>) : AddCarState
    data class Error(val message: String) : AddCarState
}
