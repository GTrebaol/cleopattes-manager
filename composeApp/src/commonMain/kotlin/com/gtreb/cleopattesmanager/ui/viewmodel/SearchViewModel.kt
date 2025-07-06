package com.gtreb.cleopattesmanager.ui.viewmodel

import com.gtreb.cleopattesmanager.data.repository.AppRepository
import com.gtreb.cleopattesmanager.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

/**
 * ViewModel for global search functionality
 */
class SearchViewModel(
    private val repository: AppRepository
) : BaseViewModel<SearchViewModel.State, SearchViewModel.Event>() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow(SearchResults())
    val searchResults: StateFlow<SearchResults> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _selectedFilters = MutableStateFlow(SearchFilters())
    val selectedFilters: StateFlow<SearchFilters> = _selectedFilters.asStateFlow()
    
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()
    
    init {
        setupSearchFlow()
    }
    
    override fun createInitialState(): State = State()
    
    override fun handleEvent(event: Event) {
        when (event) {
            is Event.Search -> performSearch(event.query)
            is Event.ClearSearch -> clearSearch()
            is Event.SetFilters -> setFilters(event.filters)
            is Event.ClearFilters -> clearFilters()
            is Event.SelectResult -> selectResult(event.result)
            is Event.AddToRecentSearches -> addToRecentSearches(event.query)
            is Event.ClearRecentSearches -> clearRecentSearches()
            is Event.LoadSuggestions -> loadSuggestions(event.query)
        }
    }
    
    private fun setupSearchFlow() {
        // Combine search query and filters with debounce
        combine(
            _searchQuery,
            _selectedFilters
        ) { query, filters ->
            if (query.isNotBlank()) {
                performSearchInternal(query, filters)
            } else {
                _searchResults.value = SearchResults()
            }
        }
    }
    
    private fun performSearch(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            addToRecentSearches(query)
        }
    }
    
    private fun performSearchInternal(query: String, filters: SearchFilters) {
        launchWithLoading {
            _isSearching.value = true
            
            val searchTerm = query.lowercase()
            val results = mutableListOf<SearchResult>()
            
            // Search in clients if enabled
            if (filters.includeClients) {
                repository.clientRepository.getAllClientsFlow().collect { clients ->
                    val clientResults = clients.filter { client ->
                        client.firstName.lowercase().contains(searchTerm) ||
                        client.lastName.lowercase().contains(searchTerm) ||
                        client.email.lowercase().contains(searchTerm) ||
                        client.phone.lowercase().contains(searchTerm) ||
                        client.address.lowercase().contains(searchTerm)
                    }.map { client ->
                        SearchResult.ClientResult(client)
                    }
                    results.addAll(clientResults)
                }
            }
            
            // Search in animals if enabled
            if (filters.includeAnimals) {
                repository.animalRepository.getAllAnimalsFlow().collect { animals ->
                    val animalResults = animals.filter { animal ->
                        animal.name.lowercase().contains(searchTerm) ||
                        animal.breed.lowercase().contains(searchTerm) ||
                        getLocalizedSpeciesName(animal.speciesId, "fr").lowercase().contains(searchTerm)
                    }.map { animal ->
                        SearchResult.AnimalResult(animal)
                    }
                    results.addAll(animalResults)
                }
            }
            
            // Search in services if enabled
            if (filters.includeServices) {
                repository.serviceRepository.getAllServicesFlow().collect { services ->
                    val serviceResults = services.filter { service ->
                        service.name.lowercase().contains(searchTerm) ||
                        service.description.lowercase().contains(searchTerm)
                    }.map { service ->
                        SearchResult.ServiceResult(service)
                    }
                    results.addAll(serviceResults)
                }
            }
            
            // Search in prestations if enabled
            if (filters.includePrestations) {
                repository.prestationRepository.getAllPrestationsFlow().collect { prestations ->
                    val prestationResults = prestations.filter { prestation ->
                        prestation.notes.lowercase().contains(searchTerm) ||
                        prestation.status.name.lowercase().contains(searchTerm)
                    }.map { prestation ->
                        SearchResult.PrestationResult(prestation)
                    }
                    results.addAll(prestationResults)
                }
            }
            
            // Sort results by relevance and type
            val sortedResults = results.sortedWith(
                compareBy<SearchResult> { it.getRelevanceScore(searchTerm) }.reversed()
                    .thenBy { it.getTypeOrder() }
            )
            
            _searchResults.value = SearchResults(
                query = query,
                results = sortedResults,
                totalCount = sortedResults.size,
                clientCount = sortedResults.count { it is SearchResult.ClientResult },
                animalCount = sortedResults.count { it is SearchResult.AnimalResult },
                serviceCount = sortedResults.count { it is SearchResult.ServiceResult },
                prestationCount = sortedResults.count { it is SearchResult.PrestationResult }
            )
            
            _isSearching.value = false
        }
    }
    
    private fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = SearchResults()
    }
    
    private fun setFilters(filters: SearchFilters) {
        _selectedFilters.value = filters
    }
    
    private fun clearFilters() {
        _selectedFilters.value = SearchFilters()
    }
    
    private fun selectResult(result: SearchResult) {
        // This would typically navigate to the appropriate detail screen
        // For now, we just update the state
        updateState { it.copy(selectedResult = result) }
    }
    
    private fun addToRecentSearches(query: String) {
        val currentSearches = _recentSearches.value.toMutableList()
        if (!currentSearches.contains(query)) {
            currentSearches.add(0, query)
            if (currentSearches.size > 10) {
                currentSearches.removeAt(currentSearches.size - 1)
            }
            _recentSearches.value = currentSearches
        }
    }
    
    private fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }
    
    private fun loadSuggestions(query: String) {
        if (query.length < 2) return
        
        launchWithLoading {
            val suggestions = mutableListOf<String>()
            
            // Get suggestions from recent searches
            suggestions.addAll(_recentSearches.value.filter { it.lowercase().contains(query.lowercase()) })
            
            // Get suggestions from client names
            repository.clientRepository.getAllClientsFlow().collect { clients ->
                val clientSuggestions = clients.map { "${it.firstName} ${it.lastName}" }
                    .filter { it.lowercase().contains(query.lowercase()) }
                suggestions.addAll(clientSuggestions)
            }
            
            // Get suggestions from animal names
            repository.animalRepository.getAllAnimalsFlow().collect { animals ->
                val animalSuggestions = animals.map { it.name }
                    .filter { it.lowercase().contains(query.lowercase()) }
                suggestions.addAll(animalSuggestions)
            }
            
            // Get suggestions from service names
            repository.serviceRepository.getAllServicesFlow().collect { services ->
                val serviceSuggestions = services.map { it.name }
                    .filter { it.lowercase().contains(query.lowercase()) }
                suggestions.addAll(serviceSuggestions)
            }
            
            updateState { it.copy(suggestions = suggestions.distinct().take(5)) }
        }
    }
    
    /**
     * UI State for SearchViewModel
     */
    data class State(
        val searchQuery: String = "",
        val searchResults: SearchResults = SearchResults(),
        val isSearching: Boolean = false,
        val selectedFilters: SearchFilters = SearchFilters(),
        val recentSearches: List<String> = emptyList(),
        val suggestions: List<String> = emptyList(),
        val selectedResult: SearchResult? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    /**
     * Search results container
     */
    data class SearchResults(
        val query: String = "",
        val results: List<SearchResult> = emptyList(),
        val totalCount: Int = 0,
        val clientCount: Int = 0,
        val animalCount: Int = 0,
        val serviceCount: Int = 0,
        val prestationCount: Int = 0
    )
    
    /**
     * Search filters
     */
    data class SearchFilters(
        val includeClients: Boolean = true,
        val includeAnimals: Boolean = true,
        val includeServices: Boolean = true,
        val includePrestations: Boolean = true
    )
    
    /**
     * Search result sealed class
     */
    sealed class SearchResult {
        data class ClientResult(val client: Client) : SearchResult()
        data class AnimalResult(val animal: Animal) : SearchResult()
        data class ServiceResult(val service: Service) : SearchResult()
        data class PrestationResult(val prestation: Prestation) : SearchResult()
        
        fun getRelevanceScore(searchTerm: String): Int {
            return when (this) {
                is ClientResult -> {
                    val client = client
                    var score = 0
                    if (client.firstName.lowercase().contains(searchTerm)) score += 10
                    if (client.lastName.lowercase().contains(searchTerm)) score += 10
                    if (client.email.lowercase().contains(searchTerm)) score += 5
                    if (client.phone.lowercase().contains(searchTerm)) score += 3
                    score
                }
                is AnimalResult -> {
                    val animal = animal
                    var score = 0
                    if (animal.name.lowercase().contains(searchTerm)) score += 10
                    if (animal.breed.lowercase().contains(searchTerm)) score += 5
                    score
                }
                is ServiceResult -> {
                    val service = service
                    var score = 0
                    if (service.name.lowercase().contains(searchTerm)) score += 10
                    if (service.description.lowercase().contains(searchTerm)) score += 5
                    score
                }
                is PrestationResult -> {
                    val prestation = prestation
                    var score = 0
                    if (prestation.notes.lowercase().contains(searchTerm)) score += 5
                    if (prestation.status.name.lowercase().contains(searchTerm)) score += 3
                    score
                }
            }
        }
        
        fun getTypeOrder(): Int {
            return when (this) {
                is ClientResult -> 1
                is AnimalResult -> 2
                is ServiceResult -> 3
                is PrestationResult -> 4
            }
        }
    }
    
    /**
     * UI Events for SearchViewModel
     */
    sealed class Event {
        data class Search(val query: String) : Event()
        object ClearSearch : Event()
        data class SetFilters(val filters: SearchFilters) : Event()
        object ClearFilters : Event()
        data class SelectResult(val result: SearchResult) : Event()
        data class AddToRecentSearches(val query: String) : Event()
        object ClearRecentSearches : Event()
        data class LoadSuggestions(val query: String) : Event()
    }
} 