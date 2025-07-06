package com.gtreb.cleopattesmanager.data.repository

import com.gtreb.cleopattesmanager.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Mock implementation of AppRepository for UI development
 */
@OptIn(ExperimentalTime::class)
class MockAppRepository : AppRepository {
    
    // Mock data
    private val mockClients = listOf(
        Client(
            id = BaseId.generate(),
            firstName = "Marie",
            lastName = "Dupont",
            email = "marie.dupont@email.com",
            phone = "0123456789",
            address = "123 Rue de la Paix",
            postalCode = "75001",
            city = "Paris",
            createdAt = Clock.System.now().toEpochMilliseconds(),
            notes = "Client fidèle",
            isActive = true
        ),
        Client(
            id = BaseId.generate(),
            firstName = "Jean",
            lastName = "Martin",
            email = "jean.martin@email.com",
            phone = "0987654321",
            address = "456 Avenue des Champs",
            postalCode = "75008",
            city = "Paris",
            createdAt = Clock.System.now().toEpochMilliseconds(),
            notes = "Nouveau client",
            isActive = true
        )
    )
    
    private val mockAnimals = listOf(
        Animal(
            id = BaseId.generate(),
            name = "Buddy",
            speciesId = "dog",
            breed = "Golden Retriever",
            clientId = mockClients[0].id,
            birthDate = Clock.System.now().toEpochMilliseconds() - (3 * 365 * 24 * 60 * 60 * 1000L),
            weight = 25.5,
            notes = "Chien très sociable"
        ),
        Animal(
            id = BaseId.generate(),
            name = "Misty",
            speciesId = "cat",
            breed = "Persan",
            clientId = mockClients[0].id,
            birthDate = Clock.System.now().toEpochMilliseconds() - (2 * 365 * 24 * 60 * 60 * 1000L),
            weight = 4.2,
            notes = "Chat calme et affectueux"
        ),
        Animal(
            id = BaseId.generate(),
            name = "Rex",
            speciesId = "dog",
            breed = "Berger Allemand",
            clientId = mockClients[1].id,
            birthDate = Clock.System.now().toEpochMilliseconds() - (4 * 365 * 24 * 60 * 60 * 1000L),
            weight = 30.0,
            notes = "Chien de garde"
        )
    )
    
    private val mockServices = listOf(
        Service(
            id = BaseId.generate(),
            name = "Promenade",
            description = "Promenade de 30 minutes",
            durationMinutes = 30,
            price = 15.0,
            isActive = true
        ),
        Service(
            id = BaseId.generate(),
            name = "Garde à domicile",
            description = "Garde de 2 heures",
            durationMinutes = 120,
            price = 25.0,
            isActive = true
        ),
        Service(
            id = BaseId.generate(),
            name = "Toilettage",
            description = "Toilettage complet",
            durationMinutes = 90,
            price = 45.0,
            isActive = true
        )
    )
    
    private val mockTimeSlots = mutableListOf<TimeSlot>()
    
    init {
        // Create some sample time slots
        val now = Clock.System.now().toEpochMilliseconds()
        val today = now - (now % (24 * 60 * 60 * 1000))
        
        // Today's appointments
        mockTimeSlots.add(
            TimeSlot(
                id = BaseId.generate(),
                clientId = mockClients[0].id,
                animalId = mockAnimals[0].id,
                serviceId = mockServices[0].id,
                startDateTime = today + (9 * 60 * 60 * 1000), // 9:00
                endDateTime = today + (9 * 60 * 60 * 1000) + (30 * 60 * 1000), // 9:30
                notes = "Promenade matinale",
                status = TimeSlotStatus.SCHEDULED
            )
        )
        
        mockTimeSlots.add(
            TimeSlot(
                id = BaseId.generate(),
                clientId = mockClients[0].id,
                animalId = mockAnimals[1].id,
                serviceId = mockServices[2].id,
                startDateTime = today + (14 * 60 * 60 * 1000), // 14:00
                endDateTime = today + (14 * 60 * 60 * 1000) + (90 * 60 * 1000), // 15:30
                notes = "Toilettage complet",
                status = TimeSlotStatus.SCHEDULED
            )
        )
        
        mockTimeSlots.add(
            TimeSlot(
                id = BaseId.generate(),
                clientId = mockClients[1].id,
                animalId = mockAnimals[2].id,
                serviceId = mockServices[1].id,
                startDateTime = today + (16 * 60 * 60 * 1000), // 16:00
                endDateTime = today + (16 * 60 * 60 * 1000) + (120 * 60 * 1000), // 18:00
                notes = "Garde à domicile",
                status = TimeSlotStatus.SCHEDULED
            )
        )
        
        // Tomorrow's appointments
        val tomorrow = today + (24 * 60 * 60 * 1000)
        mockTimeSlots.add(
            TimeSlot(
                id = BaseId.generate(),
                clientId = mockClients[0].id,
                animalId = mockAnimals[0].id,
                serviceId = mockServices[0].id,
                startDateTime = tomorrow + (10 * 60 * 60 * 1000), // 10:00
                endDateTime = tomorrow + (10 * 60 * 60 * 1000) + (30 * 60 * 1000), // 10:30
                notes = "Promenade quotidienne",
                status = TimeSlotStatus.SCHEDULED
            )
        )
    }
    
