# Data Repository Layer - Cleopattes Manager

This package contains the repository layer for data access and business logic in the pet sitting management application.

## Architecture Overview

The repository layer follows the **Repository Pattern** and provides a clean abstraction over data sources. It's designed to work with Kotlin Multiplatform and supports both local and remote data sources.

## Repository Interfaces

### ClientRepository
Manages client data with operations for:
- CRUD operations (Create, Read, Update, Delete)
- Search functionality
- Soft delete (deactivation)
- Email validation
- Statistics

### AnimalRepository
Manages animal data with operations for:
- CRUD operations
- Client-based filtering
- Species-based filtering
- Search by name/breed
- Statistics and counts

### ServiceRepository
Manages service offerings with operations for:
- CRUD operations
- Price range filtering
- Duration filtering
- Popularity tracking
- Revenue statistics

### PrestationRepository
Manages pet sitting services with operations for:
- CRUD operations
- Status management (PLANNED, IN_PROGRESS, COMPLETED, CANCELLED)
- Date-based filtering
- Revenue tracking
- Business logic operations

### AppRepository
Main repository that combines all entity repositories and provides:
- Cross-entity operations
- Business logic
- Dashboard statistics
- Data export/import
- Global search

## Key Features

### Reactive Data Flow
All repositories support reactive data streams using Kotlin Flow:

```kotlin
// Get real-time updates
repository.getAllClientsFlow().collect { clients ->
    // UI updates automatically when data changes
}
```

### Type Safety
All operations use typed IDs for compile-time safety:

```kotlin
// Type-safe operations
val client = repository.getClientById(BaseId("client-123"))
val animals = repository.getAnimalsByClient(client.id)
```

### Business Logic
Repositories include business-specific operations:

```kotlin
// Start a prestation
repository.startPrestation(prestationId)

// Get revenue statistics
val revenue = repository.getRevenueStats(RevenuePeriod.MONTH)

// Create client with animal
val (clientId, animalId) = repository.createClientWithAnimal(client, animal)
```

### Error Handling
All operations are suspend functions that can throw exceptions:

```kotlin
try {
    val client = repository.getClientById(clientId)
    // Handle success
} catch (e: Exception) {
    // Handle error
}
```

## Data Classes

### DashboardStats
Contains aggregated statistics for the dashboard:
- Total counts for all entities
- Today's and upcoming prestations
- Monthly revenue
- Active client count

### ClientWithAnimals
Combines client data with their animals for efficient loading.

### PrestationWithDetails
Contains prestation with all related entities (client, animal, service).

### RevenueStats
Provides revenue analytics:
- Total and average revenue
- Service breakdown
- Monthly trends

### AppDataExport
Structure for data backup and restore operations.

## Usage Examples

### Basic CRUD Operations

```kotlin
// Create a new client
val client = Client(
    id = BaseId.generate(),
    firstName = "John",
    lastName = "Doe",
    email = "john@example.com",
    phone = "123456789",
    address = "123 Street",
    postalCode = "12345",
    city = "City",
    createdAt = System.currentTimeMillis()
)

val clientId = repository.insertClient(client)

// Retrieve client
val retrievedClient = repository.getClientById(clientId)

// Update client
val updatedClient = retrievedClient.copy(firstName = "Jane")
repository.updateClient(updatedClient)

// Delete client
repository.deleteClient(clientId)
```

### Business Operations

```kotlin
// Create a prestation
val prestationId = repository.createPrestation(
    clientId = clientId,
    animalId = animalId,
    serviceId = serviceId,
    startDate = System.currentTimeMillis(),
    endDate = System.currentTimeMillis() + (60 * 60 * 1000), // 1 hour
    price = 25.0,
    notes = "First visit"
)

// Start the prestation
repository.startPrestation(prestationId)

// Complete the prestation
repository.completePrestation(prestationId)
```

### Analytics and Reporting

```kotlin
// Get dashboard statistics
val stats = repository.getDashboardStats()
println("Total clients: ${stats.totalClients}")
println("Monthly revenue: ${stats.monthlyRevenue}")

// Get revenue statistics
val revenue = repository.getRevenueStats(RevenuePeriod.MONTH)
println("Total revenue: ${revenue.totalRevenue}")
println("Average per prestation: ${revenue.averageRevenue}")

// Get popular services
val popularServices = repository.serviceRepository.getMostPopularServices(5)
```

### Search and Filtering

```kotlin
// Search clients
val clients = repository.searchClients("john")

// Get animals by species
val dogs = repository.getAnimalsBySpecies(SpeciesIds.DOG)

// Get prestations by date range
val today = System.currentTimeMillis()
val tomorrow = today + (24 * 60 * 60 * 1000)
val prestations = repository.getPrestationsByDateRange(today, tomorrow)

// Global search
val results = repository.globalSearch("rex")
```

## Implementation Notes

### Platform-Specific Implementations
Repository implementations will vary by platform:
- **Android**: Room database with SQLite
- **iOS**: Core Data or SQLite
- **Desktop**: SQLite or file-based storage
- **Web**: IndexedDB or localStorage

### Dependency Injection
Repositories should be injected using a DI framework:

```kotlin
// Using Kotlin Inject
@Inject
lateinit var clientRepository: ClientRepository

@Inject
lateinit var appRepository: AppRepository
```

### Testing
Repository interfaces can be easily mocked for testing:

```kotlin
val mockRepository = mock<ClientRepository>()
whenever(mockRepository.getAllClients()).thenReturn(testClients)
```

## Future Enhancements

- **Caching**: Implement caching strategies for better performance
- **Offline Support**: Handle offline scenarios gracefully
- **Sync**: Add cloud synchronization capabilities
- **Analytics**: Enhanced reporting and analytics features
- **Notifications**: Push notifications for upcoming prestations 