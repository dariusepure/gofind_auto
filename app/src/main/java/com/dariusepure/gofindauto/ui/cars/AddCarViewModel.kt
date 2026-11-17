package com.dariusepure.gofindauto.ui.cars

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dariusepure.gofindauto.R
import com.dariusepure.gofindauto.data.ai.GeminiRepository
import com.dariusepure.gofindauto.data.auth.AuthRepository
import com.dariusepure.gofindauto.data.cars.CarRepository
import com.dariusepure.gofindauto.domain.Car
import com.dariusepure.gofindauto.domain.EngineOption
import com.dariusepure.gofindauto.domain.ScannedCarData
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
                    _state.value = AddCarState.EditMode(car)
                } else {
                    _state.value = AddCarState.Error(context.getString(R.string.error_car_not_found))
                }
            } catch (e: Exception) {
                _state.value = AddCarState.Error(e.localizedMessage ?: context.getString(R.string.error_failed_to_load_car))
            }
        }
    }

    suspend fun getCarData(carId: String): Car? = carRepository.getCar(carId)

    fun scanCarPhoto(uri: Uri, mimeType: String = "image/jpeg") {
        viewModelScope.launch {
            _state.value = AddCarState.Pending
            val isRomanian = context.resources.configuration.locales[0].language == "ro"
            val language = if (isRomanian) "Romanian" else "English"
            geminiRepository.scanDocument(uri, mimeType, language = language)
                .onSuccess { scannedDataList ->
                    _state.value = AddCarState.Recommendations(scannedDataList)
                }
                .onFailure { e ->
                    _state.value = AddCarState.Error(e.localizedMessage ?: "Failed to identify car from photo")
                }
        }
    }

    fun saveRecommendation(
        data: ScannedCarData,
        selectedEngine: EngineOption? = null,
        includeYears: Boolean = true,
        includeBodyType: Boolean = true,
        includeColor: Boolean = true,
        includeTechSpecs: Boolean = true
    ) {
        viewModelScope.launch {
            _state.value = AddCarState.Pending
            try {
                val chosenFuelType = selectedEngine?.fuelType?.ifBlank { null } ?: data.fuelType ?: ""
                val chosenEngineSize = selectedEngine?.engineSize?.roundToInt()?.toString() ?: data.engineSize?.roundToInt()?.toString() ?: ""
                val chosenPower = selectedEngine?.power?.roundToInt() ?: data.power?.roundToInt() ?: 0

                val extraInfo = buildString {
                    if (selectedEngine != null && selectedEngine.name.isNotBlank()) {
                        append("Engine Variant: ${selectedEngine.name}. ")
                    }
                    if (includeYears && !data.productionYears.isNullOrBlank()) append("Production Years: ${data.productionYears}. ")
                    if (includeBodyType && !data.bodyType.isNullOrBlank()) append("Body Type: ${data.bodyType}. ")
                    if (includeColor && !data.color.isNullOrBlank()) append("Color: ${data.color}. ")
                }.trim()

                val car = Car(
                    id = currentCarId ?: "",
                    make = data.make ?: "Unknown",
                    model = data.model ?: "Unknown",
                    year = if (includeYears) (data.year?.roundToInt() ?: 0) else 0,
                    fuelType = if (includeTechSpecs) chosenFuelType else "",
                    engineSize = if (includeTechSpecs) chosenEngineSize else "",
                    power = if (includeTechSpecs) chosenPower else 0,
                    powerUnit = data.powerUnit ?: "hp",
                    recommendationReason = extraInfo,
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

    fun saveCarForm(
        make: String,
        model: String,
        year: String,
        fuelType: String,
        engineSize: String,
        power: String
    ) {
        viewModelScope.launch {
            _state.value = AddCarState.Pending
            try {
                val car = Car(
                    id = currentCarId ?: "",
                    make = make.trim(),
                    model = model.trim(),
                    year = year.toIntOrNull() ?: 0,
                    fuelType = fuelType.trim(),
                    engineSize = engineSize.trim(),
                    power = power.toIntOrNull() ?: 0,
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
