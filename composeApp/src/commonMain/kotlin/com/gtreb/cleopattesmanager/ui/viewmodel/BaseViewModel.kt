package com.gtreb.cleopattesmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtreb.cleopattesmanager.data.model.Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class with common state management
 */
abstract class BaseViewModel<State, Event> : ViewModel() {
    
    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<State> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * Create initial state for the ViewModel
     */
    abstract fun createInitialState(): State
    
    /**
     * Update the UI state
     */
    protected fun updateState(update: (State) -> State) {
        _uiState.value = update(_uiState.value)
    }
    
    /**
     * Set loading state
     */
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    /**
     * Set error message
     */
    protected fun setError(message: String?) {
        _errorMessage.value = message
    }
    
    /**
     * Clear error message
     */
    protected fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Execute a coroutine with loading and error handling
     */
    protected fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                setLoading(true)
                clearError()
                block()
            } catch (e: Exception) {
                setError(e.message ?: "Une erreur est survenue")
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Handle UI events
     */
    abstract fun handleEvent(event: Event)
} 