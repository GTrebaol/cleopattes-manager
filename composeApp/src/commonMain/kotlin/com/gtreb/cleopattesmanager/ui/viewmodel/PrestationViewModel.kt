package com.gtreb.cleopattesmanager.ui.viewmodel

import com.gtreb.cleopattesmanager.data.repository.AppRepository
import com.gtreb.cleopattesmanager.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ViewModel for prestation management
 */
@OptIn(ExperimentalTime::class)
class PrestationViewModel(
    private val repository: AppRepository
) : BaseViewModel<PrestationViewModel.State, PrestationViewModel.Event>() {
    
    private val _prestations = MutableStateFlow<List<Prestation>>(emptyList())
    val prestations: StateFlow<List<Prestation>> = _prestations.asStateFlow()
    
    private val _selectedPrestation = MutableStateFlow<Prestation?>(null)
    val selectedPrestation: StateFlow<Prestation?> = _selectedPrestation.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredPrestations = MutableStateFlow<List<Prestation>>(emptyList())
    val filteredPrestations: StateFlow<List<Prestation>> = _filteredPrestations.asStateFlow()
    
    private val _dateRangeFilter = MutableStateFlow<ClosedRange<LocalDate>?>(null)
    val dateRangeFilter: StateFlow<ClosedRange<LocalDate>?> = _dateRangeFilter.asStateFlow()
    
    private val _statusFilter = MutableStateFlow<PrestationStatus?>(null)
    val statusFilter: StateFlow<PrestationStatus?> = _statusFilter.asStateFlow()
    
    private val _clientFilter = MutableStateFlow<BaseId?>(null)
    val clientFilter: StateFlow<BaseId?> = _clientFilter.asStateFlow()
    
    private val _animalFilter = MutableStateFlow<BaseId?>(null)
    val animalFilter: StateFlow<BaseId?> = _animalFilter.asStateFlow()
    
    init {
        loadPrestations()
    }
    
    override fun createInitialState(): State = State()
    
    override fun handleEvent(event: Event) {
        when (event) {
            is Event.LoadPrestations -> loadPrestations()
            is Event.SelectPrestation -> selectPrestation(event.prestation)
            is Event.DeselectPrestation -> deselectPrestation()
            is Event.SearchPrestations -> searchPrestations(event.query)
            is Event.AddPrestation -> addPrestation(event.prestation)
            is Event.UpdatePrestation -> updatePrestation(event.prestation)
            is Event.DeletePrestation -> deletePrestation(event.prestationId)
            is Event.FilterByDateRange -> filterByDateRange(event.startDate, event.endDate)
            is Event.FilterByStatus -> filterByStatus(event.status)
            is Event.FilterByClient -> filterByClient(event.clientId)
            is Event.FilterByAnimal -> filterByAnimal(event.animalId)
            is Event.ClearDateFilter -> clearDateFilter()
            is Event.ClearStatusFilter -> clearStatusFilter()
            is Event.ClearClientFilter -> clearClientFilter()
            is Event.ClearAnimalFilter -> clearAnimalFilter()
            is Event.ClearAllFilters -> clearAllFilters()
            is Event.ClearSearch -> clearSearch()
            is Event.UpdatePrestationStatus -> updatePrestationStatus(event.prestationId, event.status)
            is Event.CalculateTotalPrice -> calculateTotalPrice(event.prestationId)
        }
    }
    
    private fun loadPrestations() {
        launchWithLoading {
            val prestationList = repository.prestationRepository.getAllPrestations()
            _prestations.value = prestationList
            applyFilters()
        }
    }
    
    private fun selectPrestation(prestation: Prestation) {
        _selectedPrestation.value = prestation
    }
    
    private fun deselectPrestation() {
        _selectedPrestation.value = null
    }
    
    private fun searchPrestations(query: String) {
        _searchQuery.value = query
        applyFilters()
    }
    
    private fun filterByDateRange(startDate: LocalDate, endDate: LocalDate) {
        _dateRangeFilter.value = startDate..endDate
        applyFilters()
    }
    
    private fun filterByStatus(status: PrestationStatus?) {
        _statusFilter.value = status
        applyFilters()
    }
    
    private fun filterByClient(clientId: BaseId?) {
        _clientFilter.value = clientId
        applyFilters()
    }
    
    private fun filterByAnimal(animalId: BaseId?) {
        _animalFilter.value = animalId
        applyFilters()
    }
    
    private fun clearDateFilter() {
        _dateRangeFilter.value = null
        applyFilters()
    }
    
    private fun clearStatusFilter() {
        _statusFilter.value = null
        applyFilters()
    }
    
    private fun clearClientFilter() {
        _clientFilter.value = null
        applyFilters()
    }
    
    private fun clearAnimalFilter() {
        _animalFilter.value = null
        applyFilters()
    }
    
    private fun clearAllFilters() {
        _dateRangeFilter.value = null
        _statusFilter.value = null
        _clientFilter.value = null
        _animalFilter.value = null
        applyFilters()
    }
    
    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val dateRange = _dateRangeFilter.value
        val status = _statusFilter.value
        val clientId = _clientFilter.value
        val animalId = _animalFilter.value
        val allPrestations = _prestations.value
        
        var filtered = allPrestations
        
        // Apply date range filter
        if (dateRange != null) {
            filtered = filtered.filter { prestation ->
                val prestationDate = LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
                prestationDate in dateRange
            }
        }
        
        // Apply status filter
        if (status != null) {
            filtered = filtered.filter { it.status == status }
        }
        
        // Apply client filter
        if (clientId != null) {
            filtered = filtered.filter { it.clientId == clientId }
        }
        
        // Apply animal filter
        if (animalId != null) {
            filtered = filtered.filter { it.animalId == animalId }
        }
        
        // Apply search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { prestation ->
                prestation.notes.lowercase().contains(query) ||
                prestation.status.name.lowercase().contains(query)
            }
        }
        
        _filteredPrestations.value = filtered
    }
    
    private fun addPrestation(prestation: Prestation) {
        // Update state immediately for better UX
        val currentPrestations = _prestations.value.toMutableList()
        currentPrestations.add(prestation)
        _prestations.value = currentPrestations
        applyFilters()
        
        // Launch async operation in background
        launchWithLoading {
            val newPrestationId = repository.prestationRepository.insertPrestation(prestation)
            val updatedPrestation = prestation.copy(id = newPrestationId)
            val updatedPrestations = _prestations.value.toMutableList()
            val index = updatedPrestations.indexOfFirst { it.id == prestation.id }
            if (index != -1) {
                updatedPrestations[index] = updatedPrestation
                _prestations.value = updatedPrestations
                applyFilters()
            }
        }
    }
    
    private fun updatePrestation(prestation: Prestation) {
        // Update state immediately for better UX
        val currentPrestations = _prestations.value.toMutableList()
        val index = currentPrestations.indexOfFirst { it.id == prestation.id }
        if (index != -1) {
            currentPrestations[index] = prestation
            _prestations.value = currentPrestations
            applyFilters()
        }
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.prestationRepository.updatePrestation(prestation)
            if (!success) {
                // Revert changes if update failed
                val revertedPrestations = _prestations.value.toMutableList()
                val originalPrestation = revertedPrestations.find { it.id == prestation.id }
                originalPrestation?.let { original ->
                    val revertIndex = revertedPrestations.indexOfFirst { it.id == prestation.id }
                    if (revertIndex != -1) {
                        revertedPrestations[revertIndex] = original
                        _prestations.value = revertedPrestations
                        applyFilters()
                    }
                }
            }
        }
    }
    
    private fun deletePrestation(prestationId: BaseId) {
        // Update state immediately for better UX
        val currentPrestations = _prestations.value.toMutableList()
        val deletedPrestation = currentPrestations.find { it.id == prestationId }
        currentPrestations.removeAll { it.id == prestationId }
        _prestations.value = currentPrestations
        if (_selectedPrestation.value?.id == prestationId) {
            deselectPrestation()
        }
        applyFilters()
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.prestationRepository.deletePrestation(prestationId)
            if (!success) {
                // Revert changes if delete failed
                deletedPrestation?.let { prestation ->
                    val revertedPrestations = _prestations.value.toMutableList()
                    revertedPrestations.add(prestation)
                    _prestations.value = revertedPrestations
                    applyFilters()
                }
            }
        }
    }
    
    private fun updatePrestationStatus(prestationId: BaseId, status: PrestationStatus) {
        val currentPrestation = _prestations.value.find { it.id == prestationId }
        currentPrestation?.let { prestation ->
            val updatedPrestation = prestation.copy(status = status)
            updatePrestation(updatedPrestation)
        }
    }
    
    private fun calculateTotalPrice(prestationId: BaseId) {
        val prestation = _prestations.value.find { it.id == prestationId }
        prestation?.let { p ->
            launchWithLoading {
                // For now, just use the existing price
                // In a real implementation, this would calculate based on services
                val totalPrice = p.price
                val updatedPrestation = p.copy(price = totalPrice)
                updatePrestation(updatedPrestation)
            }
        }
    }
    
    private fun clearSearch() {
        _searchQuery.value = ""
        applyFilters()
    }
    
    /**
     * Get prestations for a specific date range
     */
    fun getPrestationsForDateRange(startDate: LocalDate, endDate: LocalDate): List<Prestation> {
        return _prestations.value.filter { prestation ->
            val prestationDate = LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
            prestationDate in startDate..endDate
        }
    }
    
    /**
     * Get prestations by status
     */
    fun getPrestationsByStatus(status: PrestationStatus): List<Prestation> {
        return _prestations.value.filter { it.status == status }
    }
    
    /**
     * Get upcoming prestations
     */
    fun getUpcomingPrestations(): List<Prestation> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return _prestations.value.filter { prestation ->
            val prestationDate = LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
            prestationDate >= today
        }.sortedBy { it.startDate }
    }
    
    /**
     * Get total revenue for a date range
     */
    fun getTotalRevenueForDateRange(startDate: LocalDate, endDate: LocalDate): Double {
        return getPrestationsForDateRange(startDate, endDate)
            .filter { it.status == PrestationStatus.COMPLETED }
            .sumOf { it.price }
    }
    
    /**
     * Get statistics for dashboard
     */
    fun getDashboardStats(): DashboardStats {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val allPrestations = _prestations.value
        
        return DashboardStats(
            totalPrestations = allPrestations.size,
            completedPrestations = allPrestations.count { it.status == PrestationStatus.COMPLETED },
            pendingPrestations = allPrestations.count { it.status == PrestationStatus.PLANNED },
            cancelledPrestations = allPrestations.count { it.status == PrestationStatus.CANCELLED },
            todayPrestations = allPrestations.count { prestation ->
                val prestationDate = LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
                prestationDate == today
            },
            totalRevenue = allPrestations.filter { it.status == PrestationStatus.COMPLETED }.sumOf { it.price }
        )
    }
    
    /**
     * UI State for PrestationViewModel
     */
    data class State(
        val prestations: List<Prestation> = emptyList(),
        val selectedPrestation: Prestation? = null,
        val searchQuery: String = "",
        val filteredPrestations: List<Prestation> = emptyList(),
        val dateRangeFilter: ClosedRange<LocalDate>? = null,
        val statusFilter: PrestationStatus? = null,
        val clientFilter: BaseId? = null,
        val animalFilter: BaseId? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    /**
     * Dashboard statistics
     */
    data class DashboardStats(
        val totalPrestations: Int,
        val completedPrestations: Int,
        val pendingPrestations: Int,
        val cancelledPrestations: Int,
        val todayPrestations: Int,
        val totalRevenue: Double
    )
    
    /**
     * UI Events for PrestationViewModel
     */
    sealed class Event {
        object LoadPrestations : Event()
        data class SelectPrestation(val prestation: Prestation) : Event()
        object DeselectPrestation : Event()
        data class SearchPrestations(val query: String) : Event()
        data class AddPrestation(val prestation: Prestation) : Event()
        data class UpdatePrestation(val prestation: Prestation) : Event()
        data class DeletePrestation(val prestationId: BaseId) : Event()
        data class FilterByDateRange(val startDate: LocalDate, val endDate: LocalDate) : Event()
        data class FilterByStatus(val status: PrestationStatus?) : Event()
        data class FilterByClient(val clientId: BaseId?) : Event()
        data class FilterByAnimal(val animalId: BaseId?) : Event()
        object ClearDateFilter : Event()
        object ClearStatusFilter : Event()
        object ClearClientFilter : Event()
        object ClearAnimalFilter : Event()
        object ClearAllFilters : Event()
        object ClearSearch : Event()
        data class UpdatePrestationStatus(val prestationId: BaseId, val status: PrestationStatus) : Event()
        data class CalculateTotalPrice(val prestationId: BaseId) : Event()
    }
} 