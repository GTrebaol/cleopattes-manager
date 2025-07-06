package com.gtreb.cleopattesmanager.ui.viewmodel

import com.gtreb.cleopattesmanager.data.model.*
import com.gtreb.cleopattesmanager.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Tests for ViewModels
 */
@OptIn(ExperimentalTime::class)
class ViewModelTest {
    
    @Test
    fun `BaseViewModel should handle loading and error states`() = runTest {
        val mockRepository = createMockRepository()
        val testViewModel = TestViewModel(mockRepository)
        
        // Test initial state
        assertEquals(false, testViewModel.isLoading.value)
        assertEquals(null, testViewModel.errorMessage.value)
        
        // Test loading state
        testViewModel.handleEvent(TestViewModel.Event.TestLoading)
        assertEquals(true, testViewModel.isLoading.value)
        
        // Test error state
        testViewModel.handleEvent(TestViewModel.Event.TestError)
        assertEquals("Test error", testViewModel.errorMessage.value)
    }
    
    @Test
    fun `ClientViewModel should handle client operations`() = runTest {
        val mockRepository = createMockRepository()
        val viewModel = ClientViewModel(mockRepository)
        
        val testClient = Client(
            id = generateClientId(),
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = "123456789",
            address = "123 Main St",
            postalCode = "12345",
            city = "Test City",
            createdAt = Clock.System.now().toEpochMilliseconds(),
            notes = "Test client",
            isActive = true
        )
        
        // Test add client
        viewModel.handleEvent(ClientViewModel.Event.AddClient(testClient))
        assertTrue(viewModel.clients.value.contains(testClient))
        
        // Test search clients
        viewModel.handleEvent(ClientViewModel.Event.SearchClients("John"))
        assertEquals(1, viewModel.filteredClients.value.size)
        
        // Test select client
        viewModel.handleEvent(ClientViewModel.Event.SelectClient(testClient))
        assertEquals(testClient, viewModel.selectedClient.value)
    }
    
    @Test
    fun `AnimalViewModel should handle animal operations with species localization`() = runTest {
        val mockRepository = createMockRepository()
        val viewModel = AnimalViewModel(mockRepository)
        
        val testAnimal = Animal(
            id = generateAnimalId(),
            name = "Buddy",
            speciesId = "dog",
            breed = "Golden Retriever",
            clientId = generateClientId(),
            birthDate = Clock.System.now().toEpochMilliseconds(),
            weight = 25.5,
            notes = "Friendly dog"
        )
        
        // Test add animal
        viewModel.handleEvent(AnimalViewModel.Event.AddAnimal(testAnimal))
        assertTrue(viewModel.animals.value.contains(testAnimal))
        
        // Test search animals
        viewModel.handleEvent(AnimalViewModel.Event.SearchAnimals("Buddy"))
        assertEquals(1, viewModel.filteredAnimals.value.size)
        
        // Test filter by client
        viewModel.handleEvent(AnimalViewModel.Event.FilterByClient(testAnimal.clientId))
        assertEquals(1, viewModel.filteredAnimals.value.size)
    }
    
    @Test
    fun `ServiceViewModel should handle service operations`() = runTest {
        val mockRepository = createMockRepository()
        val viewModel = ServiceViewModel(mockRepository)
        
        val testService = Service(
            id = generateServiceId(),
            name = "Dog Walking",
            description = "30-minute walk",
            durationMinutes = 30,
            price = 25.0,
            isActive = true
        )
        
        // Test add service
        viewModel.handleEvent(ServiceViewModel.Event.AddService(testService))
        assertTrue(viewModel.services.value.contains(testService))
        
        // Test toggle service active
        viewModel.handleEvent(ServiceViewModel.Event.ToggleServiceActive(testService.id))
        val updatedService = viewModel.services.value.find { it.id == testService.id }
        assertEquals(false, updatedService?.isActive)
    }
    
