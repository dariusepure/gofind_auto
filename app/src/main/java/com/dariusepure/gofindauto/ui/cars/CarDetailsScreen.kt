package com.dariusepure.gofindauto.ui.cars

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dariusepure.gofindauto.R
import com.dariusepure.gofindauto.domain.*
import com.dariusepure.gofindauto.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    carId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onDiagnosisClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CarDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CarDetailsUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(carId) {
        viewModel.loadCarData(carId)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.car_technical_sheet), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(carId) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.common_edit),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            CarDetailsUiState.Loading -> CarDetailsSkeleton()
            is CarDetailsUiState.Error -> ErrorState(message = s.message, onRetry = { viewModel.loadCarData(carId) })
            is CarDetailsUiState.Success -> {
                val car = s.car
                val carAccentColor = MaterialTheme.colorScheme.primary
                val logoRes = remember(car.make) { CarFormatters.getBrandLogoResource(car.make) }

                val makeLabel = stringResource(R.string.car_make_label)
                val modelLabel = stringResource(R.string.car_model_label)
                val yearLabel = stringResource(R.string.car_year_label)
                val bodyLabel = stringResource(R.string.spec_label_body)
                val fuelLabel = stringResource(R.string.discovery_label_fuel)
                val engineSizeLabel = stringResource(R.string.car_engine_size_label)
                val powerLabel = stringResource(R.string.car_power_label)

                val identityItems = listOfNotNull(
                    if (car.make.isNotBlank()) SpecItem(makeLabel, car.make, Icons.Default.Factory) else null,
                    if (car.model.isNotBlank()) SpecItem(modelLabel, car.model, Icons.Default.Category) else null,
                    if (car.year > 0) SpecItem(yearLabel, car.year.toString(), Icons.Default.CalendarMonth) else null,
                    if (car.bodyType.isNotBlank()) SpecItem(bodyLabel, CarTranslations.getVehicleTypeLabel(context, car.bodyType), Icons.Outlined.DirectionsCar) else null
                )

                val engineItems = listOfNotNull(
                    if (car.fuelType.isNotBlank()) SpecItem(fuelLabel, CarTranslations.getFuelTypeLabel(context, car.fuelType), Icons.Default.LocalGasStation) else null,
                    if (car.engineSize.isNotBlank()) SpecItem(engineSizeLabel, "${car.engineSize} cc", CheckEngineIcon) else null,
                    if (car.power > 0) SpecItem(powerLabel, CarFormatters.formatPower(context, car), Icons.Default.Speed) else null
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card (Title Only, no summary line)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (logoRes != null) {
                                        Image(
                                            painter = painterResource(logoRes),
                                            contentDescription = car.make,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .padding(6.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.DirectionsCar,
                                            contentDescription = car.make,
                                            modifier = Modifier.size(40.dp),
                                            tint = carAccentColor
                                        )
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    text = car.displayName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // 1. Vehicle Identity Category
                    if (identityItems.isNotEmpty()) {
                        item {
                            TechnicalCategory(
                                title = stringResource(R.string.car_identity_section),
                                icon = Icons.Default.DirectionsCar
                            ) {
                                SpecificationCard(specItems = identityItems)
                            }
                        }
                    }

                    // 2. Engine & Performance Category
                    if (engineItems.isNotEmpty()) {
                        item {
                            TechnicalCategory(
                                title = stringResource(R.string.car_engine_transmission_section),
                                icon = CheckEngineIcon
                            ) {
                                SpecificationCard(specItems = engineItems)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TechnicalCategory(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        content()
    }
}
