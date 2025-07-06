package com.gtreb.cleopattesmanager.data.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class Prestation(
    val id: BaseId,
    val clientId: BaseId, // Relation vers le Client
    val animalId: BaseId, // Relation vers l'Animal
    val serviceId: BaseId, // Relation vers le Service
    val startDate: Long,
    val endDate: Long,
    val price: Double,
    val status: PrestationStatus,
    val notes: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
enum class PrestationStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
} 