    @Test
    fun `PrestationViewModel should handle prestation operations`() = runTest {
        val mockRepository = createMockRepository()
        val viewModel = PrestationViewModel(mockRepository)
        
        val testPrestation = Prestation(
            id = generatePrestationId(),
            clientId = generateClientId(),
            animalId = generateAnimalId(),
            serviceId = generateServiceId(),
            startDate = Clock.System.now().toEpochMilliseconds(),
            endDate = Clock.System.now().toEpochMilliseconds() + (30 * 60 * 1000L),
            price = 25.0,
            status = PrestationStatus.PLANNED,
            notes = "Test prestation"
        )
        
        // Test add prestation
        viewModel.handleEvent(PrestationViewModel.Event.AddPrestation(testPrestation))
        assertTrue(viewModel.prestations.value.contains(testPrestation))
        
        // Test filter by status
        viewModel.handleEvent(PrestationViewModel.Event.FilterByStatus(PrestationStatus.PLANNED))
        assertEquals(1, viewModel.filteredPrestations.value.size)
        
        // Test update status
        viewModel.handleEvent(PrestationViewModel.Event.UpdatePrestationStatus(
            testPrestation.id, 
            PrestationStatus.COMPLETED
        ))
        val updatedPrestation = viewModel.prestations.value.find { it.id == testPrestation.id }
        assertEquals(PrestationStatus.COMPLETED, updatedPrestation?.status)
    }
    
    @Test
    fun `DashboardViewModel should calculate statistics correctly`() = runTest {
        val mockRepository = createMockRepository()
        val viewModel = DashboardViewModel(mockRepository)
        
        // Test dashboard data loading
        viewModel.handleEvent(DashboardViewModel.Event.LoadDashboardData)
        
        val dashboardData = viewModel.dashboardData.value
        assertNotNull(dashboardData)
        assertTrue(dashboardData.totalClients >= 0)
        assertTrue(dashboardData.totalAnimals >= 0)
        assertTrue(dashboardData.totalServices >= 0)
        assertTrue(dashboardData.totalPrestations >= 0)
        
        // Test date range selection
        viewModel.handleEvent(DashboardViewModel.Event.SetDateRange(DashboardViewModel.DateRange.THIS_MONTH))
        assertEquals(DashboardViewModel.DateRange.THIS_MONTH, viewModel.selectedDateRange.value)
    }
    
    @Test
    fun `SearchViewModel should perform global search`() = runTest {
        val mockRepository = createMockRepository()
        val viewModel = SearchViewModel(mockRepository)
        
        // Test search
        viewModel.handleEvent(SearchViewModel.Event.Search("test"))
        assertEquals("test", viewModel.searchQuery.value)
        
        // Test filters
        val filters = SearchViewModel.SearchFilters(
            includeClients = true,
            includeAnimals = false,
            includeServices = true,
            includePrestations = false
        )
        viewModel.handleEvent(SearchViewModel.Event.SetFilters(filters))
        assertEquals(filters, viewModel.selectedFilters.value)
        
        // Test clear search
        viewModel.handleEvent(SearchViewModel.Event.ClearSearch)
        assertEquals("", viewModel.searchQuery.value)
    }
    
    @Test
    fun `SettingsViewModel should handle settings operations`() = runTest {
        val mockRepository = createMockRepository()
        val viewModel = SettingsViewModel(mockRepository)
        
        // Test language update
        viewModel.handleEvent(SettingsViewModel.Event.UpdateLanguage("en"))
        assertEquals("en", viewModel.settings.value.language)
        
        // Test currency update
        viewModel.handleEvent(SettingsViewModel.Event.UpdateCurrency("USD"))
        assertEquals("USD", viewModel.settings.value.currency)
        
        // Test notifications toggle
        viewModel.handleEvent(SettingsViewModel.Event.ToggleNotifications(false))
        assertEquals(false, viewModel.settings.value.notificationsEnabled)
        
        // Test backup frequency
        viewModel.handleEvent(SettingsViewModel.Event.SetBackupFrequency(
            SettingsViewModel.BackupFrequency.DAILY
        ))
        assertEquals(SettingsViewModel.BackupFrequency.DAILY, viewModel.settings.value.backupFrequency)
    }
    
    @Test
    fun `PlanningViewModel should handle time slot operations`() = runTest {
        val mockRepository = createMockRepository()
        val viewModel = PlanningViewModel(mockRepository)
        
        val testTimeSlot = TimeSlot(
            id = generateTimeSlotId(),
            clientId = generateClientId(),
            animalId = generateAnimalId(),
            serviceId = generateServiceId(),
            startDateTime = Clock.System.now().toEpochMilliseconds(),
            endDateTime = Clock.System.now().toEpochMilliseconds() + (60 * 60 * 1000),
            notes = "Test time slot"
        )
        
        // Test add time slot
        viewModel.handleEvent(PlanningViewModel.Event.AddTimeSlot(testTimeSlot))
        assertTrue(viewModel.timeSlots.value.isNotEmpty())
        
        // Test calendar view type change
        viewModel.handleEvent(PlanningViewModel.Event.SetCalendarViewType(CalendarViewType.DAY))
        assertEquals(CalendarViewType.DAY, viewModel.calendarViewType.value)
        
        // Test navigation
        viewModel.handleEvent(PlanningViewModel.Event.NavigateToNextPeriod())
        assertNotEquals(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date, viewModel.currentDate.value)
    }
    
