package com.gtreb.cleopattesmanager.data.repository

import com.gtreb.cleopattesmanager.data.model.*

/**
 * Main repository that combines all entity repositories
 * Provides business logic and cross-entity operations
 */
interface AppRepository {
    
    // Individual repositories
    val clientRepository: ClientRepository
    val animalRepository: AnimalRepository
    val serviceRepository: ServiceRepository
    val prestationRepository: PrestationRepository
    val timeSlotRepository: TimeSlotRepository
    
    // Business logic operations
    
    /**
     * Get dashboard statistics
     */
    suspend fun getDashboardStats(): DashboardStats
    
    /**
     * Get client with all their animals
     */
    suspend fun getClientWithAnimals(clientId: BaseId): ClientWithAnimals?
    
    /**
     * Create a new client with their first animal
     */
    suspend fun createClientWithAnimal(
        client: Client,
        animal: Animal
    ): Pair<BaseId, BaseId>
    
    /**
     * Get prestation details with all related entities
     */
    suspend fun getPrestationDetails(prestationId: BaseId): PrestationWithDetails?
    
    /**
     * Create a new prestation with validation
     */
    suspend fun createPrestation(
        clientId: BaseId,
        animalId: BaseId,
        serviceId: BaseId,
        startDate: Long,
        endDate: Long,
        price: Double,
        notes: String = ""
    ): BaseId
    
    /**
     * Get time slot details with all related entities
     */
    suspend fun getTimeSlotDetails(timeSlotId: BaseId): TimeSlotWithDetails?
    
    /**
     * Create a new time slot with validation
     */
    suspend fun createTimeSlot(
        clientId: BaseId,
        animalId: BaseId,
        serviceId: BaseId,
        startDateTime: Long,
        endDateTime: Long,
        notes: String = ""
    ): BaseId
    
    /**
     * Get revenue statistics
     */
    suspend fun getRevenueStats(period: RevenuePeriod): RevenueStats
    
    /**
     * Search across all entities
     */
    suspend fun globalSearch(query: String): GlobalSearchResults
    
    /**
     * Export data for backup
     */
    suspend fun exportData(): AppDataExport
    
    /**
     * Import data from backup
     */
    suspend fun importData(data: AppDataExport): Boolean
    
    /**
     * Get data sync status
     */
    suspend fun getSyncStatus(): SyncStatus
}

/**
 * Dashboard statistics
 */
data class DashboardStats(
    val totalClients: Int,
    val totalAnimals: Int,
    val totalServices: Int,
    val totalPrestations: Int,
    val todayPrestations: Int,
    val upcomingPrestations: Int,
    val monthlyRevenue: Double,
    val activeClients: Int
)

/**
 * Client with all their animals
 */
data class ClientWithAnimals(
    val client: Client,
    val animals: List<Animal>
)

/**
 * Revenue period options
 */
enum class RevenuePeriod {
    TODAY, WEEK, MONTH, QUARTER, YEAR, CUSTOM
}

/**
 * Revenue statistics
 */
data class RevenueStats(
    val totalRevenue: Double,
    val averageRevenue: Double,
    val prestationCount: Int,
    val topServices: List<ServiceRevenue>,
    val monthlyBreakdown: Map<Int, Double>
)

/**
 * Service revenue information
 */
data class ServiceRevenue(
    val service: Service,
    val revenue: Double,
    val count: Int
)

/**
 * Global search results
 */
data class GlobalSearchResults(
    val clients: List<Client>,
    val animals: List<Animal>,
    val services: List<Service>,
    val prestations: List<Prestation>
)

/**
 * App data export for backup
 */
data class AppDataExport(
    val clients: List<Client>,
    val animals: List<Animal>,
    val services: List<Service>,
    val prestations: List<Prestation>,
    val exportDate: Long,
    val version: String
)

/**
 * Sync status information
 */
data class SyncStatus(
    val lastSyncDate: Long?,
    val isSyncing: Boolean,
    val pendingChanges: Int,
    val errorMessage: String? = null
) 