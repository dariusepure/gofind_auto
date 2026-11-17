package com.dariusepure.gofindauto.ui.cars

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dariusepure.gofindauto.R
import com.dariusepure.gofindauto.domain.Car
import com.dariusepure.gofindauto.domain.bodyType
import com.dariusepure.gofindauto.domain.displayName
import com.dariusepure.gofindauto.ui.common.ActionButtons
import com.dariusepure.gofindauto.ui.common.AutoSizeText
import com.dariusepure.gofindauto.ui.common.CarCardSkeleton
import com.dariusepure.gofindauto.ui.common.CarFormatters
import com.dariusepure.gofindauto.ui.common.CarTranslations
import com.dariusepure.gofindauto.ui.common.DeleteConfirmationDialog
import com.dariusepure.gofindauto.ui.common.EmptyState
import com.dariusepure.gofindauto.ui.common.ErrorState
import com.dariusepure.gofindauto.ui.common.SpecChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarCard(
    car: Car,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val logoRes = remember(car.make) { CarFormatters.getBrandLogoResource(car.make) }

    val specPills = remember(car, context) {
        val list = mutableListOf<Pair<ImageVector, String>>()
        if (car.year > 0) {
            list.add(Pair(Icons.Default.CalendarToday, car.year.toString()))
        }
        val body = car.bodyType
        if (body.isNotBlank()) {
            list.add(Pair(Icons.Outlined.DirectionsCar, CarTranslations.getVehicleTypeLabel(context, body)))
        }
        if (car.fuelType.isNotBlank()) {
            list.add(Pair(Icons.Default.LocalGasStation, CarTranslations.getFuelTypeLabel(context, car.fuelType)))
        }
        if (car.power > 0) {
            list.add(Pair(Icons.Default.Speed, CarFormatters.formatPower(context, car)))
        }
        list
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Logo Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoRes != null) {
                        Image(
                            painter = painterResource(logoRes),
                            contentDescription = car.make,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(3.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = car.make,
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                AutoSizeText(
                    text = car.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                ActionButtons(
                    onEdit = onEditClick,
                    onDelete = onDeleteClick
                )
            }

            if (specPills.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    specPills.forEach { (icon, text) ->
                        SpecChip(icon = icon, text = text)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListScreen(
    onCarClick: (String) -> Unit,
    onAddCarClick: (() -> Unit)? = null,
    onEditCarClick: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: CarListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var carToDelete by remember { mutableStateOf<String?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    if (carToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.onDeleteCar(carToDelete!!)
                carToDelete = null
            },
            onDismiss = { carToDelete = null }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    AutoSizeText(
                        text = stringResource(R.string.car_list_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.common_sort)
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            CarSortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = stringResource(order.labelRes),
                                            fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    onClick = {
                                        viewModel.onSortOrderChanged(order)
                                        sortMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (order == sortOrder) {
                                            Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.car_list_search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val s = state) {
                    CarListUiState.Loading -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(5) { CarCardSkeleton() }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                    CarListUiState.Empty -> EmptyState(
                        title = if (searchQuery.isEmpty()) stringResource(R.string.car_list_empty_title) else stringResource(R.string.car_list_no_results_title),
                        subtitle = if (searchQuery.isEmpty()) stringResource(R.string.car_list_empty_subtitle) else stringResource(R.string.car_list_no_results_subtitle),
                        icon = Icons.Outlined.DirectionsCar,
                    )
                    is CarListUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = s.cars,
                                key = { it.id }
                            ) { car ->
                                CarCard(
                                    car = car,
                                    onClick = { onCarClick(car.id) },
                                    onEditClick = { onEditCarClick(car.id) },
                                    onDeleteClick = { carToDelete = car.id }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                    is CarListUiState.Error -> ErrorState(
                        message = s.message,
                        onRetry = { },
                    )
                }
            }
        }
    }
}