    private fun createMockRepository(): AppRepository {
        return object : AppRepository {
            override val clientRepository: ClientRepository = createMockClientRepository()
            override val animalRepository: AnimalRepository = createMockAnimalRepository()
            override val serviceRepository: ServiceRepository = createMockServiceRepository()
            override val prestationRepository: PrestationRepository = createMockPrestationRepository()
            override val timeSlotRepository: TimeSlotRepository = createMockTimeSlotRepository()
            
            override suspend fun getDashboardStats(): DashboardStats = DashboardStats(0, 0, 0, 0, 0, 0, 0.0, 0)
            override suspend fun getClientWithAnimals(clientId: BaseId): ClientWithAnimals? = null
            override suspend fun createClientWithAnimal(client: Client, animal: Animal): Pair<BaseId, BaseId> = 
                Pair(generateClientId(), generateAnimalId())
            override suspend fun getPrestationDetails(prestationId: BaseId): PrestationWithDetails? = null
            override suspend fun createPrestation(clientId: BaseId, animalId: BaseId, serviceId: BaseId, startDate: Long, endDate: Long, price: Double, notes: String): BaseId =
                generatePrestationId()
            override suspend fun getTimeSlotDetails(timeSlotId: TimeSlotId): TimeSlotWithDetails? = null
            override suspend fun createTimeSlot(clientId: BaseId, animalId: BaseId, serviceId: BaseId, startDateTime: Long, endDateTime: Long, notes: String): TimeSlotId =
                generateTimeSlotId()
            override suspend fun getRevenueStats(period: RevenuePeriod): RevenueStats = 
                RevenueStats(0.0, 0.0, 0, emptyList(), emptyMap())
            override suspend fun globalSearch(query: String): GlobalSearchResults = 
                GlobalSearchResults(emptyList(), emptyList(), emptyList(), emptyList())
            override suspend fun exportData(): AppDataExport = 
                AppDataExport(emptyList(), emptyList(), emptyList(), emptyList(), Clock.System.now().toEpochMilliseconds(), "1.0")
            override suspend fun importData(data: AppDataExport): Boolean = false
            override suspend fun getSyncStatus(): SyncStatus = SyncStatus(null, false, 0)
        }
    }
    
    private fun createMockClientRepository(): ClientRepository {
        return object : ClientRepository {
            private val clients = mutableListOf<Client>()
            
            override suspend fun getAllClients(): List<Client> = clients
            override fun getAllClientsFlow() = MutableStateFlow(clients)
            override suspend fun getClientById(id: BaseId): Client? = clients.find { it.id == id }
            override suspend fun searchClients(query: String): List<Client> = clients.filter { 
                it.firstName.contains(query) || it.lastName.contains(query) 
            }
            override suspend fun insertClient(client: Client): BaseId {
                clients.add(client)
                return client.id
            }
            override suspend fun updateClient(client: Client): Boolean {
                val index = clients.indexOfFirst { it.id == client.id }
                return if (index != -1) {
                    clients[index] = client
                    true
                } else false
            }
            override suspend fun deleteClient(id: BaseId): Boolean {
                return clients.removeAll { it.id == id }
            }
            override suspend fun deactivateClient(id: BaseId): Boolean = false
            override suspend fun getClientCount(): Int = clients.size
            override suspend fun clientExistsByEmail(email: String): Boolean = clients.any { it.email == email }
        }
    }
    
    private fun createMockAnimalRepository(): AnimalRepository {
        return object : AnimalRepository {
            private val animals = mutableListOf<Animal>()
            
            override suspend fun getAllAnimals(): List<Animal> = animals
            override fun getAllAnimalsFlow() = MutableStateFlow(animals)
            override suspend fun getAnimalById(id: BaseId): Animal? = animals.find { it.id == id }
            override suspend fun getAnimalsByClient(clientId: BaseId): List<Animal> = animals.filter { it.clientId == clientId }
            override suspend fun getAnimalsBySpecies(speciesId: String): List<Animal> = animals.filter { it.speciesId == speciesId }
            override suspend fun searchAnimals(query: String): List<Animal> = animals.filter { 
                it.name.contains(query) || it.breed.contains(query) 
            }
            override suspend fun insertAnimal(animal: Animal): BaseId {
                animals.add(animal)
                return animal.id
            }
            override suspend fun updateAnimal(animal: Animal): Boolean {
                val index = animals.indexOfFirst { it.id == animal.id }
                return if (index != -1) {
                    animals[index] = animal
                    true
                } else false
            }
            override suspend fun deleteAnimal(id: BaseId): Boolean {
                return animals.removeAll { it.id == id }
            }
            override suspend fun deactivateAnimal(id: BaseId): Boolean = false
            override suspend fun getAnimalCount(): Int = animals.size
            override suspend fun getAnimalCountByClient(clientId: BaseId): Int = animals.count { it.clientId == clientId }
            override suspend fun getAnimalsWithClients(): List<Pair<Animal, Client>> = emptyList()
        }
    }
    
