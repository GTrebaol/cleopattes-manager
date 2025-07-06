@file:OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)

package com.gtreb.cleopattesmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gtreb.cleopattesmanager.data.model.*
import com.gtreb.cleopattesmanager.ui.viewmodel.PlanningViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Dialog for adding or editing time slots
 */
@Composable
fun TimeSlotDialog(
    viewModel: PlanningViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.planningUiState.collectAsState()
    val isEditing = uiState.selectedTimeSlot != null
    val timeSlot = uiState.selectedTimeSlot?.timeSlot
    
    var selectedClientId by remember { mutableStateOf(timeSlot?.clientId ?: BaseId("")) }
    var selectedAnimalId by remember { mutableStateOf(timeSlot?.animalId ?: BaseId("")) }
    var selectedServiceId by remember { mutableStateOf(timeSlot?.serviceId ?: BaseId("")) }
    var startDateTime by remember { mutableStateOf(timeSlot?.startDateTime ?: Clock.System.now().toEpochMilliseconds()) }
    var endDateTime by remember { mutableStateOf(
        timeSlot?.endDateTime ?: (Clock.System.now().toEpochMilliseconds() + (60 * 60 * 1000))
    ) }
    var notes by remember { mutableStateOf(timeSlot?.notes ?: "") }
    
    // Filter animals based on selected client
    val filteredAnimals = remember(selectedClientId) {
        if (selectedClientId.value.isNotEmpty()) {
            uiState.animals.filter { it.clientId == selectedClientId }
        } else {
            emptyList()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Modifier le rendez-vous" else "Nouveau rendez-vous"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Client selection
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = uiState.clients.find { it.id == selectedClientId }?.let { "${it.firstName} ${it.lastName}" } ?: "",
                        onValueChange = { },
                        label = { Text("Client") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Animal selection
                if (selectedClientId.value.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { }
                    ) {
                        OutlinedTextField(
                            value = filteredAnimals.find { it.id == selectedAnimalId }?.name ?: "",
                            onValueChange = { },
                            label = { Text("Animal") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Service selection
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = uiState.services.find { it.id == selectedServiceId }?.name ?: "",
                        onValueChange = { },
                        label = { Text("Service") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Date and time selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formatDateTime(startDateTime),
                        onValueChange = { },
                        label = { Text("Début") },
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = formatDateTime(endDateTime),
                        onValueChange = { },
                        label = { Text("Fin") },
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newTimeSlot = TimeSlot(
                        id = timeSlot?.id ?: BaseId.generate(),
                        clientId = selectedClientId,
                        animalId = selectedAnimalId,
                        serviceId = selectedServiceId,
                        startDateTime = startDateTime,
                        endDateTime = endDateTime,
                        notes = notes,
                        status = timeSlot?.status ?: TimeSlotStatus.SCHEDULED,
                        createdAt = timeSlot?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
                        updatedAt = Clock.System.now().toEpochMilliseconds()
                    )
                    
                    if (isEditing) {
                        viewModel.handleEvent(PlanningViewModel.Event.UpdateTimeSlot(newTimeSlot))
                    } else {
                        viewModel.handleEvent(PlanningViewModel.Event.AddTimeSlot(newTimeSlot))
                    }
                    onDismiss()
                },
                enabled = selectedClientId.value.isNotEmpty() && 
                         selectedAnimalId.value.isNotEmpty() && 
                         selectedServiceId.value.isNotEmpty()
            ) {
                Text(if (isEditing) "Modifier" else "Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Format date time for display
 */
fun formatDateTime(timestamp: Long): String {
    val dateTime =  Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = dateTime.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.date} ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
} 