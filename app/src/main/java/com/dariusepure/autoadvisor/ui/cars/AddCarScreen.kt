package com.dariusepure.autoadvisor.ui.cars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedUsage by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("") }

    val usages = listOf(
        stringResource(R.string.discovery_usage_city),
        stringResource(R.string.discovery_usage_highway),
        stringResource(R.string.discovery_usage_family)
    )

    val priorities = listOf(
        stringResource(R.string.discovery_priority_economy),
        stringResource(R.string.discovery_priority_reliability),
        stringResource(R.string.discovery_priority_luxury)
    )

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
                        selectedUsage = selectedUsage,
                        onUsageChange = { selectedUsage = it },
                        selectedPriority = selectedPriority,
                        onPriorityChange = { selectedPriority = it },
                        onGenerate = { viewModel.getRecommendations(selectedUsage, selectedPriority) },
                        usages = usages,
                        priorities = priorities
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoveryQuestionnaire(
    selectedUsage: String,
    onUsageChange: (String) -> Unit,
    selectedPriority: String,
    onPriorityChange: (String) -> Unit,
    onGenerate: () -> Unit,
    usages: List<String>,
    priorities: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.discovery_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Usage
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.discovery_usage_label), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                usages.forEach { usage ->
                    FilterChip(
                        selected = selectedUsage == usage,
                        onClick = { onUsageChange(usage) },
                        label = { Text(usage) }
                    )
                }
            }
        }

        // Priority
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.discovery_priority_label), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                priorities.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { onPriorityChange(priority) },
                        label = { Text(priority) }
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = selectedUsage.isNotBlank() && selectedPriority.isNotBlank(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.AutoAwesome, null)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.discovery_generate_button), fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
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
