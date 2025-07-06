package com.gtreb.cleopattesmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: BaseId,
    val name: String = "",
    val description: String = "",
    val durationMinutes: Int = 0,
    val price: Double = 0.toDouble(),
    val isActive: Boolean = true
)
