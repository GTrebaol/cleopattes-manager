package com.gtreb.cleopattesmanager.data.repository

import com.gtreb.cleopattesmanager.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing time slots in the planning system
 */
interface TimeSlotRepository {
    
    /**
     * Get all time slots
     */
    suspend fun getAllTimeSlots(): List<TimeSlot>
    
    /**
     * Get all time slots as Flow
     */
    fun getAllTimeSlotsFlow(): Flow<List<TimeSlot>>
    
    /**
     * Get time slot by ID
     */
    suspend fun getTimeSlotById(id: BaseId): TimeSlot?
    
    /**
     * Get time slots for a specific date
     */
    suspend fun getTimeSlotsForDate(date: Long): List<TimeSlot>
    
    /**
     * Get time slots for a date range
     */
    suspend fun getTimeSlotsForDateRange(startDate: Long, endDate: Long): List<TimeSlot>
    
    /**
     * Get time slots for a specific client
     */
    suspend fun getTimeSlotsForClient(clientId: BaseId): List<TimeSlot>
    
    /**
     * Get time slots for a specific animal
     */
    suspend fun getTimeSlotsForAnimal(animalId: BaseId): List<TimeSlot>
    
    /**
     * Get time slots for a specific service
     */
    suspend fun getTimeSlotsForService(serviceId: BaseId): List<TimeSlot>
    
    /**
     * Get time slots by status
     */
    suspend fun getTimeSlotsByStatus(status: TimeSlotStatus): List<TimeSlot>
    
    /**
     * Get today's time slots
     */
    suspend fun getTodayTimeSlots(): List<TimeSlot>
    
    /**
     * Get upcoming time slots
     */
    suspend fun getUpcomingTimeSlots(): List<TimeSlot>
    
    /**
     * Get past time slots
     */
    suspend fun getPastTimeSlots(): List<TimeSlot>
    
    /**
     * Check for time slot conflicts
     */
    suspend fun hasTimeSlotConflict(startDateTime: Long, endDateTime: Long, excludeId: BaseId? = null): Boolean
    
    /**
     * Insert a new time slot
     */
    suspend fun insertTimeSlot(timeSlot: TimeSlot): BaseId
    
    /**
     * Update an existing time slot
     */
    suspend fun updateTimeSlot(timeSlot: TimeSlot): Boolean
    
    /**
     * Delete a time slot
     */
    suspend fun deleteTimeSlot(id: BaseId): Boolean
    
    /**
     * Update time slot status
     */
    suspend fun updateTimeSlotStatus(id: BaseId, status: TimeSlotStatus): Boolean
    
    /**
     * Get time slot count
     */
    suspend fun getTimeSlotCount(): Int
    
    /**
     * Get time slot count by status
     */
    suspend fun getTimeSlotCountByStatus(status: TimeSlotStatus): Int
    
    /**
     * Get time slots with all related data
     */
    suspend fun getTimeSlotsWithDetails(): List<TimeSlotWithDetails>
    
    /**
     * Get time slots with details for a date range
     */
    suspend fun getTimeSlotsWithDetailsForDateRange(startDate: Long, endDate: Long): List<TimeSlotWithDetails>
} 