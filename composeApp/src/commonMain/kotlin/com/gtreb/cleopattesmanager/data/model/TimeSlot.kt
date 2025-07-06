package com.gtreb.cleopattesmanager.data.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Represents a time slot in the planning system
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class TimeSlot(
    val id: BaseId,
    val clientId: BaseId,
    val animalId: BaseId,
    val serviceId: BaseId,
    val startDateTime: Long, // Unix timestamp
    val endDateTime: Long,   // Unix timestamp
    val notes: String = "",
    val status: TimeSlotStatus = TimeSlotStatus.SCHEDULED,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)


/**
 * Status of a time slot
 */
enum class TimeSlotStatus {
    SCHEDULED,    // Scheduled but not started
    IN_PROGRESS,  // Currently in progress
    COMPLETED,    // Completed successfully
    CANCELLED,    // Cancelled
    NO_SHOW       // Client didn't show up
}

/**
 * Calendar view types
 */
enum class CalendarViewType {
    DAY,
    WEEK,
    MONTH
}

/**
 * Time slot with all related data
 */
data class TimeSlotWithDetails(
    val timeSlot: TimeSlot,
    val client: Client,
    val animal: Animal,
    val service: Service
) 