    private fun createMockServiceRepository(): ServiceRepository {
        return object : ServiceRepository {
            private val services = mutableListOf<Service>()
            
            override suspend fun getAllServices(): List<Service> = services
            override fun getAllServicesFlow() = MutableStateFlow(services)
            override suspend fun getServiceById(id: BaseId): Service? = services.find { it.id == id }
            override suspend fun getServicesByPriceRange(minPrice: Double, maxPrice: Double): List<Service> = 
                services.filter { it.price in minPrice..maxPrice }
            override suspend fun getServicesByDurationRange(minMinutes: Int, maxMinutes: Int): List<Service> = 
                services.filter { it.durationMinutes in minMinutes..maxMinutes }
            override suspend fun searchServices(query: String): List<Service> = services.filter { 
                it.name.contains(query) || it.description.contains(query) 
            }
            override suspend fun insertService(service: Service): BaseId {
                services.add(service)
                return service.id
            }
            override suspend fun updateService(service: Service): Boolean {
                val index = services.indexOfFirst { it.id == service.id }
                return if (index != -1) {
                    services[index] = service
                    true
                } else false
            }
            override suspend fun deleteService(id: BaseId): Boolean {
                return services.removeAll { it.id == id }
            }
            override suspend fun deactivateService(id: BaseId): Boolean = false
            override suspend fun getServiceCount(): Int = services.size
            override suspend fun getAverageServicePrice(): Double = if (services.isNotEmpty()) services.map { it.price }.average() else 0.0
            override suspend fun getMostPopularServices(limit: Int): List<Service> = services.take(limit)
        }
    }
    
    private fun createMockPrestationRepository(): PrestationRepository {
        return object : PrestationRepository {
            private val prestations = mutableListOf<Prestation>()
            
            override suspend fun getAllPrestations(): List<Prestation> = prestations
            override fun getAllPrestationsFlow() = MutableStateFlow(prestations)
            override suspend fun getPrestationById(id: BaseId): Prestation? = prestations.find { it.id == id }
            override suspend fun getPrestationsByStatus(status: PrestationStatus): List<Prestation> = 
                prestations.filter { it.status == status }
            override suspend fun getPrestationsByClient(clientId: BaseId): List<Prestation> = 
                prestations.filter { it.clientId == clientId }
            override suspend fun getPrestationsByAnimal(animalId: BaseId): List<Prestation> = 
                prestations.filter { it.animalId == animalId }
            override suspend fun getPrestationsByService(serviceId: BaseId): List<Prestation> =
                prestations.filter { it.serviceId == serviceId }
            override suspend fun getPrestationsByDateRange(startDate: Long, endDate: Long): List<Prestation> = 
                prestations.filter { it.startDate in startDate..endDate }
            override suspend fun getTodayPrestations(): List<Prestation> = emptyList()
            override suspend fun getUpcomingPrestations(): List<Prestation> = emptyList()
            override suspend fun getPastPrestations(): List<Prestation> = emptyList()
            override suspend fun searchPrestations(query: String): List<Prestation> = prestations.filter { 
                it.notes.contains(query) 
            }
            override suspend fun insertPrestation(prestation: Prestation): BaseId {
                prestations.add(prestation)
                return prestation.id
            }
            override suspend fun updatePrestation(prestation: Prestation): Boolean {
                val index = prestations.indexOfFirst { it.id == prestation.id }
                return if (index != -1) {
                    prestations[index] = prestation
                    true
                } else false
            }
            override suspend fun deletePrestation(id: BaseId): Boolean {
                return prestations.removeAll { it.id == id }
            }
            override suspend fun updatePrestationStatus(id: BaseId, status: PrestationStatus): Boolean = false
            override suspend fun startPrestation(id: BaseId): Boolean = false
            override suspend fun completePrestation(id: BaseId): Boolean = false
            override suspend fun cancelPrestation(id: BaseId): Boolean = false
            override suspend fun getPrestationCount(): Int = prestations.size
            override suspend fun getPrestationCountByStatus(status: PrestationStatus): Int = 
                prestations.count { it.status == status }
            override suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double = 
                prestations.filter { it.startDate in startDate..endDate && it.status == PrestationStatus.COMPLETED }
                    .sumOf { it.price }
            override suspend fun getMonthlyRevenue(year: Int): Map<Int, Double> = emptyMap()
            override suspend fun getPrestationsWithDetails(): List<PrestationWithDetails> = emptyList()
        }
    }
    
