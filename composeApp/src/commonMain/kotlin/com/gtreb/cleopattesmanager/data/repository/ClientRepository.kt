package com.gtreb.cleopattesmanager.data.repository

import com.gtreb.cleopattesmanager.data.model.Client
import com.gtreb.cleopattesmanager.data.model.BaseId
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing Client entities
 * Provides CRUD operations and data access abstraction
 */
interface ClientRepository {
    
    /**
     * Get all active clients
     */
    suspend fun getAllClients(): List<Client>
    
    /**
     * Get all clients as a Flow for reactive updates
     */
    fun getAllClientsFlow(): Flow<List<Client>>
    
    /**
     * Get a client by ID
     */
    suspend fun getClientById(id: BaseId): Client?
    
    /**
     * Get clients by search term (name, email, phone)
     */
    suspend fun searchClients(query: String): List<Client>
    
    /**
     * Insert a new client
     */
    suspend fun insertClient(client: Client): BaseId
    
    /**
     * Update an existing client
     */
    suspend fun updateClient(client: Client): Boolean
    
    /**
     * Delete a client by ID
     */
    suspend fun deleteClient(id: BaseId): Boolean
    
    /**
     * Soft delete a client (mark as inactive)
     */
    suspend fun deactivateClient(id: BaseId): Boolean
    
    /**
     * Get client count
     */
    suspend fun getClientCount(): Int
    
    /**
     * Check if a client exists by email
     */
    suspend fun clientExistsByEmail(email: String): Boolean
} 