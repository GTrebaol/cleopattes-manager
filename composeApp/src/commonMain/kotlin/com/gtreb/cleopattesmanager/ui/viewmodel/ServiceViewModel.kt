package com.gtreb.cleopattesmanager.ui.viewmodel

import com.gtreb.cleopattesmanager.data.repository.AppRepository
import com.gtreb.cleopattesmanager.data.model.Service
import com.gtreb.cleopattesmanager.data.model.BaseId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for service management
 */
class ServiceViewModel(
    private val repository: AppRepository
) : BaseViewModel<ServiceViewModel.State, ServiceViewModel.Event>() {
    
    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services.asStateFlow()
    
    private val _selectedService = MutableStateFlow<Service?>(null)
    val selectedService: StateFlow<Service?> = _selectedService.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredServices = MutableStateFlow<List<Service>>(emptyList())
    val filteredServices: StateFlow<List<Service>> = _filteredServices.asStateFlow()
    
    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()
    
    private val _priceRangeFilter = MutableStateFlow<ClosedFloatingPointRange<Double>?>(null)
    val priceRangeFilter: StateFlow<ClosedFloatingPointRange<Double>?> = _priceRangeFilter.asStateFlow()
    
    init {
        loadServices()
    }
    
    override fun createInitialState(): State = State()
    
    override fun handleEvent(event: Event) {
        when (event) {
            is Event.LoadServices -> loadServices()
            is Event.SelectService -> selectService(event.service)
            is Event.DeselectService -> deselectService()
            is Event.SearchServices -> searchServices(event.query)
            is Event.AddService -> addService(event.service)
            is Event.UpdateService -> updateService(event.service)
            is Event.DeleteService -> deleteService(event.serviceId)
            is Event.FilterByCategory -> filterByCategory(event.category)
            is Event.FilterByPriceRange -> filterByPriceRange(event.minPrice, event.maxPrice)
            is Event.ClearCategoryFilter -> clearCategoryFilter()
            is Event.ClearPriceFilter -> clearPriceFilter()
            is Event.ClearAllFilters -> clearAllFilters()
            is Event.ClearSearch -> clearSearch()
            is Event.ToggleServiceActive -> toggleServiceActive(event.serviceId)
        }
    }
    
    private fun loadServices() {
        launchWithLoading {
            val serviceList = repository.serviceRepository.getAllServices()
            _services.value = serviceList
            applyFilters()
        }
    }
    
    private fun selectService(service: Service) {
        _selectedService.value = service
    }
    
    private fun deselectService() {
        _selectedService.value = null
    }
    
    private fun searchServices(query: String) {
        _searchQuery.value = query
        applyFilters()
    }
    
    private fun filterByCategory(category: String?) {
        _categoryFilter.value = category
        applyFilters()
    }
    
    private fun filterByPriceRange(minPrice: Double, maxPrice: Double) {
        _priceRangeFilter.value = minPrice..maxPrice
        applyFilters()
    }
    
    private fun clearCategoryFilter() {
        _categoryFilter.value = null
        applyFilters()
    }
    
    private fun clearPriceFilter() {
        _priceRangeFilter.value = null
        applyFilters()
    }
    
    private fun clearAllFilters() {
        _categoryFilter.value = null
        _priceRangeFilter.value = null
        applyFilters()
    }
    
    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val category = _categoryFilter.value
        val priceRange = _priceRangeFilter.value
        val allServices = _services.value
        
        var filtered = allServices
        
        // Apply category filter
        if (category != null) {
            filtered = filtered.filter { it.name.lowercase() == category.lowercase() }
        }
        
        // Apply price range filter
        if (priceRange != null) {
            filtered = filtered.filter { service ->
                service.price in priceRange
            }
        }
        
        // Apply search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { service ->
                service.name.lowercase().contains(query) ||
                service.description.lowercase().contains(query)
            }
        }
        
        _filteredServices.value = filtered
    }
    
    private fun addService(service: Service) {
        // Update state immediately for better UX
        val currentServices = _services.value.toMutableList()
        currentServices.add(service)
        _services.value = currentServices
        applyFilters()
        
        // Launch async operation in background
        launchWithLoading {
            val newServiceId = repository.serviceRepository.insertService(service)
            val updatedService = service.copy(id = newServiceId)
            val updatedServices = _services.value.toMutableList()
            val index = updatedServices.indexOfFirst { it.id == service.id }
            if (index != -1) {
                updatedServices[index] = updatedService
                _services.value = updatedServices
                applyFilters()
            }
        }
    }
    
    private fun updateService(service: Service) {
        // Update state immediately for better UX
        val currentServices = _services.value.toMutableList()
        val index = currentServices.indexOfFirst { it.id == service.id }
        if (index != -1) {
            currentServices[index] = service
            _services.value = currentServices
            applyFilters()
        }
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.serviceRepository.updateService(service)
            if (!success) {
                // Revert changes if update failed
                val revertedServices = _services.value.toMutableList()
                val originalService = revertedServices.find { it.id == service.id }
                originalService?.let { original ->
                    val revertIndex = revertedServices.indexOfFirst { it.id == service.id }
                    if (revertIndex != -1) {
                        revertedServices[revertIndex] = original
                        _services.value = revertedServices
                        applyFilters()
                    }
                }
            }
        }
    }
    
    private fun deleteService(serviceId: BaseId) {
        // Update state immediately for better UX
        val currentServices = _services.value.toMutableList()
        val deletedService = currentServices.find { it.id == serviceId }
        currentServices.removeAll { it.id == serviceId }
        _services.value = currentServices
        if (_selectedService.value?.id == serviceId) {
            deselectService()
        }
        applyFilters()
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.serviceRepository.deleteService(serviceId)
            if (!success) {
                // Revert changes if delete failed
                deletedService?.let { service ->
                    val revertedServices = _services.value.toMutableList()
                    revertedServices.add(service)
                    _services.value = revertedServices
                    applyFilters()
                }
            }
        }
    }
    
    private fun toggleServiceActive(serviceId: BaseId) {
        val currentService = _services.value.find { it.id == serviceId }
        currentService?.let { service ->
            val updatedService = service.copy(isActive = !service.isActive)
            updateService(updatedService)
        }
    }
    
    private fun clearSearch() {
        _searchQuery.value = ""
        applyFilters()
    }
    
    /**
     * Get all available categories
     */
    fun getAvailableCategories(): List<String> {
        return _services.value.map { it.name }.distinct().sorted()
    }
    
    /**
     * Get price range for all services
     */
    fun getPriceRange(): ClosedFloatingPointRange<Double>? {
        val services = _services.value
        if (services.isEmpty()) return null
        
        val minPrice = services.minOf { it.price }
        val maxPrice = services.maxOf { it.price }
        return minPrice..maxPrice
    }
    
    /**
     * Get active services only
     */
    fun getActiveServices(): List<Service> {
        return _services.value.filter { it.isActive }
    }
    
    /**
     * UI State for ServiceViewModel
     */
    data class State(
        val services: List<Service> = emptyList(),
        val selectedService: Service? = null,
        val searchQuery: String = "",
        val filteredServices: List<Service> = emptyList(),
        val categoryFilter: String? = null,
        val priceRangeFilter: ClosedFloatingPointRange<Double>? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    /**
     * UI Events for ServiceViewModel
     */
    sealed class Event {
        object LoadServices : Event()
        data class SelectService(val service: Service) : Event()
        object DeselectService : Event()
        data class SearchServices(val query: String) : Event()
        data class AddService(val service: Service) : Event()
        data class UpdateService(val service: Service) : Event()
        data class DeleteService(val serviceId: BaseId) : Event()
        data class FilterByCategory(val category: String?) : Event()
        data class FilterByPriceRange(val minPrice: Double, val maxPrice: Double) : Event()
        object ClearCategoryFilter : Event()
        object ClearPriceFilter : Event()
        object ClearAllFilters : Event()
        object ClearSearch : Event()
        data class ToggleServiceActive(val serviceId: BaseId) : Event()
    }
} 