package com.gtreb.cleopattesmanager.ui.viewmodel

import com.gtreb.cleopattesmanager.data.model.*
import com.gtreb.cleopattesmanager.data.repository.AppRepository
import com.gtreb.cleopattesmanager.data.model.Animal
import com.gtreb.cleopattesmanager.data.model.Client
import com.gtreb.cleopattesmanager.data.model.BaseId
import com.gtreb.cleopattesmanager.data.model.Prestation
import com.gtreb.cleopattesmanager.data.model.PrestationStatus
import com.gtreb.cleopattesmanager.data.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ViewModel for dashboard management
 */
@OptIn(ExperimentalTime::class)
class DashboardViewModel(
    private val repository: AppRepository
) : BaseViewModel<DashboardViewModel.State, DashboardViewModel.Event>() {
    
    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData: StateFlow<DashboardData> = _dashboardData.asStateFlow()
    
    private val _selectedDateRange = MutableStateFlow(DateRange.THIS_MONTH)
    val selectedDateRange: StateFlow<DateRange> = _selectedDateRange.asStateFlow()
    
    private val _recentPrestations = MutableStateFlow<List<Prestation>>(emptyList())
    val recentPrestations: StateFlow<List<Prestation>> = _recentPrestations.asStateFlow()
    
    private val _upcomingPrestations = MutableStateFlow<List<Prestation>>(emptyList())
    val upcomingPrestations: StateFlow<List<Prestation>> = _upcomingPrestations.asStateFlow()
    
    private val _topClients = MutableStateFlow<List<ClientStats>>(emptyList())
    val topClients: StateFlow<List<ClientStats>> = _topClients.asStateFlow()
    
    private val _topServices = MutableStateFlow<List<ServiceStats>>(emptyList())
    val topServices: StateFlow<List<ServiceStats>> = _topServices.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    override fun createInitialState(): State = State()
    
    override fun handleEvent(event: Event) {
        when (event) {
            is Event.LoadDashboardData -> loadDashboardData()
            is Event.SetDateRange -> setDateRange(event.dateRange)
            is Event.RefreshData -> refreshData()
            is Event.ExportData -> exportData(event.dateRange)
            is Event.ShowClientDetails -> showClientDetails(event.clientId)
            is Event.ShowServiceDetails -> showServiceDetails(event.serviceId)
        }
    }
    
    private fun loadDashboardData() {
        launchWithLoading {
            // For now, we'll use placeholders since the repository methods return Flow
            // In a real implementation, this would properly combine the Flows
            val clients = emptyList<Client>() // Placeholder
            val animals = emptyList<Animal>() // Placeholder
            val services = emptyList<Service>() // Placeholder
            val prestations = emptyList<Prestation>() // Placeholder
            val dateRange = _selectedDateRange.value
            val (startDate, endDate) = getDateRangeBounds(dateRange)

            // Calculate dashboard statistics
            val stats = calculateDashboardStats(prestations, startDate, endDate)

            // Get recent and upcoming prestations
            val recent = getRecentPrestations(prestations)
            val upcoming = getUpcomingPrestations(prestations)

            // Calculate top clients and services
            val topClients = calculateTopClients(clients, prestations, startDate, endDate)
            val topServices = calculateTopServices(services, prestations, startDate, endDate)
                
            val data = DashboardData(
                statistics = stats,
                recentPrestations = recent,
                upcomingPrestations = upcoming,
                topClients = topClients,
                topServices = topServices,
                totalClients = clients.size,
                totalAnimals = animals.size,
                totalServices = services.size,
                totalPrestations = prestations.size
            )
            _dashboardData.value = data
            _recentPrestations.value = data.recentPrestations
            _upcomingPrestations.value = data.upcomingPrestations
            _topClients.value = data.topClients
            _topServices.value = data.topServices
        }
    }
    
    private fun setDateRange(dateRange: DateRange) {
        _selectedDateRange.value = dateRange
        loadDashboardData()
    }
    
    private fun refreshData() {
        loadDashboardData()
    }
    
    private fun exportData(dateRange: DateRange) {
        launchWithLoading {
            repository.exportData()
        }
    }
    
    private fun showClientDetails(clientId: BaseId) {
        // This would typically navigate to client details screen
        // For now, we just update the state
        updateState { it.copy(selectedClientId = clientId) }
    }
    
    private fun showServiceDetails(serviceId: BaseId) {
        // This would typically navigate to service details screen
        // For now, we just update the state
        updateState { it.copy(selectedServiceId = serviceId) }
    }
    
    private fun calculateDashboardStats(
        prestations: List<Prestation>,
        startDate: LocalDate,
        endDate: LocalDate
    ): DashboardStatistics {
        val filteredPrestations = prestations.filter { prestation ->
            val prestationDate = LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
            prestationDate in startDate..endDate
        }
        
        return DashboardStatistics(
            totalPrestations = filteredPrestations.size,
            completedPrestations = filteredPrestations.count { it.status == PrestationStatus.COMPLETED },
            pendingPrestations = filteredPrestations.count { it.status == PrestationStatus.PLANNED },
            cancelledPrestations = filteredPrestations.count { it.status == PrestationStatus.CANCELLED },
            totalRevenue = filteredPrestations.filter { it.status == PrestationStatus.COMPLETED }.sumOf { it.price },
            averageRevenuePerPrestation = if (filteredPrestations.isNotEmpty()) {
                filteredPrestations.filter { it.status == PrestationStatus.COMPLETED }.sumOf { it.price } / 
                filteredPrestations.count { it.status == PrestationStatus.COMPLETED }
            } else 0.0,
            completionRate = if (filteredPrestations.isNotEmpty()) {
                filteredPrestations.count { it.status == PrestationStatus.COMPLETED }.toDouble() / filteredPrestations.size
            } else 0.0
        )
    }
    
    private fun getRecentPrestations(prestations: List<Prestation>): List<Prestation> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return prestations.filter { prestation ->
            val prestationDate = LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
            prestationDate <= today
        }.sortedByDescending { it.startDate }
            .take(10)
    }
    
    private fun getUpcomingPrestations(prestations: List<Prestation>): List<Prestation> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return prestations.filter { prestation ->
            val prestationDate = LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
            prestationDate > today
        }.sortedBy { it.startDate }
            .take(10)
    }
    
    private fun calculateTopClients(
        clients: List<Client>,
        prestations: List<Prestation>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ClientStats> {
        val filteredPrestations = prestations.filter { prestation ->
            val prestationDate = LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
            prestationDate in startDate..endDate
        }
        
        return clients.map { client ->
            val clientPrestations = filteredPrestations.filter { it.clientId == client.id }
            ClientStats(
                client = client,
                prestationCount = clientPrestations.size,
                totalRevenue = clientPrestations.filter { it.status == PrestationStatus.COMPLETED }.sumOf { it.price },
                lastPrestationDate = clientPrestations.maxOfOrNull { prestation ->
                    LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
                }
            )
        }.sortedByDescending { it.totalRevenue }
            .take(5)
    }
    
    private fun calculateTopServices(
        services: List<Service>,
        prestations: List<Prestation>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ServiceStats> {
        val filteredPrestations = prestations.filter { prestation ->
            val prestationDate = LocalDate.fromEpochDays(prestation.startDate / (24 * 60 * 60 * 1000))
            prestationDate in startDate..endDate
        }
        
        return services.map { service ->
            val servicePrestations = filteredPrestations.filter { prestation ->
                prestation.serviceId == service.id
            }
            ServiceStats(
                service = service,
                usageCount = servicePrestations.size,
                totalRevenue = servicePrestations.filter { it.status == PrestationStatus.COMPLETED }
                    .sumOf { it.price }
            )
        }.sortedByDescending { it.totalRevenue }
            .take(5)
    }
    
    private fun getDateRangeBounds(dateRange: DateRange): Pair<LocalDate, LocalDate> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        return when (dateRange) {
            DateRange.TODAY -> today to today
            DateRange.THIS_WEEK -> {
                val startOfWeek = today.minus(DatePeriod(days = today.dayOfWeek.ordinal))
                startOfWeek to today
            }
            DateRange.THIS_MONTH -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                startOfMonth to today
            }
            DateRange.LAST_MONTH -> {
                val lastMonth = today.minus(DatePeriod(months = 1))
                val startOfLastMonth = LocalDate(lastMonth.year, lastMonth.month, 1)
                val endOfLastMonth = startOfLastMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
                startOfLastMonth to endOfLastMonth
            }
            DateRange.THIS_YEAR -> {
                val startOfYear = LocalDate(today.year, 1, 1)
                startOfYear to today
            }
        }
    }
    
    /**
     * UI State for DashboardViewModel
     */
    data class State(
        val dashboardData: DashboardData = DashboardData(),
        val selectedDateRange: DateRange = DateRange.THIS_MONTH,
        val selectedClientId: BaseId? = null,
        val selectedServiceId: BaseId? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    /**
     * Dashboard data container
     */
    data class DashboardData(
        val statistics: DashboardStatistics = DashboardStatistics(),
        val recentPrestations: List<Prestation> = emptyList(),
        val upcomingPrestations: List<Prestation> = emptyList(),
        val topClients: List<ClientStats> = emptyList(),
        val topServices: List<ServiceStats> = emptyList(),
        val totalClients: Int = 0,
        val totalAnimals: Int = 0,
        val totalServices: Int = 0,
        val totalPrestations: Int = 0
    )
    
    /**
     * Dashboard statistics
     */
    data class DashboardStatistics(
        val totalPrestations: Int = 0,
        val completedPrestations: Int = 0,
        val pendingPrestations: Int = 0,
        val cancelledPrestations: Int = 0,
        val totalRevenue: Double = 0.0,
        val averageRevenuePerPrestation: Double = 0.0,
        val completionRate: Double = 0.0
    )
    
    /**
     * Client statistics
     */
    data class ClientStats(
        val client: Client,
        val prestationCount: Int,
        val totalRevenue: Double,
        val lastPrestationDate: LocalDate?
    )
    
    /**
     * Service statistics
     */
    data class ServiceStats(
        val service: Service,
        val usageCount: Int,
        val totalRevenue: Double
    )
    
    /**
     * Date range options
     */
    enum class DateRange {
        TODAY, THIS_WEEK, THIS_MONTH, LAST_MONTH, THIS_YEAR
    }
    
    /**
     * UI Events for DashboardViewModel
     */
    sealed class Event {
        object LoadDashboardData : Event()
        data class SetDateRange(val dateRange: DateRange) : Event()
        object RefreshData : Event()
        data class ExportData(val dateRange: DateRange) : Event()
        data class ShowClientDetails(val clientId: BaseId) : Event()
        data class ShowServiceDetails(val serviceId: BaseId) : Event()
    }
} 