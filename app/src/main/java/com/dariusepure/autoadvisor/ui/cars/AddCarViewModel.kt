package com.dariusepure.autoadvisor.ui.cars

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dariusepure.autoadvisor.R
import com.dariusepure.autoadvisor.data.ai.GeminiRepository
import com.dariusepure.autoadvisor.data.auth.AuthRepository
import com.dariusepure.autoadvisor.data.cars.CarRepository
import com.dariusepure.autoadvisor.data.prefs.PreferenceRepository
import com.dariusepure.autoadvisor.domain.Car
import com.dariusepure.autoadvisor.domain.ScannedCarData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class AddCarViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val authRepository: AuthRepository,
    private val geminiRepository: GeminiRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<AddCarState>(AddCarState.Idle)
    val state = _state.asStateFlow()

    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var currentCarId: String? = null

    fun loadCar(carId: String) {
        currentCarId = carId
        viewModelScope.launch {
            _state.value = AddCarState.Pending
            try {
                val car = carRepository.getCar(carId)
                if (car != null) {
                    _state.value = AddCarState.Idle
                }
            } catch (e: Exception) {
                _state.value = AddCarState.Error(e.localizedMessage ?: context.getString(R.string.error_failed_to_load_car))
            }
        }
    }

    suspend fun getCarData(carId: String): Car? = carRepository.getCar(carId)

    fun getRecommendations(
        usage: String,
        priority: String,
        details: String = "",
        year: String = ""
    ) {
        viewModelScope.launch {
            _state.value = AddCarState.Pending
            val criteria = buildString {
                append("Main Usage: $usage, Priority: $priority.")
                if (details.isNotBlank()) append(" Preferred engine/generation details: $details.")
                if (year.isNotBlank()) append(" Target production year: $year.")
            }
            geminiRepository.getCarRecommendations(criteria)
                .onSuccess { recommendations ->
                    _state.value = AddCarState.Recommendations(recommendations)
                }
                .onFailure { e ->
                    _state.value = AddCarState.Error(e.localizedMessage ?: "Failed to get recommendations from AI")
                }
        }
    }

    fun saveRecommendation(data: ScannedCarData) {
        viewModelScope.launch {
            _state.value = AddCarState.Pending
            try {
                val car = Car(
                    id = "",
                    make = data.make ?: "Unknown",
                    model = data.model ?: "Unknown",
                    year = data.year?.roundToInt() ?: 0,
                    recommendationReason = data.recommendationReason ?: "",
                    updatedAt = Date()
                )
                carRepository.createCar(car)
                _state.value = AddCarState.Success
                _navigationEvent.trySend(Unit)
            } catch (e: Exception) {
                _state.value = AddCarState.Error(e.localizedMessage ?: "Failed to save recommendation")
            }
        }
    }

    fun onAddOrUpdateCar(
        make: String,
        model: String,
        year: String,
        maintenanceGoal: String
    ) {
        viewModelScope.launch {
            _state.value = AddCarState.Pending
            try {
                val car = Car(
                    id = currentCarId ?: "",
                    make = make.trim(),
                    model = model.trim(),
                    year = year.toIntOrNull() ?: 0,
                    maintenanceGoal = maintenanceGoal,
                    updatedAt = Date()
                )
                carRepository.createCar(car)
                _state.value = AddCarState.Success
                _navigationEvent.trySend(Unit)
            } catch (e: Exception) {
                _state.value = AddCarState.Error(e.localizedMessage ?: context.getString(R.string.error_failed_to_save_car))
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                _state.value = AddCarState.Error(e.localizedMessage ?: context.getString(R.string.error_failed_to_sign_out))
            }
        }
    }

    fun resetState() {
        _state.value = AddCarState.Idle
        currentCarId = null
    }
}
