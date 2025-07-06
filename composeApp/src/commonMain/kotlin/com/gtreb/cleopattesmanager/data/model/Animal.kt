package com.gtreb.cleopattesmanager.data.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.Double
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class Animal(
    val id: BaseId,
    val name: String = "",
    val speciesId: String = "",
    val breed:  String = "",
    val birthDate: Long = Clock.System.now().toEpochMilliseconds(),
    val weight: Double = 0.toDouble(),
    val color:  String = "",
    val clientId: BaseId? = null, // Relation vers le Client
    val notes:  String = "",
    val isActive: Boolean = true
) {
    /**
     * Get the localized species name
     */
    fun getSpeciesName(languageCode: String): String {
        return getLocalizedSpeciesName(speciesId, languageCode)
    }
    
    /**
     * Get the English species name
     */
    fun getSpeciesName(): String = getSpeciesName("en")
} 