    private fun createMockTimeSlotRepository(): TimeSlotRepository {
        return object : TimeSlotRepository {
            private val timeSlots = mutableListOf<TimeSlot>()
            
            override suspend fun getAllTimeSlots(): List<TimeSlot> = timeSlots
            override fun getAllTimeSlotsFlow() = MutableStateFlow(timeSlots)
            override suspend fun getTimeSlotById(id: TimeSlotId): TimeSlot? = timeSlots.find { it.id == id }
            override suspend fun getTimeSlotsForDate(date: Long): List<TimeSlot> = timeSlots.filter { 
                val timeSlotDate = it.startDateTime / (24 * 60 * 60 * 1000)
                timeSlotDate == date / (24 * 60 * 60 * 1000)
            }
            override suspend fun getTimeSlotsForDateRange(startDate: Long, endDate: Long): List<TimeSlot> = 
                timeSlots.filter { it.startDateTime in startDate..endDate }
            override suspend fun getTimeSlotsForClient(clientId: BaseId): List<TimeSlot> = 
                timeSlots.filter { it.clientId == clientId }
            override suspend fun getTimeSlotsForAnimal(animalId: BaseId): List<TimeSlot> = 
                timeSlots.filter { it.animalId == animalId }
            override suspend fun getTimeSlotsForService(serviceId: BaseId): List<TimeSlot> =
                timeSlots.filter { it.serviceId == serviceId }
            override suspend fun getTimeSlotsByStatus(status: TimeSlotStatus): List<TimeSlot> = 
                timeSlots.filter { it.status == status }
            override suspend fun getTodayTimeSlots(): List<TimeSlot> = emptyList()
            override suspend fun getUpcomingTimeSlots(): List<TimeSlot> = emptyList()
            override suspend fun getPastTimeSlots(): List<TimeSlot> = emptyList()
            override suspend fun hasTimeSlotConflict(startDateTime: Long, endDateTime: Long, excludeId: TimeSlotId?): Boolean = false
            override suspend fun insertTimeSlot(timeSlot: TimeSlot): TimeSlotId {
                timeSlots.add(timeSlot)
                return timeSlot.id
            }
            override suspend fun updateTimeSlot(timeSlot: TimeSlot): Boolean {
                val index = timeSlots.indexOfFirst { it.id == timeSlot.id }
                return if (index != -1) {
                    timeSlots[index] = timeSlot
                    true
                } else false
            }
            override suspend fun deleteTimeSlot(id: TimeSlotId): Boolean {
                return timeSlots.removeAll { it.id == id }
            }
            override suspend fun updateTimeSlotStatus(id: TimeSlotId, status: TimeSlotStatus): Boolean = false
            override suspend fun getTimeSlotCount(): Int = timeSlots.size
            override suspend fun getTimeSlotCountByStatus(status: TimeSlotStatus): Int = 
                timeSlots.count { it.status == status }
            override suspend fun getTimeSlotsWithDetails(): List<TimeSlotWithDetails> = emptyList()
            override suspend fun getTimeSlotsWithDetailsForDateRange(startDate: Long, endDate: Long): List<TimeSlotWithDetails> = emptyList()
        }
    }
    
    private fun generateTimeSlotId(): TimeSlotId = TimeSlotId.generate()
    
    /**
     * Test ViewModel for BaseViewModel testing
     */
    private class TestViewModel(repository: AppRepository) : BaseViewModel<TestViewModel.State, TestViewModel.Event>() {
        override fun createInitialState(): State = State()
        
        override fun handleEvent(event: Event) {
            when (event) {
                is Event.TestLoading -> setLoading(true)
                is Event.TestError -> setError("Test error")
            }
        }
        
        data class State(val test: String = "")
        
        sealed class Event {
            object TestLoading : Event()
            object TestError : Event()
        }
    }
} 