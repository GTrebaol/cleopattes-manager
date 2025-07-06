package com.gtreb.cleopattesmanager.data.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Base class for all typed IDs following Android best practices
 * - Uses UUID for uniqueness
 * - Resettable by user (app data clear)
 * - No hardware identifiers
 * - App-scoped only
 */
@Serializable
@JvmInline
value class BaseId(val value: String) {
    
    /**
     * Validates that this ID follows proper UUID format
     */
    fun isValid(): Boolean = value.matches(UUID_REGEX)
    
    companion object {
        private val UUID_REGEX = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        
        /**
         * Creates a BaseId from a string, with validation
         * @throws IllegalArgumentException if the string is not a valid UUID
         */
        fun fromString(id: String): BaseId {
            require(id.matches(UUID_REGEX)) {
                "Invalid UUID format: $id"
            }
            return BaseId(id)
        }
        
        /**
         * Generates a new BaseId with a random UUID
         */
        fun generate(): BaseId = BaseId(IdGenerator.generateId())
    }
}
