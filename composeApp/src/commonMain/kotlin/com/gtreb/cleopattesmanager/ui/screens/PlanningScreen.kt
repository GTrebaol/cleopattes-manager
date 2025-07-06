@file:OptIn(ExperimentalTime::class)

package com.gtreb.cleopattesmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gtreb.cleopattesmanager.data.model.Animal
import com.gtreb.cleopattesmanager.data.model.BaseId
import com.gtreb.cleopattesmanager.data.model.Client
import com.gtreb.cleopattesmanager.data.model.Service
import com.gtreb.cleopattesmanager.data.model.TimeSlotWithDetails
import com.gtreb.cleopattesmanager.ui.Dimensions
import com.gtreb.cleopattesmanager.ui.components.TimeSlotDialog
import com.gtreb.cleopattesmanager.ui.viewmodel.CalendarViewType
import com.gtreb.cleopattesmanager.ui.viewmodel.PlanningViewModel
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Main planning screen with Google Calendar style interface
 */
@Composable
fun PlanningScreen(
    viewModel: PlanningViewModel,
    onNavigateToAnimal: (BaseId) -> Unit
) {
    val uiState by viewModel.planningUiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = Dimensions.TOP_PADDING.dp)
    ) {
        // Header with app title and actions
        PlanningHeader(
            onAddTimeSlot = { viewModel.showAddTimeSlotDialog() },
            onTodayClick = { viewModel.goToToday() }
        )
        
        // Main content
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.calendarView) {
                CalendarViewType.DAY -> DayView(
                    timeSlots = uiState.timeSlotsForSelectedDate,
                    onTimeSlotClick = { timeSlot ->
                        viewModel.selectTimeSlot(timeSlot)
                    },
                    onNavigateToAnimal = onNavigateToAnimal
                )
                CalendarViewType.WEEK -> WeekView(
                    timeSlots = uiState.timeSlotsForSelectedWeek,
                    onTimeSlotClick = { timeSlot ->
                        viewModel.selectTimeSlot(timeSlot)
                    },
                    onNavigateToAnimal = onNavigateToAnimal
                )
                CalendarViewType.MONTH -> MonthView(
                    timeSlots = uiState.timeSlotsForSelectedMonth,
                    onTimeSlotClick = { timeSlot ->
                        viewModel.selectTimeSlot(timeSlot)
                    },
                    onNavigateToAnimal = onNavigateToAnimal
                )
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = Dimensions.ALPHA_OVERLAY)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // Dialogs
    if (uiState.showAddTimeSlotDialog) {
        TimeSlotDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideAddTimeSlotDialog() }
        )
    }
    
    if (uiState.selectedTimeSlot != null) {
        TimeSlotDetailsDialog(
            timeSlot = uiState.selectedTimeSlot!!,
            onDismiss = { viewModel.clearSelectedTimeSlot() },
            onEdit = { viewModel.showEditTimeSlotDialog() },
            onDelete = { viewModel.deleteTimeSlot() },
            onNavigateToAnimal = onNavigateToAnimal
        )
    }
}

@Composable
private fun PlanningHeader(
    onAddTimeSlot: () -> Unit,
    onTodayClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.HEADER_PADDING.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // App title
            Text(
                text = Dimensions.APP_TITLE,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.HEADER_BUTTON_SPACING.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onTodayClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(
                        horizontal = Dimensions.HEADER_BUTTON_HORIZONTAL_PADDING.dp, 
                        vertical = Dimensions.HEADER_BUTTON_VERTICAL_PADDING.dp
                    )
                ) {
                    Text(Dimensions.TODAY_BUTTON_TEXT)
                }
                Button(
                    onClick = onAddTimeSlot,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(
                        horizontal = Dimensions.HEADER_BUTTON_HORIZONTAL_PADDING.dp, 
                        vertical = Dimensions.HEADER_BUTTON_VERTICAL_PADDING.dp
                    )
                ) {
                    Text(Dimensions.ADD_BUTTON_TEXT)
                }
            }
        }
    }
}

