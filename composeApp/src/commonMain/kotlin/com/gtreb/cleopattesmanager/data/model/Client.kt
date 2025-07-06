package com.gtreb.cleopattesmanager.data.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class Client(
    val id: BaseId,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val notes: String = "",
    val isActive: Boolean = true
) 