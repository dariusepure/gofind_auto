package com.dariusepure.autoadvisor.ui.cars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import com.dariusepure.autoadvisor.R
import com.dariusepure.autoadvisor.domain.ScannedCarData
import com.dariusepure.autoadvisor.ui.common.ErrorState

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

    var selectedBrand by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("") }
    
    var yearFrom by remember { mutableStateOf("") }
    var yearTo by remember { mutableStateOf("") }
    
    var engineFrom by remember { mutableStateOf("") }
    var engineTo by remember { mutableStateOf("") }
    
    var powerFrom by remember { mutableStateOf("") }
    var powerTo by remember { mutableStateOf("") }

    var selectedFuel by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onCarSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.car_add_title)) },
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
                is AddCarState.Recommendations -> {
                    RecommendationsList(
                        recommendations = s.cars,
                        onSave = { viewModel.saveRecommendation(it) }
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
                            text = "GoFind Auto is thinking...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is AddCarState.Error -> {
                    ErrorState(message = s.message, onRetry = { viewModel.resetState() })
                }
                else -> {
                    DiscoveryQuestionnaire(
                        selectedBrand = selectedBrand,
                        onBrandChange = { 
                            selectedBrand = it
                            selectedModel = "" 
                        },
                        selectedModel = selectedModel,
                        onModelChange = { selectedModel = it },
                        yearFrom = yearFrom,
                        onYearFromChange = { yearFrom = it },
                        yearTo = yearTo,
                        onYearToChange = { yearTo = it },
                        engineFrom = engineFrom,
                        onEngineFromChange = { engineFrom = it },
                        engineTo = engineTo,
                        onEngineToChange = { engineTo = it },
                        powerFrom = powerFrom,
                        onPowerFromChange = { powerFrom = it },
                        powerTo = powerTo,
                        onPowerToChange = { powerTo = it },
                        selectedFuel = selectedFuel,
                        onFuelChange = { selectedFuel = it },
                        onGenerate = { 
                            viewModel.getRecommendations(
                                brand = selectedBrand,
                                model = selectedModel,
                                yearFrom = yearFrom,
                                yearTo = yearTo,
                                engineFrom = engineFrom,
                                engineTo = engineTo,
                                powerFrom = powerFrom,
                                powerTo = powerTo,
                                fuelType = selectedFuel
                            ) 
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryQuestionnaire(
    selectedBrand: String,
    onBrandChange: (String) -> Unit,
    selectedModel: String,
    onModelChange: (String) -> Unit,
    yearFrom: String,
    onYearFromChange: (String) -> Unit,
    yearTo: String,
    onYearToChange: (String) -> Unit,
    engineFrom: String,
    onEngineFromChange: (String) -> Unit,
    engineTo: String,
    onEngineToChange: (String) -> Unit,
    powerFrom: String,
    onPowerFromChange: (String) -> Unit,
    powerTo: String,
    onPowerToChange: (String) -> Unit,
    selectedFuel: String,
    onFuelChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    var brandExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }

    val models = brandModels[selectedBrand] ?: emptyList()

    val fuelTypes = listOf("Petrol", "Diesel", "Electric", "Hybrid", "LPG")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.discovery_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Brand Selector
        ExposedDropdownMenuBox(
            expanded = brandExpanded,
            onExpandedChange = { brandExpanded = !brandExpanded }
        ) {
            OutlinedTextField(
                value = selectedBrand.ifBlank { "Any Brand" },
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.car_make_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                shape = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(
                expanded = brandExpanded,
                onDismissRequest = { brandExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Any Brand") },
                    onClick = {
                        onBrandChange("")
                        brandExpanded = false
                    }
                )
                allCarBrands.forEach { brand ->
                    DropdownMenuItem(
                        text = { Text(brand) },
                        onClick = {
                            onBrandChange(brand)
                            brandExpanded = false
                        }
                    )
                }
            }
        }

        // Model Selector
        ExposedDropdownMenuBox(
            expanded = modelExpanded,
            onExpandedChange = { if (selectedBrand.isNotEmpty()) modelExpanded = !modelExpanded }
        ) {
            OutlinedTextField(
                value = selectedModel.ifBlank { if (selectedBrand.isEmpty()) "Select Brand first" else "Any Model" },
                onValueChange = {},
                readOnly = true,
                enabled = selectedBrand.isNotEmpty(),
                label = { Text(stringResource(R.string.car_model_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                shape = MaterialTheme.shapes.medium
            )
            if (models.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Any Model") },
                        onClick = {
                            onModelChange("")
                            modelExpanded = false
                        }
                    )
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                onModelChange(model)
                                modelExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Fuel Type
        ChoiceRow(
            label = stringResource(R.string.discovery_label_fuel),
            items = fuelTypes,
            selectedItem = selectedFuel,
            onItemClick = { if (selectedFuel == it) onFuelChange("") else onFuelChange(it) }
        )

        // Year Range
        RangeInputRow(
            label = stringResource(R.string.discovery_year_range),
            fromValue = yearFrom,
            onFromChange = onYearFromChange,
            toValue = yearTo,
            onToChange = onYearToChange,
            placeholderFrom = "2015",
            placeholderTo = "2024"
        )

        // Engine Range
        RangeInputRow(
            label = stringResource(R.string.discovery_engine_range),
            fromValue = engineFrom,
            onFromChange = onEngineFromChange,
            toValue = engineTo,
            onToChange = onEngineToChange,
            placeholderFrom = "1500",
            placeholderTo = "3000"
        )

        // Power Range
        RangeInputRow(
            label = stringResource(R.string.discovery_power_range),
            fromValue = powerFrom,
            onFromChange = onPowerFromChange,
            toValue = powerTo,
            onToChange = onPowerToChange,
            placeholderFrom = "150",
            placeholderTo = "300"
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = true, // Always enabled for AI to suggest something
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Search, null)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.discovery_generate_button), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChoiceRow(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    isScrollable: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (isScrollable) {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    FilterChip(
                        selected = selectedItem == item,
                        onClick = { onItemClick(item) },
                        label = { Text(item) }
                    )
                }
            }
        } else {
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    FilterChip(
                        selected = selectedItem == item,
                        onClick = { onItemClick(item) },
                        label = { Text(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun RangeInputRow(
    label: String,
    fromValue: String,
    onFromChange: (String) -> Unit,
    toValue: String,
    onToChange: (String) -> Unit,
    placeholderFrom: String,
    placeholderTo: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = fromValue,
                onValueChange = onFromChange,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.discovery_range_from)) },
                placeholder = { Text(placeholderFrom) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            OutlinedTextField(
                value = toValue,
                onValueChange = onToChange,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.discovery_range_to)) },
                placeholder = { Text(placeholderTo) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
        }
    }
}

@Composable
fun RecommendationsList(
    recommendations: List<ScannedCarData>,
    onSave: (ScannedCarData) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.discovery_recommendations_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(recommendations) { car ->
                RecommendationCard(car = car, onSave = { onSave(car) })
            }
        }
    }
}

@Composable
fun RecommendationCard(
    car: ScannedCarData,
    onSave: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${car.make} ${car.model}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val specs = mutableListOf<String>()
                    car.year?.let { specs.add(it.roundToInt().toString()) }
                    car.fuelType?.let { specs.add(it) }
                    car.power?.let { specs.add("${it.roundToInt()} hp") }
                    car.engineSize?.let { specs.add("${it.roundToInt()} cc") }
                    
                    if (specs.isNotEmpty()) {
                        Text(
                            text = specs.joinToString(" \u00B7 "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Button(
                    onClick = onSave,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.discovery_save_button), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.discovery_why_this_car),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = car.recommendationReason ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