@Composable
private fun DayView(
    timeSlots: List<TimeSlotWithDetails>,
    onTimeSlotClick: (TimeSlotWithDetails) -> Unit,
    onNavigateToAnimal: (BaseId) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Dimensions.DAY_VIEW_HORIZONTAL_PADDING.dp, 
            vertical = Dimensions.DAY_VIEW_VERTICAL_PADDING.dp
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp) // No spacing between rows
    ) {
        // Time slots from 8:00 to 20:00
        for (hour in Dimensions.START_HOUR..Dimensions.END_HOUR) {
            item {
                TimeSlotRow(
                    hour = hour,
                    timeSlots = timeSlots.filter {
                        val slotHour = Instant.fromEpochMilliseconds(it.timeSlot.startDateTime).toLocalDateTime(TimeZone.currentSystemDefault()).hour
                        slotHour == hour
                    },
                    onTimeSlotClick = onTimeSlotClick,
                    onNavigateToAnimal = onNavigateToAnimal
                )
            }
        }
    }
}

@Composable
private fun TimeSlotRow(
    hour: Int,
    timeSlots: List<TimeSlotWithDetails>,
    onTimeSlotClick: (TimeSlotWithDetails) -> Unit,
    onNavigateToAnimal: (BaseId) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.TIME_SLOT_ROW_HEIGHT.dp) // Fixed height for the row
    ) {
        // Time label
        Box(
            modifier = Modifier
                .width(Dimensions.TIME_LABEL_WIDTH.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d:00", hour),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Time slots for this hour
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = Dimensions.TIME_SLOT_CARD_BORDER_WIDTH.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
        ) {
            // Stack time slots vertically if there are multiple
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                timeSlots.forEach { timeSlot ->
                    TimeSlotCard(
                        timeSlot = timeSlot,
                        onClick = { onTimeSlotClick(timeSlot) },
                        onAnimalClick = { onNavigateToAnimal(timeSlot.animal.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun TimeSlotCard(
    timeSlot: TimeSlotWithDetails,
    onClick: () -> Unit,
    onAnimalClick: () -> Unit
) {
    val startTime = Instant.fromEpochMilliseconds(timeSlot.timeSlot.startDateTime)
    val endTime = Instant.fromEpochMilliseconds(timeSlot.timeSlot.endDateTime)

    // Calculate duration in minutes
    val durationMinutes = (endTime - startTime).inWholeMinutes

    val cardHeight = when {
        durationMinutes <= Dimensions.SHORT_DURATION_THRESHOLD -> Dimensions.SHORT_DURATION_CARD_HEIGHT.dp
        durationMinutes <= Dimensions.MEDIUM_DURATION_THRESHOLD -> Dimensions.MEDIUM_DURATION_CARD_HEIGHT.dp
        else -> Dimensions.LONG_DURATION_CARD_HEIGHT.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(
                horizontal = Dimensions.TIME_SLOT_CARD_HORIZONTAL_PADDING.dp, 
                vertical = Dimensions.TIME_SLOT_CARD_VERTICAL_PADDING.dp
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = Dimensions.TIME_SLOT_CARD_ALPHA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.TIME_SLOT_CARD_ELEVATION.dp),
        shape = CutCornerShape(Dimensions.TIME_SLOT_CARD_CORNER_RADIUS.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.TIME_SLOT_CARD_INTERNAL_PADDING.dp)
        ) {
            Text(
                text = timeSlot.animal.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun WeekView(
    timeSlots: List<TimeSlotWithDetails>,
    onTimeSlotClick: (TimeSlotWithDetails) -> Unit,
    onNavigateToAnimal: (BaseId) -> Unit
) {
    // Simplified week view - similar to day view but with day headers
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimensions.DAY_VIEW_VERTICAL_PADDING.dp)
    ) {
        item {
            Text(
                text = Dimensions.WEEK_VIEW_TITLE,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = Dimensions.DAY_VIEW_VERTICAL_PADDING.dp)
            )
        }
        
        items(timeSlots) { timeSlot ->
            TimeSlotCard(
                timeSlot = timeSlot,
                onClick = { onTimeSlotClick(timeSlot) },
                onAnimalClick = { onNavigateToAnimal(timeSlot.animal.id) }
            )
            Spacer(Modifier.height(Dimensions.HEADER_BUTTON_SPACING.dp))
        }
    }
}

@Composable
private fun MonthView(
    timeSlots: List<TimeSlotWithDetails>,
    onTimeSlotClick: (TimeSlotWithDetails) -> Unit,
    onNavigateToAnimal: (BaseId) -> Unit
) {
    // Simplified month view
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimensions.DAY_VIEW_VERTICAL_PADDING.dp)
    ) {
        item {
            Text(
                text = Dimensions.MONTH_VIEW_TITLE,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = Dimensions.DAY_VIEW_VERTICAL_PADDING.dp)
            )
        }
        
        items(timeSlots) { timeSlot ->
            TimeSlotCard(
                timeSlot = timeSlot,
                onClick = { onTimeSlotClick(timeSlot) },
                onAnimalClick = { onNavigateToAnimal(timeSlot.animal.id) }
            )
            Spacer(Modifier.height(Dimensions.HEADER_BUTTON_SPACING.dp))
        }
    }
}

@Composable
private fun TimeSlotDetailsDialog(
    timeSlot: TimeSlotWithDetails,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToAnimal: (BaseId) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(Dimensions.TIME_SLOT_DETAILS_TITLE)
        },
        text = {
            Column {
                Text("${Dimensions.SERVICE_LABEL}${timeSlot.service.name}")
                Text("${Dimensions.ANIMAL_LABEL}${timeSlot.animal.name}")
                Text("${Dimensions.CLIENT_LABEL}${timeSlot.client.firstName} ${timeSlot.client.lastName}")
                if (timeSlot.timeSlot.notes.isNotBlank()) {
                    Text("${Dimensions.NOTES_LABEL}${timeSlot.timeSlot.notes}")
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onNavigateToAnimal(timeSlot.animal.id) }) {
                    Text(Dimensions.VIEW_ANIMAL_BUTTON_TEXT)
                }
                TextButton(onClick = onEdit) {
                    Text(Dimensions.EDIT_BUTTON_TEXT)
                }
                TextButton(onClick = onDelete) {
                    Text(Dimensions.DELETE_BUTTON_TEXT)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Dimensions.CLOSE_BUTTON_TEXT)
            }
        }
    )
}

/**
 * Alternative planning screen with filters
 */
@Composable
fun PlanningScreenWithFilters(
    viewModel: PlanningViewModel,
    onNavigateToAnimal: (BaseId) -> Unit
) {
    val uiState by viewModel.planningUiState.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        PlanningFilters(
            clients = uiState.clients,
            animals = uiState.animals,
            services = uiState.services,
            onClientFilter = { clientId ->
                viewModel.handleEvent(PlanningViewModel.Event.FilterByClient(clientId))
            },
            onAnimalFilter = { animalId ->
                viewModel.handleEvent(PlanningViewModel.Event.FilterByAnimal(animalId))
            },
            onServiceFilter = { serviceId ->
                viewModel.handleEvent(PlanningViewModel.Event.FilterByService(serviceId))
            },
            onClearFilters = {
                viewModel.handleEvent(PlanningViewModel.Event.ClearFilters)
            }
        )
        
        PlanningScreen(viewModel, onNavigateToAnimal)
    }
}

