package com.gtreb.cleopattesmanager.data.repository

import com.gtreb.cleopattesmanager.data.model.Animal
import com.gtreb.cleopattesmanager.data.model.BaseId
import com.gtreb.cleopattesmanager.data.model.Client
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing Animal entities
 * Provides CRUD operations and data access abstraction
 */
interface AnimalRepository {
    
    /**
     * Get all active animals
     */
    suspend fun getAllAnimals(): List<Animal>
    
    /**
     * Get all animals as a Flow for reactive updates
     */
    fun getAllAnimalsFlow(): Flow<List<Animal>>
    
    /**
     * Get an animal by ID
     */
    suspend fun getAnimalById(id: BaseId): Animal?
    
    /**
     * Get all animals for a specific client
     */
    suspend fun getAnimalsByClient(clientId: BaseId): List<Animal>
    
    /**
     * Get animals by species
     */
    suspend fun getAnimalsBySpecies(speciesId: String): List<Animal>
    
    /**
     * Search animals by name or breed
     */
    suspend fun searchAnimals(query: String): List<Animal>
    
    /**
     * Insert a new animal
     */
    suspend fun insertAnimal(animal: Animal): BaseId
    
    /**
     * Update an existing animal
     */
    suspend fun updateAnimal(animal: Animal): Boolean
    
    /**
     * Delete an animal by ID
     */
    suspend fun deleteAnimal(id: BaseId): Boolean
    
    /**
     * Soft delete an animal (mark as inactive)
     */
    suspend fun deactivateAnimal(id: BaseId): Boolean
    
    /**
     * Get animal count
     */
    suspend fun getAnimalCount(): Int
    
    /**
     * Get animal count by client
     */
    suspend fun getAnimalCountByClient(clientId: BaseId): Int
    
    /**
     * Get animals with their client information
     */
    suspend fun getAnimalsWithClients(): List<Pair<Animal, Client>>
} 