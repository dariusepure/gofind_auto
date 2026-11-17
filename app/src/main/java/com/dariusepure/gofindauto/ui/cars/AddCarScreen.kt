package com.dariusepure.gofindauto.ui.cars

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import com.dariusepure.gofindauto.R
import com.dariusepure.gofindauto.domain.Car
import com.dariusepure.gofindauto.domain.EngineOption
import com.dariusepure.gofindauto.domain.ScannedCarData
import com.dariusepure.gofindauto.ui.common.CarTranslations
import com.dariusepure.gofindauto.ui.common.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    carId: String? = null,
    onCarSaved: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddCarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.scanCarPhoto(uri)
        }
    }

    LaunchedEffect(carId) {
        if (!carId.isNullOrBlank()) {
            viewModel.loadCar(carId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onCarSaved()
        }
    }

    val screenTitle = when {
        !carId.isNullOrBlank() -> stringResource(R.string.common_edit)
        else -> stringResource(R.string.car_add_title)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is AddCarState.EditMode -> {
                    EditCarForm(
                        initialCar = s.car,
                        onSave = { make, model, year, fuelType, engineSize, power ->
                            viewModel.saveCarForm(make, model, year, fuelType, engineSize, power)
                        }
                    )
                }
                is AddCarState.Recommendations -> {
                    RecommendationsList(
                        recommendations = s.cars,
                        onSave = { car, selectedEngine, incY, incB, incC, incT ->
                            viewModel.saveRecommendation(
                                data = car,
                                selectedEngine = selectedEngine,
                                includeYears = incY,
                                includeBodyType = incB,
                                includeColor = incC,
                                includeTechSpecs = incT
                            )
                        },
                        onScanNew = { viewModel.resetState() }
                    )
                }
                AddCarState.Pending -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.ai_scan_analyzing),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is AddCarState.Error -> {
                    ErrorState(message = s.message, onRetry = { viewModel.resetState() })
                }
                else -> {
                    DiscoveryPhotoScanCard(
                        onUploadPhoto = { photoPickerLauncher.launch("image/*") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarForm(
    initialCar: Car,
    onSave: (
        make: String,
        model: String,
        year: String,
        fuelType: String,
        engineSize: String,
        power: String
    ) -> Unit
) {
    val context = LocalContext.current
    var make by remember { mutableStateOf(initialCar.make) }
    var model by remember { mutableStateOf(initialCar.model) }
    var year by remember { mutableStateOf(if (initialCar.year > 0) initialCar.year.toString() else "") }
    var fuelType by remember { mutableStateOf(initialCar.fuelType) }
    var engineSize by remember { mutableStateOf(initialCar.engineSize) }
    var power by remember { mutableStateOf(if (initialCar.power > 0) initialCar.power.toString() else "") }

    val fuelTypes = listOf("Petrol", "Diesel", "Electric", "Hybrid", "LPG", "CNG", "Hydrogen")
    var fuelDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Main Info
        Text(
            text = stringResource(R.string.section_main_info),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = make,
            onValueChange = { make = it },
            label = { Text(stringResource(R.string.car_make_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text(stringResource(R.string.car_model_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = year,
            onValueChange = { year = it.filter { char -> char.isDigit() }.take(4) },
            label = { Text(stringResource(R.string.car_year_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Section 2: Engine & Specs
        Text(
            text = stringResource(R.string.section_engine_fuel),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // Fuel Type Dropdown
        ExposedDropdownMenuBox(
            expanded = fuelDropdownExpanded,
            onExpandedChange = { fuelDropdownExpanded = !fuelDropdownExpanded }
        ) {
            OutlinedTextField(
                value = CarTranslations.getFuelTypeLabel(context, fuelType),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.discovery_label_fuel)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelDropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = fuelDropdownExpanded,
                onDismissRequest = { fuelDropdownExpanded = false }
            ) {
                fuelTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(CarTranslations.getFuelTypeLabel(context, type)) },
                        onClick = {
                            fuelType = type
                            fuelDropdownExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = engineSize,
            onValueChange = { engineSize = it.filter { char -> char.isDigit() } },
            label = { Text(stringResource(R.string.car_engine_size_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = power,
            onValueChange = { power = it.filter { char -> char.isDigit() } },
            label = { Text(stringResource(R.string.car_power_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                onSave(make, model, year, fuelType, engineSize, power)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = make.isNotBlank() && model.isNotBlank()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.common_save), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun DiscoveryPhotoScanCard(
    onUploadPhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HERO CARD FOR CAR PHOTO IDENTIFIER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.ai_scan_hero_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.ai_scan_hero_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onUploadPhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.ai_scan_upload_button),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendationsList(
    recommendations: List<ScannedCarData>,
    onSave: (
        car: ScannedCarData,
        selectedEngine: EngineOption?,
        includeYears: Boolean,
        includeBodyType: Boolean,
        includeColor: Boolean,
        includeTechSpecs: Boolean
    ) -> Unit,
    onScanNew: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ai_scan_result_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedButton(
                onClick = onScanNew,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Scan Another")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(recommendations) { car ->
                RecommendationCard(
                    car = car,
                    onSave = { selectedEngine, incY, incB, incC, incT ->
                        onSave(car, selectedEngine, incY, incB, incC, incT)
                    }
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(
    car: ScannedCarData,
    onSave: (
        selectedEngine: EngineOption?,
        includeYears: Boolean,
        includeBodyType: Boolean,
        includeColor: Boolean,
        includeTechSpecs: Boolean
    ) -> Unit
) {
    val context = LocalContext.current

    val engineOptions = remember(car) {
        val list = mutableListOf<EngineOption>()
        car.availableEngineOptions?.filter { it.name.isNotBlank() }?.let { list.addAll(it) }
        if (list.isEmpty() && (!car.fuelType.isNullOrBlank() || car.engineSize != null)) {
            val defaultName = listOfNotNull(
                car.engineSize?.roundToInt()?.let { "%.1f".format(it / 1000.0) },
                car.fuelType
            ).joinToString(" ").ifBlank { "Standard Engine" }
            list.add(
                EngineOption(
                    name = defaultName,
                    fuelType = car.fuelType ?: "",
                    engineSize = car.engineSize,
                    power = car.power
                )
            )
        }
        list
    }

    var selectedEngineIndex by remember(car) { mutableIntStateOf(0) }
    val currentSelectedEngine = engineOptions.getOrNull(selectedEngineIndex)

    var includeYears by remember { mutableStateOf(true) }
    var includeBodyType by remember { mutableStateOf(true) }
    var includeColor by remember { mutableStateOf(true) }
    var includeTechSpecs by remember { mutableStateOf(true) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Make & Model + Save Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${car.make.orEmpty()} ${car.model.orEmpty()}".trim(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        onSave(
                            currentSelectedEngine,
                            includeYears,
                            includeBodyType,
                            includeColor,
                            includeTechSpecs
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.ai_scan_save_to_garage), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // --- 1. ENGINE VARIANT SELECTION (RADIO BUTTONS) ---
            if (engineOptions.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.select_engine_variant),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    engineOptions.forEachIndexed { index, option ->
                        val isSelected = index == selectedEngineIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedEngineIndex = index }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedEngineIndex = index }
                            )
                            Spacer(Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                val fuelLabel = CarTranslations.getFuelTypeLabel(context, option.fuelType)
                                val specsInfo = buildList {
                                    if (fuelLabel.isNotBlank()) add(fuelLabel)
                                    option.engineSize?.let { add("${it.roundToInt()} cc") }
                                    option.power?.let { add("${it.roundToInt()} hp") }
                                }.joinToString(" \u00B7 ")

                                if (specsInfo.isNotBlank()) {
                                    Text(
                                        text = specsInfo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // --- 2. OTHER SPECIFICATIONS TO INCLUDE ---
            Text(
                text = stringResource(R.string.select_specs_to_save),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Production Years
                val yearsText = car.productionYears?.ifBlank { null }
                    ?: car.year?.roundToInt()?.let { "$it" }
                if (!yearsText.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeYears,
                            onCheckedChange = { includeYears = it }
                        )
                        Text(
                            text = "${stringResource(R.string.spec_label_years)}: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = yearsText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Body Style
                if (!car.bodyType.isNullOrBlank()) {
                    val bodyLabel = CarTranslations.getVehicleTypeLabel(context, car.bodyType)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeBodyType,
                            onCheckedChange = { includeBodyType = it }
                        )
                        Text(
                            text = "${stringResource(R.string.spec_label_body)}: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = bodyLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Color
                if (!car.color.isNullOrBlank()) {
                    val colorLabel = CarTranslations.getColorLabel(context, car.color)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeColor,
                            onCheckedChange = { includeColor = it }
                        )
                        Text(
                            text = "${stringResource(R.string.spec_label_color)}: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = colorLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Tech Specs (from currentSelectedEngine)
                if (currentSelectedEngine != null) {
                    val fuelLabel = CarTranslations.getFuelTypeLabel(context, currentSelectedEngine.fuelType)
                    val techSpecs = buildList {
                        if (fuelLabel.isNotBlank()) add(fuelLabel)
                        currentSelectedEngine.engineSize?.let { add("${it.roundToInt()} cc") }
                        currentSelectedEngine.power?.let { add("${it.roundToInt()} hp") }
                    }.joinToString(" \u00B7 ")

                    if (techSpecs.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeTechSpecs,
                                onCheckedChange = { includeTechSpecs = it }
                            )
                            Text(
                                text = "${stringResource(R.string.spec_label_engine_specs)}: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = techSpecs,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
