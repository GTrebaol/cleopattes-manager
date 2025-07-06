package com.gtreb.cleopattesmanager.data.repository

import com.gtreb.cleopattesmanager.data.model.Animal
import com.gtreb.cleopattesmanager.data.model.Prestation
import com.gtreb.cleopattesmanager.data.model.BaseId
import com.gtreb.cleopattesmanager.data.model.PrestationStatus
import com.gtreb.cleopattesmanager.data.model.Client
import com.gtreb.cleopattesmanager.data.model.Service
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing Prestation entities
 * Provides CRUD operations and business logic for pet sitting services
 */
interface PrestationRepository {
    
    /**
     * Get all prestations
     */
    suspend fun getAllPrestations(): List<Prestation>
    
    /**
     * Get all prestations as a Flow for reactive updates
     */
    fun getAllPrestationsFlow(): Flow<List<Prestation>>
    
    /**
     * Get a prestation by ID
     */
    suspend fun getPrestationById(id: BaseId): Prestation?
    
    /**
     * Get prestations by status
     */
    suspend fun getPrestationsByStatus(status: PrestationStatus): List<Prestation>
    
    /**
     * Get prestations for a specific client
     */
    suspend fun getPrestationsByClient(clientId: BaseId): List<Prestation>
    
    /**
     * Get prestations for a specific animal
     */
    suspend fun getPrestationsByAnimal(animalId: BaseId): List<Prestation>
    
    /**
     * Get prestations for a specific service
     */
    suspend fun getPrestationsByService(serviceId: BaseId): List<Prestation>
    
    /**
     * Get prestations in a date range
     */
    suspend fun getPrestationsByDateRange(startDate: Long, endDate: Long): List<Prestation>
    
    /**
     * Get today's prestations
     */
    suspend fun getTodayPrestations(): List<Prestation>
    
    /**
     * Get upcoming prestations (future dates)
     */
    suspend fun getUpcomingPrestations(): List<Prestation>
    
    /**
     * Get past prestations (completed)
     */
    suspend fun getPastPrestations(): List<Prestation>
    
    /**
     * Search prestations by notes or client name
     */
    suspend fun searchPrestations(query: String): List<Prestation>
    
    /**
     * Insert a new prestation
     */
    suspend fun insertPrestation(prestation: Prestation): BaseId
    
    /**
     * Update an existing prestation
     */
    suspend fun updatePrestation(prestation: Prestation): Boolean
    
    /**
     * Delete a prestation by ID
     */
    suspend fun deletePrestation(id: BaseId): Boolean
    
    /**
     * Update prestation status
     */
    suspend fun updatePrestationStatus(id: BaseId, status: PrestationStatus): Boolean
    
    /**
     * Mark prestation as in progress
     */
    suspend fun startPrestation(id: BaseId): Boolean
    
    /**
     * Mark prestation as completed
     */
    suspend fun completePrestation(id: BaseId): Boolean
    
    /**
     * Cancel a prestation
     */
    suspend fun cancelPrestation(id: BaseId): Boolean
    
    /**
     * Get prestation count
     */
    suspend fun getPrestationCount(): Int
    
    /**
     * Get prestation count by status
     */
    suspend fun getPrestationCountByStatus(status: PrestationStatus): Int
    
    /**
     * Get total revenue for a date range
     */
    suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double
    
    /**
     * Get monthly revenue statistics
     */
    suspend fun getMonthlyRevenue(year: Int): Map<Int, Double>
    
    /**
     * Get prestations with full details (client, animal, service)
     */
    suspend fun getPrestationsWithDetails(): List<PrestationWithDetails>
}

/**
 * Data class containing prestation with all related entities
 */
data class PrestationWithDetails(
    val prestation: Prestation,
    val client: Client,
    val animal: Animal,
    val service: Service
) 