package com.gtreb.cleopattesmanager.ui.viewmodel

import com.gtreb.cleopattesmanager.data.repository.AppRepository
import com.gtreb.cleopattesmanager.data.model.Client
import com.gtreb.cleopattesmanager.data.model.BaseId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for client management
 */
class ClientViewModel(
    private val repository: AppRepository
) : BaseViewModel<ClientViewModel.State, ClientViewModel.Event>() {
    
    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()
    
    private val _selectedClient = MutableStateFlow<Client?>(null)
    val selectedClient: StateFlow<Client?> = _selectedClient.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredClients = MutableStateFlow<List<Client>>(emptyList())
    val filteredClients: StateFlow<List<Client>> = _filteredClients.asStateFlow()
    
    init {
        loadClients()
    }
    
    override fun createInitialState(): State = State()
    
    override fun handleEvent(event: Event) {
        when (event) {
            is Event.LoadClients -> loadClients()
            is Event.SelectClient -> selectClient(event.client)
            is Event.DeselectClient -> deselectClient()
            is Event.SearchClients -> searchClients(event.query)
            is Event.AddClient -> addClient(event.client)
            is Event.UpdateClient -> updateClient(event.client)
            is Event.DeleteClient -> deleteClient(event.clientId)
            is Event.ClearSearch -> clearSearch()
        }
    }
    
    private fun loadClients() {
        launchWithLoading {
            val clientList = repository.clientRepository.getAllClients()
            _clients.value = clientList
            applySearchFilter()
        }
    }
    
    private fun selectClient(client: Client) {
        _selectedClient.value = client
    }
    
    private fun deselectClient() {
        _selectedClient.value = null
    }
    
    private fun searchClients(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }
    
    private fun applySearchFilter() {
        val query = _searchQuery.value.lowercase()
        val allClients = _clients.value
        
        val filtered = if (query.isBlank()) {
            allClients
        } else {
            allClients.filter { client ->
                client.firstName.lowercase().contains(query) ||
                client.lastName.lowercase().contains(query) ||
                client.email.lowercase().contains(query) ||
                client.phone.lowercase().contains(query)
            }
        }
        
        _filteredClients.value = filtered
    }
    
    private fun addClient(client: Client) {
        // Update state immediately for better UX
        val currentClients = _clients.value.toMutableList()
        currentClients.add(client)
        _clients.value = currentClients
        applySearchFilter()
        
        // Launch async operation in background
        launchWithLoading {
            val newClientId = repository.clientRepository.insertClient(client)
            val updatedClient = client.copy(id = newClientId)
            val updatedClients = _clients.value.toMutableList()
            val index = updatedClients.indexOfFirst { it.id == client.id }
            if (index != -1) {
                updatedClients[index] = updatedClient
                _clients.value = updatedClients
                applySearchFilter()
            }
        }
    }
    
    private fun updateClient(client: Client) {
        // Update state immediately for better UX
        val currentClients = _clients.value.toMutableList()
        val index = currentClients.indexOfFirst { it.id == client.id }
        if (index != -1) {
            currentClients[index] = client
            _clients.value = currentClients
            applySearchFilter()
        }
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.clientRepository.updateClient(client)
            if (!success) {
                // Revert changes if update failed
                val revertedClients = _clients.value.toMutableList()
                val originalClient = revertedClients.find { it.id == client.id }
                originalClient?.let { original ->
                    val revertIndex = revertedClients.indexOfFirst { it.id == client.id }
                    if (revertIndex != -1) {
                        revertedClients[revertIndex] = original
                        _clients.value = revertedClients
                        applySearchFilter()
                    }
                }
            }
        }
    }
    
    private fun deleteClient(clientId: BaseId) {
        // Update state immediately for better UX
        val currentClients = _clients.value.toMutableList()
        val deletedClient = currentClients.find { it.id == clientId }
        currentClients.removeAll { it.id == clientId }
        _clients.value = currentClients
        if (_selectedClient.value?.id == clientId) {
            deselectClient()
        }
        applySearchFilter()
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.clientRepository.deleteClient(clientId)
            if (!success) {
                // Revert changes if delete failed
                deletedClient?.let { client ->
                    val revertedClients = _clients.value.toMutableList()
                    revertedClients.add(client)
                    _clients.value = revertedClients
                    applySearchFilter()
                }
            }
        }
    }
    
    private fun clearSearch() {
        _searchQuery.value = ""
        applySearchFilter()
    }
    
    /**
     * UI State for ClientViewModel
     */
    data class State(
        val clients: List<Client> = emptyList(),
        val selectedClient: Client? = null,
        val searchQuery: String = "",
        val filteredClients: List<Client> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    /**
     * UI Events for ClientViewModel
     */
    sealed class Event {
        object LoadClients : Event()
        data class SelectClient(val client: Client) : Event()
        object DeselectClient : Event()
        data class SearchClients(val query: String) : Event()
        data class AddClient(val client: Client) : Event()
        data class UpdateClient(val client: Client) : Event()
        data class DeleteClient(val clientId: BaseId) : Event()
        object ClearSearch : Event()
    }
} 