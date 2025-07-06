package com.gtreb.cleopattesmanager.data.repository

import com.gtreb.cleopattesmanager.data.model.Service
import com.gtreb.cleopattesmanager.data.model.BaseId
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing Service entities
 * Provides CRUD operations and data access abstraction
 */
interface ServiceRepository {
    
    /**
     * Get all active services
     */
    suspend fun getAllServices(): List<Service>
    
    /**
     * Get all services as a Flow for reactive updates
     */
    fun getAllServicesFlow(): Flow<List<Service>>
    
    /**
     * Get a service by ID
     */
    suspend fun getServiceById(id: BaseId): Service?
    
    /**
     * Get services by price range
     */
    suspend fun getServicesByPriceRange(minPrice: Double, maxPrice: Double): List<Service>
    
    /**
     * Get services by duration range
     */
    suspend fun getServicesByDurationRange(minMinutes: Int, maxMinutes: Int): List<Service>
    
    /**
     * Search services by name or description
     */
    suspend fun searchServices(query: String): List<Service>
    
    /**
     * Insert a new service
     */
    suspend fun insertService(service: Service): BaseId
    
    /**
     * Update an existing service
     */
    suspend fun updateService(service: Service): Boolean
    
    /**
     * Delete a service by ID
     */
    suspend fun deleteService(id: BaseId): Boolean
    
    /**
     * Soft delete a service (mark as inactive)
     */
    suspend fun deactivateService(id: BaseId): Boolean
    
    /**
     * Get service count
     */
    suspend fun getServiceCount(): Int
    
    /**
     * Get average service price
     */
    suspend fun getAverageServicePrice(): Double
    
    /**
     * Get most popular services (by usage in prestations)
     */
    suspend fun getMostPopularServices(limit: Int = 5): List<Service>
} 