package com.dariusepure.gofindauto.ui.cars

sealed interface AddCarState {
    data object Idle : AddCarState
    data object Pending : AddCarState
    data object Success : AddCarState
    data object Scanning : AddCarState
    data class Recommendations(val cars: List<com.dariusepure.gofindauto.domain.ScannedCarData>) : AddCarState
    data class Error(val message: String) : AddCarState
}

