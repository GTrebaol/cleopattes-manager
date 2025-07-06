package com.gtreb.cleopattesmanager.ui.viewmodel

import com.gtreb.cleopattesmanager.data.repository.AppRepository
import com.gtreb.cleopattesmanager.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ViewModel for planning and calendar management
 */
@OptIn(ExperimentalTime::class)
class PlanningViewModel(
    private val repository: AppRepository
) : BaseViewModel<PlanningViewModel.State, PlanningViewModel.Event>() {
    
    private val _planningUiState = MutableStateFlow(PlanningUiState())
    val planningUiState: StateFlow<PlanningUiState> = _planningUiState.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    override fun createInitialState(): State = State()
    
    override fun handleEvent(event: Event) {
        when (event) {
            is Event.LoadTimeSlots -> loadTimeSlots()
            is Event.SelectTimeSlot -> selectTimeSlot(event.timeSlot)
            is Event.ClearSelectedTimeSlot -> clearSelectedTimeSlot()
            is Event.AddTimeSlot -> addTimeSlot(event.timeSlot)
            is Event.UpdateTimeSlot -> updateTimeSlot(event.timeSlot)
            is Event.DeleteTimeSlot -> deleteTimeSlot(event.BaseId)
            is Event.SetCurrentDate -> setCurrentDate(event.date)
            is Event.SetCalendarViewType -> setCalendarViewType(event.viewType)
            is Event.NavigateToPreviousPeriod -> navigateToPreviousPeriod()
            is Event.NavigateToNextPeriod -> navigateToNextPeriod()
            is Event.NavigateToToday -> navigateToToday()
            is Event.UpdateTimeSlotStatus -> updateTimeSlotStatus(event.BaseId, event.status)
            is Event.LoadClients -> loadClients()
            is Event.LoadAnimals -> loadAnimals()
            is Event.LoadServices -> loadServices()
            is Event.FilterByClient -> filterByClient(event.clientId)
            is Event.FilterByAnimal -> filterByAnimal(event.animalId)
            is Event.FilterByService -> filterByService(event.serviceId)
            is Event.ClearFilters -> clearFilters()
        }
    }
    
    // UI Actions
    fun showAddTimeSlotDialog() {
        _planningUiState.value = _planningUiState.value.copy(showAddTimeSlotDialog = true)
    }
    
    fun hideAddTimeSlotDialog() {
        _planningUiState.value = _planningUiState.value.copy(showAddTimeSlotDialog = false)
    }
    
    fun showEditTimeSlotDialog() {
        _planningUiState.value = _planningUiState.value.copy(showEditTimeSlotDialog = true)
    }
    
    fun hideEditTimeSlotDialog() {
        _planningUiState.value = _planningUiState.value.copy(showEditTimeSlotDialog = false)
    }
    
    fun selectTimeSlot(timeSlot: TimeSlotWithDetails) {
        _planningUiState.value = _planningUiState.value.copy(selectedTimeSlot = timeSlot)
    }
    
    fun clearSelectedTimeSlot() {
        _planningUiState.value = _planningUiState.value.copy(selectedTimeSlot = null)
    }
    
    fun goToToday() {
        setCurrentDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }
    
    fun deleteTimeSlot() {
        val selectedTimeSlot = _planningUiState.value.selectedTimeSlot
        if (selectedTimeSlot != null) {
            deleteTimeSlot(selectedTimeSlot.timeSlot.id)
            clearSelectedTimeSlot()
        }
    }
    
    private fun loadInitialData() {
        loadTimeSlots()
        loadClients()
        loadAnimals()
        loadServices()
    }
    
    private fun loadTimeSlots() {
        launchWithLoading {
            val currentDate = _planningUiState.value.currentDate
            val calendarView = _planningUiState.value.calendarView
            
            val timeSlots = when (calendarView) {
                CalendarViewType.DAY -> {
                    val startOfDay = currentDate.toEpochDays() * 24 * 60 * 60 * 1000
                    val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1
                    repository.timeSlotRepository.getTimeSlotsWithDetailsForDateRange(startOfDay, endOfDay)
                }
                CalendarViewType.WEEK -> {
                    val startOfWeek = currentDate.toEpochDays() * 24 * 60 * 60 * 1000
                    val endOfWeek = startOfWeek + (7 * 24 * 60 * 60 * 1000) - 1
                    repository.timeSlotRepository.getTimeSlotsWithDetailsForDateRange(startOfWeek, endOfWeek)
                }
                CalendarViewType.MONTH -> {
                    val startOfMonth = currentDate.toEpochDays() * 24 * 60 * 60 * 1000
                    val endOfMonth = startOfMonth + (30 * 24 * 60 * 60 * 1000) - 1
                    repository.timeSlotRepository.getTimeSlotsWithDetailsForDateRange(startOfMonth, endOfMonth)
                }
            }
            
            _planningUiState.value = _planningUiState.value.copy(
                timeSlotsForSelectedDate = timeSlots,
                timeSlotsForSelectedWeek = timeSlots,
                timeSlotsForSelectedMonth = timeSlots
            )
        }
    }
    
    private fun loadClients() {
        launchWithLoading {
            val clientList = repository.clientRepository.getAllClients()
            _planningUiState.value = _planningUiState.value.copy(clients = clientList)
        }
    }
    
    private fun loadAnimals() {
        launchWithLoading {
            val animalList = repository.animalRepository.getAllAnimals()
            _planningUiState.value = _planningUiState.value.copy(animals = animalList)
        }
    }
    
    private fun loadServices() {
        launchWithLoading {
            val serviceList = repository.serviceRepository.getAllServices()
            _planningUiState.value = _planningUiState.value.copy(services = serviceList)
        }
    }
    
    private fun addTimeSlot(timeSlot: TimeSlot) {
        // Update state immediately for better UX
        val currentTimeSlots = _planningUiState.value.timeSlotsForSelectedDate.toMutableList()
        val newTimeSlotWithDetails = TimeSlotWithDetails(
            timeSlot = timeSlot,
            client = _planningUiState.value.clients.find { it.id == timeSlot.clientId } ?: Client(id=BaseId(IdGenerator.generateId())),
            animal = _planningUiState.value.animals.find { it.id == timeSlot.animalId } ?: Animal(id=BaseId(IdGenerator.generateId())),
            service = _planningUiState.value.services.find { it.id == timeSlot.serviceId } ?: Service(id=BaseId(IdGenerator.generateId()))
        )
        currentTimeSlots.add(newTimeSlotWithDetails)
        _planningUiState.value = _planningUiState.value.copy(
            timeSlotsForSelectedDate = currentTimeSlots,
            timeSlotsForSelectedWeek = currentTimeSlots,
            timeSlotsForSelectedMonth = currentTimeSlots,
            showAddTimeSlotDialog = false
        )
        
        // Launch async operation in background
        launchWithLoading {
            val newBaseId = repository.timeSlotRepository.insertTimeSlot(timeSlot)
            val updatedTimeSlot = timeSlot.copy(id = newBaseId)
            val updatedTimeSlots = _planningUiState.value.timeSlotsForSelectedDate.toMutableList()
            val index = updatedTimeSlots.indexOfFirst { it.timeSlot.id == timeSlot.id }
            if (index != -1) {
                updatedTimeSlots[index] = newTimeSlotWithDetails.copy(timeSlot = updatedTimeSlot)
                _planningUiState.value = _planningUiState.value.copy(
                    timeSlotsForSelectedDate = updatedTimeSlots,
                    timeSlotsForSelectedWeek = updatedTimeSlots,
                    timeSlotsForSelectedMonth = updatedTimeSlots
                )
            }
        }
    }
    
    private fun updateTimeSlot(timeSlot: TimeSlot) {
        // Update state immediately for better UX
        val currentTimeSlots = _planningUiState.value.timeSlotsForSelectedDate.toMutableList()
        val index = currentTimeSlots.indexOfFirst { it.timeSlot.id == timeSlot.id }
        if (index != -1) {
            currentTimeSlots[index] = currentTimeSlots[index].copy(timeSlot = timeSlot)
            _planningUiState.value = _planningUiState.value.copy(
                timeSlotsForSelectedDate = currentTimeSlots,
                timeSlotsForSelectedWeek = currentTimeSlots,
                timeSlotsForSelectedMonth = currentTimeSlots,
                showEditTimeSlotDialog = false
            )
        }
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.timeSlotRepository.updateTimeSlot(timeSlot)
            if (!success) {
                // Revert changes if update failed
                loadTimeSlots()
            }
        }
    }
    
    private fun deleteTimeSlot(BaseId: BaseId) {
        // Update state immediately for better UX
        val currentTimeSlots = _planningUiState.value.timeSlotsForSelectedDate.toMutableList()
        currentTimeSlots.removeAll { it.timeSlot.id == BaseId }
        _planningUiState.value = _planningUiState.value.copy(
            timeSlotsForSelectedDate = currentTimeSlots,
            timeSlotsForSelectedWeek = currentTimeSlots,
            timeSlotsForSelectedMonth = currentTimeSlots
        )
        
        // Launch async operation in background
        launchWithLoading {
            val success = repository.timeSlotRepository.deleteTimeSlot(BaseId)
            if (!success) {
                // Revert changes if delete failed
                loadTimeSlots()
            }
        }
    }
    
    private fun setCurrentDate(date: LocalDate) {
        _planningUiState.value = _planningUiState.value.copy(currentDate = date)
        loadTimeSlots()
    }
    
    private fun setCalendarViewType(viewType: CalendarViewType) {
        _planningUiState.value = _planningUiState.value.copy(calendarView = viewType)
        loadTimeSlots()
    }
    
    private fun navigateToPreviousPeriod() {
        val currentDate = _planningUiState.value.currentDate
        val newDate = when (_planningUiState.value.calendarView) {
            CalendarViewType.DAY -> currentDate - DatePeriod(days = 1)
            CalendarViewType.WEEK -> currentDate - DatePeriod(days = 7)
            CalendarViewType.MONTH -> currentDate - DatePeriod(months = 1)
        }
        setCurrentDate(newDate)
    }
    
    private fun navigateToNextPeriod() {
        val currentDate = _planningUiState.value.currentDate
        val newDate = when (_planningUiState.value.calendarView) {
            CalendarViewType.DAY -> currentDate + DatePeriod(days = 1)
            CalendarViewType.WEEK -> currentDate + DatePeriod(days = 7)
            CalendarViewType.MONTH -> currentDate+ DatePeriod(months = 1)
        }
        setCurrentDate(newDate)
    }
    
    private fun navigateToToday() {
        setCurrentDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }
    
    private fun updateTimeSlotStatus(timeSlotId: BaseId, status: TimeSlotStatus) {
        launchWithLoading {
            val success = repository.timeSlotRepository.updateTimeSlotStatus(timeSlotId, status)
            if (success) {
                loadTimeSlots()
            }
        }
    }
    
    private fun filterByClient(clientId: BaseId?) {
        // TODO: Implement client filtering
    }
    
    private fun filterByAnimal(animalId: BaseId?) {
        // TODO: Implement animal filtering
    }
    
    private fun filterByService(serviceId: BaseId?) {
        // TODO: Implement service filtering
    }
    
    private fun clearFilters() {
        // TODO: Implement clear filters
    }
    
    data class PlanningUiState(
        val isLoading: Boolean = false,
        val currentDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        val calendarView: CalendarViewType = CalendarViewType.DAY,
        val timeSlotsForSelectedDate: List<TimeSlotWithDetails> = emptyList(),
        val timeSlotsForSelectedWeek: List<TimeSlotWithDetails> = emptyList(),
        val timeSlotsForSelectedMonth: List<TimeSlotWithDetails> = emptyList(),
        val selectedTimeSlot: TimeSlotWithDetails? = null,
        val showAddTimeSlotDialog: Boolean = false,
        val showEditTimeSlotDialog: Boolean = false,
        val clients: List<Client> = emptyList(),
        val animals: List<Animal> = emptyList(),
        val services: List<Service> = emptyList()
    )
    
    sealed class Event {
        data class LoadTimeSlots(val dateRange: ClosedRange<LocalDate>? = null) : Event()
        data class SelectTimeSlot(val timeSlot: TimeSlotWithDetails) : Event()
        object ClearSelectedTimeSlot : Event()
        data class AddTimeSlot(val timeSlot: TimeSlot) : Event()
        data class UpdateTimeSlot(val timeSlot: TimeSlot) : Event()
        data class DeleteTimeSlot(val BaseId: BaseId) : Event()
        data class SetCurrentDate(val date: LocalDate) : Event()
        data class SetCalendarViewType(val viewType: CalendarViewType) : Event()
        object NavigateToPreviousPeriod : Event()
        object NavigateToNextPeriod : Event()
        object NavigateToToday : Event()
        data class UpdateTimeSlotStatus(val BaseId: BaseId, val status: TimeSlotStatus) : Event()
        object LoadClients : Event()
        object LoadAnimals : Event()
        object LoadServices : Event()
        data class FilterByClient(val clientId: BaseId?) : Event()
        data class FilterByAnimal(val animalId: BaseId?) : Event()
        data class FilterByService(val serviceId: BaseId?) : Event()
        object ClearFilters : Event()
    }
    
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null
    )
}

enum class CalendarViewType {
    DAY, WEEK, MONTH
} 