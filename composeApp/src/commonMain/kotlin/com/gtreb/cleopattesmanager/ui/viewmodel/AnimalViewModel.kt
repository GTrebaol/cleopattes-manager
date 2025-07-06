package com.gtreb.cleopattesmanager.ui.viewmodel

import com.gtreb.cleopattesmanager.data.model.Animal
import com.gtreb.cleopattesmanager.data.model.BaseId
import com.gtreb.cleopattesmanager.data.repository.AppRepository
import com.gtreb.cleopattesmanager.data.model.getLocalizedSpeciesName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for animal management
 */
class AnimalViewModel(
    private val repository: AppRepository
) : BaseViewModel<AnimalViewModel.State, AnimalViewModel.Event>() {
    
    private val _animals = MutableStateFlow<List<Animal>>(emptyList())
    val animals: StateFlow<List<Animal>> = _animals.asStateFlow()
    
    private val _selectedAnimal = MutableStateFlow<Animal?>(null)
    val selectedAnimal: StateFlow<Animal?> = _selectedAnimal.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredAnimals = MutableStateFlow<List<Animal>>(emptyList())
    val filteredAnimals: StateFlow<List<Animal>> = _filteredAnimals.asStateFlow()
    
    private val _clientFilter = MutableStateFlow<BaseId?>(null)
    val clientFilter: StateFlow<BaseId?> = _clientFilter.asStateFlow()
    
    private val _currentLanguage = MutableStateFlow("fr")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
    
    init {
        loadAnimals()
    }
    
    override fun createInitialState(): State = State()
    
    override fun handleEvent(event: Event) {
        when (event) {
            is Event.LoadAnimals -> loadAnimals()
            is Event.SelectAnimal -> selectAnimal(event.animal)
            is Event.DeselectAnimal -> deselectAnimal()
            is Event.SearchAnimals -> searchAnimals(event.query)
            is Event.AddAnimal -> addAnimal(event.animal)
            is Event.UpdateAnimal -> updateAnimal(event.animal)
            is Event.DeleteAnimal -> deleteAnimal(event.animalId)
            is Event.FilterByClient -> filterByClient(event.clientId)
            is Event.ClearClientFilter -> clearClientFilter()
            is Event.ClearSearch -> clearSearch()
            is Event.SetLanguage -> setLanguage(event.languageCode)
        }
    }
    
    private fun loadAnimals() {
        launchWithLoading {
            val animalList = repository.animalRepository.getAllAnimals()
            _animals.value = animalList
            applyFilters()
        }
    }
    
    private fun selectAnimal(animal: Animal) {
        _selectedAnimal.value = animal
    }
    
    private fun deselectAnimal() {
        _selectedAnimal.value = null
    }
    
    private fun searchAnimals(query: String) {
        _searchQuery.value = query
        applyFilters()
    }
    
    private fun filterByClient(clientId: BaseId?) {
        _clientFilter.value = clientId
        applyFilters()
    }
    
    private fun clearClientFilter() {
        _clientFilter.value = null
        applyFilters()
    }
    
    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val clientId = _clientFilter.value
        val allAnimals = _animals.value
        
        var filtered = allAnimals
        
        // Apply client filter
        if (clientId != null) {
            filtered = filtered.filter { it.clientId == clientId }
        }
        
        // Apply search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { animal ->
                animal.name.lowercase().contains(query) ||
                animal.breed.lowercase().contains(query) ||
                getLocalizedSpeciesName(animal.speciesId, _currentLanguage.value)
                    .lowercase().contains(query)
            }
        }
        
        _filteredAnimals.value = filtered
    }
    
    private fun addAnimal(animal: Animal) {
        // Update state immediately for better UX
        val currentAnimals = _animals.value.toMutableList()
        currentAnimals.add(animal)
        _animals.value = currentAnimals
        applyFilters()
        
        // Launch async operation in background
        launchWithLoading {
            val newAnimalId = repository.animalRepository.insertAnimal(animal)
            val updatedAnimal = animal.copy(id = newAnimalId)
            val updatedAnimals = _animals.value.toMutableList()
            val index = updatedAnimals.indexOfFirst { it.id == animal.id }
            if (index != -1) {
                updatedAnimals[index] = updatedAnimal
                _animals.value = updatedAnimals
                applyFilters()
            }
        }
    }
    
    private fun updateAnimal(animal: Animal) {
        // Update state immediately for better UX
        val currentAnimals = _animals.value.toMutableList()
        val index = currentAnimals.indexOfFirst { it.id == animal.id }
        if (index != -1) {
            currentAnimals[index] = animal
            _animals.value = currentAnimals
            applyFilters()
        }
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.animalRepository.updateAnimal(animal)
            if (!success) {
                // Revert changes if update failed
                val revertedAnimals = _animals.value.toMutableList()
                val originalAnimal = revertedAnimals.find { it.id == animal.id }
                originalAnimal?.let { original ->
                    val revertIndex = revertedAnimals.indexOfFirst { it.id == animal.id }
                    if (revertIndex != -1) {
                        revertedAnimals[revertIndex] = original
                        _animals.value = revertedAnimals
                        applyFilters()
                    }
                }
            }
        }
    }
    
    private fun deleteAnimal(animalId: BaseId) {
        // Update state immediately for better UX
        val currentAnimals = _animals.value.toMutableList()
        val deletedAnimal = currentAnimals.find { it.id == animalId }
        currentAnimals.removeAll { it.id == animalId }
        _animals.value = currentAnimals
        if (_selectedAnimal.value?.id == animalId) {
            deselectAnimal()
        }
        applyFilters()
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.animalRepository.deleteAnimal(animalId)
            if (!success) {
                // Revert changes if delete failed
                deletedAnimal?.let { animal ->
                    val revertedAnimals = _animals.value.toMutableList()
                    revertedAnimals.add(animal)
                    _animals.value = revertedAnimals
                    applyFilters()
                }
            }
        }
    }
    
    private fun clearSearch() {
        _searchQuery.value = ""
        applyFilters()
    }
    
    private fun setLanguage(languageCode: String) {
        _currentLanguage.value = languageCode
        // Refresh filtered animals to update localized species names
        applyFilters()
    }
    
    /**
     * Get animals for a specific client
     */
    fun getAnimalsForClient(clientId: BaseId): List<Animal> {
        return _animals.value.filter { it.clientId == clientId }
    }
    
    /**
     * UI State for AnimalViewModel
     */
    data class State(
        val animals: List<Animal> = emptyList(),
        val selectedAnimal: Animal? = null,
        val searchQuery: String = "",
        val filteredAnimals: List<Animal> = emptyList(),
        val clientFilter: BaseId? = null,
        val currentLanguage: String = "fr",
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    /**
     * UI Events for AnimalViewModel
     */
    sealed class Event {
        object LoadAnimals : Event()
        data class SelectAnimal(val animal: Animal) : Event()
        object DeselectAnimal : Event()
        data class SearchAnimals(val query: String) : Event()
        data class AddAnimal(val animal: Animal) : Event()
        data class UpdateAnimal(val animal: Animal) : Event()
        data class DeleteAnimal(val animalId: BaseId) : Event()
        data class FilterByClient(val clientId: BaseId?) : Event()
        object ClearClientFilter : Event()
        object ClearSearch : Event()
        data class SetLanguage(val languageCode: String) : Event()
    }
} 