package com.dariusepure.autoadvisor.ui.cars

import com.dariusepure.autoadvisor.domain.Car

sealed interface CarListUiState {
    data object Loading : CarListUiState
    data class Success(val cars: List<Car>): CarListUiState
    data object Empty : CarListUiState
    data class Error(val message: String): CarListUiState
}

