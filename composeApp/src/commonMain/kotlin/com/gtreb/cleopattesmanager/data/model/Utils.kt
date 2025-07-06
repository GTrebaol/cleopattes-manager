package com.gtreb.cleopattesmanager.data.model

import com.benasher44.uuid.uuid4

// Utilitaires pour la génération d'IDs
object IdGenerator {
    fun generateId(): String = uuid4().toString()
}

// Extensions utiles pour les modèles
fun Client.fullName(): String = "$firstName $lastName"
fun Animal.fullName(languageCode: String = "en"): String = "$name (${getLocalizedSpeciesName(languageCode)})"

// Utility function to get localized species name by speciesId
fun getLocalizedSpeciesName(speciesId: String?, languageCode: String = "en"): String {
    return SpeciesTranslations.getSpeciesName(speciesId, languageCode)
}

// Constantes pour les IDs d'espèces (language-independent)
object SpeciesIds {
    const val DOG = "dog"
    const val CAT = "cat"
    const val BIRD = "bird"
    const val RODENT = "rodent"
    const val REPTILE = "reptile"
    const val OTHER = "other"
    
    val ALL_SPECIES_IDS = listOf(DOG, CAT, BIRD, RODENT, REPTILE, OTHER)
} 