    // Repository implementations
    override val clientRepository: ClientRepository = object : ClientRepository {
        override suspend fun getAllClients(): List<Client> = mockClients
        override fun getAllClientsFlow() = MutableStateFlow(mockClients)
        override suspend fun getClientById(id: BaseId): Client? = mockClients.find { it.id == id }
        override suspend fun searchClients(query: String): List<Client> = mockClients.filter { 
            it.firstName.contains(query, ignoreCase = true) || it.lastName.contains(query, ignoreCase = true) 
        }
        override suspend fun insertClient(client: Client): BaseId = client.id
        override suspend fun updateClient(client: Client): Boolean = true
        override suspend fun deleteClient(id: BaseId): Boolean = true
        override suspend fun deactivateClient(id: BaseId): Boolean = true
        override suspend fun getClientCount(): Int = mockClients.size
        override suspend fun clientExistsByEmail(email: String): Boolean = mockClients.any { it.email == email }
    }
    
    override val animalRepository: AnimalRepository = object : AnimalRepository {
        override suspend fun getAllAnimals(): List<Animal> = mockAnimals
        override fun getAllAnimalsFlow() = MutableStateFlow(mockAnimals)
        override suspend fun getAnimalById(id: BaseId): Animal? = mockAnimals.find { it.id == id }
        override suspend fun getAnimalsByClient(clientId: BaseId): List<Animal> = mockAnimals.filter { it.clientId == clientId }
        override suspend fun getAnimalsBySpecies(speciesId: String): List<Animal> = mockAnimals.filter { it.speciesId == speciesId }
        override suspend fun searchAnimals(query: String): List<Animal> = mockAnimals.filter { 
            it.name.contains(query, ignoreCase = true) || it.breed.contains(query, ignoreCase = true) 
        }
        override suspend fun insertAnimal(animal: Animal): BaseId = animal.id
        override suspend fun updateAnimal(animal: Animal): Boolean = true
        override suspend fun deleteAnimal(id: BaseId): Boolean = true
        override suspend fun deactivateAnimal(id: BaseId): Boolean = true
        override suspend fun getAnimalCount(): Int = mockAnimals.size
        override suspend fun getAnimalCountByClient(clientId: BaseId): Int = mockAnimals.count { it.clientId == clientId }
        override suspend fun getAnimalsWithClients(): List<Pair<Animal, Client>> = mockAnimals.map { animal ->
            animal to mockClients.find { it.id == animal.clientId }!!
        }
    }
    
