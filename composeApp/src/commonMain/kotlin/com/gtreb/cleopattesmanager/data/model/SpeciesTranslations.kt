package com.gtreb.cleopattesmanager.data.model

/**
 * Utility for handling species translations using resource files
 */
object SpeciesTranslations {
    
    /**
     * Get the localized name for a species ID
     * @param speciesId The species ID (e.g., "dog", "cat")
     * @param languageCode The language code (e.g., "en", "fr", "es")
     * @return The localized species name
     */
    fun getSpeciesName(speciesId: String?, languageCode: String = "en"): String {
        return when (speciesId) {
            SpeciesIds.DOG -> getSpeciesResource("species_dog", languageCode)
            SpeciesIds.CAT -> getSpeciesResource("species_cat", languageCode)
            SpeciesIds.BIRD -> getSpeciesResource("species_bird", languageCode)
            SpeciesIds.RODENT -> getSpeciesResource("species_rodent", languageCode)
            SpeciesIds.REPTILE -> getSpeciesResource("species_reptile", languageCode)
            SpeciesIds.OTHER -> getSpeciesResource("species_other", languageCode)
            else -> speciesId // Fallback to ID if unknown
        }!!
    }
    
    /**
     * Get all species names in a specific language
     */
    fun getAllSpeciesNames(languageCode: String = "en"): List<String> {
        return SpeciesIds.ALL_SPECIES_IDS.map { getSpeciesName(it, languageCode) }
    }
    
    /**
     * Get species name with resource key
     * This would be implemented with actual resource loading
     */
    private fun getSpeciesResource(resourceKey: String, languageCode: String): String {
        // In a real implementation, this would use the resource system
        // For now, we'll use a simple mapping
        return when (resourceKey) {
            "species_dog" -> when (languageCode) {
                "fr" -> "Chien"
                "es" -> "Perro"
                else -> "Dog"
            }
            "species_cat" -> when (languageCode) {
                "fr" -> "Chat"
                "es" -> "Gato"
                else -> "Cat"
            }
            "species_bird" -> when (languageCode) {
                "fr" -> "Oiseau"
                "es" -> "Pájaro"
                else -> "Bird"
            }
            "species_rodent" -> when (languageCode) {
                "fr" -> "Rongeur"
                "es" -> "Roedor"
                else -> "Rodent"
            }
            "species_reptile" -> when (languageCode) {
                "fr" -> "Reptile"
                "es" -> "Reptil"
                else -> "Reptile"
            }
            "species_other" -> when (languageCode) {
                "fr" -> "Autre"
                "es" -> "Otro"
                else -> "Other"
            }
            else -> resourceKey
        }
    }
}

/**
 * Extension function for Animal to get localized species name
 */
fun Animal.getLocalizedSpeciesName(languageCode: String = "en"): String {
    return SpeciesTranslations.getSpeciesName(speciesId, languageCode)
} 