/**
 * Planning filters component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningFilters(
    clients: List<Client>,
    animals: List<Animal>,
    services: List<Service>,
    onClientFilter: (BaseId?) -> Unit,
    onAnimalFilter: (BaseId?) -> Unit,
    onServiceFilter: (BaseId?) -> Unit,
    onClearFilters: () -> Unit
) {
    var selectedClientId by remember { mutableStateOf<BaseId?>(null) }
    var selectedAnimalId by remember { mutableStateOf<BaseId?>(null) }
    var selectedServiceId by remember { mutableStateOf<BaseId?>(null) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.DAY_VIEW_VERTICAL_PADDING.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.DAY_VIEW_VERTICAL_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(Dimensions.HEADER_BUTTON_SPACING.dp)
        ) {
            Text(
                text = Dimensions.FILTERS_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Client filter
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                OutlinedTextField(
                    value = clients.find { it.id == selectedClientId }?.let { "${it.firstName} ${it.lastName}" } ?: Dimensions.ALL_CLIENTS_TEXT,
                    onValueChange = { },
                    label = { Text(Dimensions.CLIENT_LABEL_TEXT) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Animal filter
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                OutlinedTextField(
                    value = animals.find { it.id == selectedAnimalId }?.name ?: Dimensions.ALL_ANIMALS_TEXT,
                    onValueChange = { },
                    label = { Text(Dimensions.ANIMAL_LABEL_TEXT) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Service filter
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                OutlinedTextField(
                    value = services.find { it.id == selectedServiceId }?.name ?: Dimensions.ALL_SERVICES_TEXT,
                    onValueChange = { },
                    label = { Text(Dimensions.SERVICE_LABEL_TEXT) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Clear filters button
            TextButton(
                onClick = {
                    selectedClientId = null
                    selectedAnimalId = null
                    selectedServiceId = null
                    onClearFilters()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(Dimensions.CLEAR_FILTERS_TEXT)
            }
        }
    }
} 