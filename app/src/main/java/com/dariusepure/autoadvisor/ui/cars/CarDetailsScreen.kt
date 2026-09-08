package com.dariusepure.autoadvisor.ui.cars

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dariusepure.autoadvisor.R
import com.dariusepure.autoadvisor.domain.*
import com.dariusepure.autoadvisor.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    carId: String,
    onBack: () -> Unit,
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
                title = { Text(stringResource(R.string.car_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(carAccentColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.DirectionsCar,
                                    null,
                                    modifier = Modifier.size(48.dp),
                                    tint = carAccentColor
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = car.displayName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            val summary = CarFormatters.getCarSummary(context, car)
                            if (summary.isNotBlank()) {
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (car.maintenanceGoal.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Outlined.Lightbulb, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(car.maintenanceGoal, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    // --- BENTO GRID FOR AI ADVISOR ---
                    item {
                        BentoCard(
                            onClick = onDiagnosisClick,
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            containerColor = carAccentColor.copy(alpha = 0.15f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(48.dp), tint = carAccentColor)
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = stringResource(R.string.car_diagnosis_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = carAccentColor
                            )
                            Text(
                                text = stringResource(R.string.car_diagnosis_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (car.recommendationReason.isNotBlank()) {
                        item {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = stringResource(R.string.discovery_why_this_car),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = carAccentColor
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = car.recommendationReason,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}
