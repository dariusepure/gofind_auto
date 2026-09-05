package com.dariusepure.autoadvisor.ui.cars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dariusepure.autoadvisor.data.cars.CarRepository
import com.dariusepure.autoadvisor.domain.Car
import com.dariusepure.autoadvisor.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CarDetailsUiEvent {
    data class ShowToast(val message: String) : CarDetailsUiEvent()
}

sealed class CarDetailsUiState {
    object Loading : CarDetailsUiState()
    data class Success(
        val car: Car
    ) : CarDetailsUiState()
    data class Error(val message: String) : CarDetailsUiState()
}

@HiltViewModel
class CarDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val carRepository: CarRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CarDetailsUiState>(CarDetailsUiState.Loading)
    val state = _state.asStateFlow()
    
    private val _uiEvent = Channel<CarDetailsUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun loadCarData(carId: String) {
        viewModelScope.launch {
            _state.value = CarDetailsUiState.Loading
            try {
                carRepository.getCarFlow(carId).collect { car ->
                    if (car != null) {
                        _state.value = CarDetailsUiState.Success(car)
                    } else {
                        _state.value = CarDetailsUiState.Error(context.getString(R.string.error_car_not_found))
                    }
                }
            } catch (e: Exception) {
                _state.value = CarDetailsUiState.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }
}