    override val serviceRepository: ServiceRepository = object : ServiceRepository {
        override suspend fun getAllServices(): List<Service> = mockServices
        override fun getAllServicesFlow() = MutableStateFlow(mockServices)
        override suspend fun getServiceById(id: BaseId): Service? = mockServices.find { it.id == id }
        override suspend fun getServicesByPriceRange(minPrice: Double, maxPrice: Double): List<Service> = 
            mockServices.filter { it.price in minPrice..maxPrice }
        override suspend fun getServicesByDurationRange(minMinutes: Int, maxMinutes: Int): List<Service> = 
            mockServices.filter { it.durationMinutes in minMinutes..maxMinutes }
        override suspend fun searchServices(query: String): List<Service> = mockServices.filter { 
            it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) 
        }
        override suspend fun insertService(service: Service): BaseId = service.id
        override suspend fun updateService(service: Service): Boolean = true
        override suspend fun deleteService(id: BaseId): Boolean = true
        override suspend fun deactivateService(id: BaseId): Boolean = true
        override suspend fun getServiceCount(): Int = mockServices.size
        override suspend fun getAverageServicePrice(): Double = mockServices.map { it.price }.average()
        override suspend fun getMostPopularServices(limit: Int): List<Service> = mockServices.take(limit)
    }
    
    override val prestationRepository: PrestationRepository = object : PrestationRepository {
        override suspend fun getAllPrestations(): List<Prestation> = emptyList()
        override fun getAllPrestationsFlow() = MutableStateFlow(emptyList<Prestation>())
        override suspend fun getPrestationById(id: BaseId): Prestation? = null
        override suspend fun getPrestationsByStatus(status: PrestationStatus): List<Prestation> = emptyList()
        override suspend fun getPrestationsByClient(clientId: BaseId): List<Prestation> = emptyList()
        override suspend fun getPrestationsByAnimal(animalId: BaseId): List<Prestation> = emptyList()
        override suspend fun getPrestationsByService(serviceId: BaseId): List<Prestation> = emptyList()
        override suspend fun getPrestationsByDateRange(startDate: Long, endDate: Long): List<Prestation> = emptyList()
        override suspend fun getTodayPrestations(): List<Prestation> = emptyList()
        override suspend fun getUpcomingPrestations(): List<Prestation> = emptyList()
        override suspend fun getPastPrestations(): List<Prestation> = emptyList()
        override suspend fun searchPrestations(query: String): List<Prestation> = emptyList()
        override suspend fun insertPrestation(prestation: Prestation): BaseId = prestation.id
        override suspend fun updatePrestation(prestation: Prestation): Boolean = true
        override suspend fun deletePrestation(id: BaseId): Boolean = true
        override suspend fun updatePrestationStatus(id: BaseId, status: PrestationStatus): Boolean = true
        override suspend fun startPrestation(id: BaseId): Boolean = true
        override suspend fun completePrestation(id: BaseId): Boolean = true
        override suspend fun cancelPrestation(id: BaseId): Boolean = true
        override suspend fun getPrestationCount(): Int = 0
        override suspend fun getPrestationCountByStatus(status: PrestationStatus): Int = 0
        override suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double = 0.0
        override suspend fun getMonthlyRevenue(year: Int): Map<Int, Double> = emptyMap()
        override suspend fun getPrestationsWithDetails(): List<PrestationWithDetails> = emptyList()
    }
    
    override val timeSlotRepository: TimeSlotRepository = object : TimeSlotRepository {
        override suspend fun getAllTimeSlots(): List<TimeSlot> = mockTimeSlots
        override fun getAllTimeSlotsFlow() = MutableStateFlow(mockTimeSlots)
        override suspend fun getTimeSlotById(id: BaseId): TimeSlot? = mockTimeSlots.find { it.id == id }
        override suspend fun getTimeSlotsForDate(date: Long): List<TimeSlot> = mockTimeSlots.filter { 
            val timeSlotDate = it.startDateTime / (24 * 60 * 60 * 1000)
            timeSlotDate == date / (24 * 60 * 60 * 1000)
        }
        override suspend fun getTimeSlotsForDateRange(startDate: Long, endDate: Long): List<TimeSlot> = 
            mockTimeSlots.filter { it.startDateTime in startDate..endDate }
        override suspend fun getTimeSlotsForClient(clientId: BaseId): List<TimeSlot> = 
            mockTimeSlots.filter { it.clientId == clientId }
        override suspend fun getTimeSlotsForAnimal(animalId: BaseId): List<TimeSlot> = 
            mockTimeSlots.filter { it.animalId == animalId }
        override suspend fun getTimeSlotsForService(serviceId: BaseId): List<TimeSlot> =
            mockTimeSlots.filter { it.serviceId == serviceId }
        override suspend fun getTimeSlotsByStatus(status: TimeSlotStatus): List<TimeSlot> = 
            mockTimeSlots.filter { it.status == status }
        override suspend fun getTodayTimeSlots(): List<TimeSlot> = getTimeSlotsForDate(Clock.System.now().toEpochMilliseconds())
        override suspend fun getUpcomingTimeSlots(): List<TimeSlot> = mockTimeSlots.filter { it.startDateTime > Clock.System.now().toEpochMilliseconds() }
        override suspend fun getPastTimeSlots(): List<TimeSlot> = mockTimeSlots.filter { it.startDateTime < Clock.System.now().toEpochMilliseconds() }
        override suspend fun hasTimeSlotConflict(startDateTime: Long, endDateTime: Long, excludeId: BaseId?): Boolean = false
        override suspend fun insertTimeSlot(timeSlot: TimeSlot): BaseId {
            mockTimeSlots.add(timeSlot)
            return timeSlot.id
        }
        override suspend fun updateTimeSlot(timeSlot: TimeSlot): Boolean {
            val index = mockTimeSlots.indexOfFirst { it.id == timeSlot.id }
            return if (index != -1) {
                mockTimeSlots[index] = timeSlot
                true
            } else false
        }
        override suspend fun deleteTimeSlot(id: BaseId): Boolean = mockTimeSlots.removeAll { it.id == id }
        override suspend fun updateTimeSlotStatus(id: BaseId, status: TimeSlotStatus): Boolean {
            val timeSlot = mockTimeSlots.find { it.id == id }
            return if (timeSlot != null) {
                val index = mockTimeSlots.indexOf(timeSlot)
                mockTimeSlots[index] = timeSlot.copy(status = status)
                true
            } else false
        }
        override suspend fun getTimeSlotCount(): Int = mockTimeSlots.size
        override suspend fun getTimeSlotCountByStatus(status: TimeSlotStatus): Int = 
            mockTimeSlots.count { it.status == status }
        override suspend fun getTimeSlotsWithDetails(): List<TimeSlotWithDetails> = 
            mockTimeSlots.map { timeSlot ->
                TimeSlotWithDetails(
                    timeSlot = timeSlot,
                    client = mockClients.find { it.id == timeSlot.clientId }!!,
                    animal = mockAnimals.find { it.id == timeSlot.animalId }!!,
                    service = mockServices.find { it.id == timeSlot.serviceId }!!
                )
            }
        override suspend fun getTimeSlotsWithDetailsForDateRange(startDate: Long, endDate: Long): List<TimeSlotWithDetails> = 
            getTimeSlotsWithDetails().filter { it.timeSlot.startDateTime in startDate..endDate }
    }
    
    // AppRepository methods
    override suspend fun getDashboardStats(): DashboardStats = DashboardStats(
        totalClients = mockClients.size,
        totalAnimals = mockAnimals.size,
        totalServices = mockServices.size,
        totalPrestations = 0,
        todayPrestations = 0,
        upcomingPrestations = 0,
        monthlyRevenue = 0.0,
        activeClients = mockClients.count { it.isActive }
    )
    
    override suspend fun getClientWithAnimals(clientId: BaseId): ClientWithAnimals? {
        val client = mockClients.find { it.id == clientId } ?: return null
        val animals = mockAnimals.filter { it.clientId == clientId }
        return ClientWithAnimals(client, animals)
    }
    
    override suspend fun createClientWithAnimal(client: Client, animal: Animal): Pair<BaseId, BaseId> = 
        Pair(client.id, animal.id)
    
    override suspend fun getPrestationDetails(prestationId: BaseId): PrestationWithDetails? = null
    
    override suspend fun createPrestation(clientId: BaseId, animalId: BaseId, serviceId: BaseId, startDate: Long, endDate: Long, price: Double, notes: String): BaseId =
        BaseId.generate()
    
    override suspend fun getTimeSlotDetails(timeSlotId: BaseId): TimeSlotWithDetails? {
        val timeSlot = mockTimeSlots.find { it.id == timeSlotId } ?: return null
        return TimeSlotWithDetails(
            timeSlot = timeSlot,
            client = mockClients.find { it.id == timeSlot.clientId }!!,
            animal = mockAnimals.find { it.id == timeSlot.animalId }!!,
            service = mockServices.find { it.id == timeSlot.serviceId }!!
        )
    }
    
    override suspend fun createTimeSlot(clientId: BaseId, animalId: BaseId, serviceId: BaseId, startDateTime: Long, endDateTime: Long, notes: String): BaseId {
        val timeSlot = TimeSlot(
            id = BaseId.generate(),
            clientId = clientId,
            animalId = animalId,
            serviceId = serviceId,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            notes = notes
        )
        return timeSlotRepository.insertTimeSlot(timeSlot)
    }
    
    override suspend fun getRevenueStats(period: RevenuePeriod): RevenueStats = 
        RevenueStats(0.0, 0.0, 0, emptyList(), emptyMap())
    
    override suspend fun globalSearch(query: String): GlobalSearchResults = 
        GlobalSearchResults(emptyList(), emptyList(), emptyList(), emptyList())
    
    override suspend fun exportData(): AppDataExport = 
        AppDataExport(emptyList(), emptyList(), emptyList(), emptyList(), Clock.System.now().toEpochMilliseconds(), "1.0")
    
    override suspend fun importData(data: AppDataExport): Boolean = false
    
    override suspend fun getSyncStatus(): SyncStatus = SyncStatus(null, false, 0)
} 