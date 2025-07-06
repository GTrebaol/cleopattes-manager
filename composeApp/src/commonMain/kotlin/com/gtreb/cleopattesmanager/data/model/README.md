# Data Models - Cleopattes Manager

This package contains the shared data models for the pet sitting management application.

## Entities

### Client
Represents a client who owns pets.
- **Fields**: id (BaseId), firstName, lastName, email, phone, address, postalCode, city, createdAt, notes, isActive
- **Relations**: A client can have multiple animals

### Animal
Represents a pet.
- **Fields**: id (BaseId), name, speciesId, breed, birthDate, weight, color, clientId (BaseId), notes, isActive
- **Relations**: An animal belongs to a client (clientId)

### Service
Represents a type of service offered.
- **Fields**: id (BaseId), name, description, durationMinutes, price, isActive
- **Relations**: A service can be used in multiple prestations

### Prestation
Represents a service performed or planned.
- **Fields**: id (BaseId), clientId (BaseId), animalId (BaseId), serviceId (BaseId), startDate, endDate, price, status, notes, createdAt
- **Relations**: A prestation links a client, an animal and a service

## Typed IDs

The application uses a generic typed ID system to prevent type confusion and improve type safety:

- `BaseId` - Type-safe identifier for Client entities
- `BaseId` - Type-safe identifier for Animal entities  
- `BaseId` - Type-safe identifier for Service entities
- `BaseId` - Type-safe identifier for Prestation entities

### Generic ID Architecture

All IDs are built on a single generic `BaseId<T>` class:

```kotlin
@JvmInline
value class BaseId<T>(val value: String) {
    fun isValid(): Boolean = value.matches(UUID_REGEX)
    
    companion object {
        fun <T> generate(): BaseId<T> = BaseId(IdGenerator.generateId())
        fun <T> fromString(id: String): BaseId<T> = BaseId(id)
    }
}

// Type aliases for specific entities
typealias BaseId = BaseId<Client>
typealias BaseId = BaseId<Animal>
typealias BaseId = BaseId<Service>
typealias BaseId = BaseId<Prestation>
```

### Benefits of Generic Typed IDs

1. **DRY Principle**: Single implementation for all ID types
2. **Type Safety**: Prevents accidentally passing wrong ID types
3. **Compile-time Checks**: Catches ID type mismatches at compile time
4. **Better IDE Support**: Autocomplete and refactoring work correctly
5. **Zero Runtime Overhead**: `@JvmInline value class` has no performance cost
6. **Easy Extension**: Adding new ID types requires only a typealias

## Internationalization (i18n)

The application supports multiple languages using resource files for translations.

### Species Translations

Animal species are stored as language-independent IDs and translated using resource files:

```kotlin
// Language-independent species IDs
object SpeciesIds {
    const val DOG = "dog"
    const val CAT = "cat"
    const val BIRD = "bird"
    const val RODENT = "rodent"
    const val REPTILE = "reptile"
    const val OTHER = "other"
}
```

### Resource Files

Translations are stored in XML resource files:

**Base (English):** `resources/MR/base/strings.xml`
```xml
<string name="species_dog">Dog</string>
<string name="species_cat">Cat</string>
```

**French:** `resources/MR/fr/strings.xml`
```xml
<string name="species_dog">Chien</string>
<string name="species_cat">Chat</string>
```

**Spanish:** `resources/MR/es/strings.xml`
```xml
<string name="species_dog">Perro</string>
<string name="species_cat">Gato</string>
```

### Usage

```kotlin
// Get localized species name
val speciesName = SpeciesTranslations.getSpeciesName(SpeciesIds.DOG, "fr") // "Chien"

// Animal with localized display
val animal = Animal(
    id = BaseId.generate(),
    name = "Rex",
    speciesId = SpeciesIds.DOG, // Language-independent ID
    clientId = clientId
)

// Display with localization
val displayName = animal.fullName("fr") // "Rex (Chien)"
val speciesName = animal.getLocalizedSpeciesName("es") // "Perro"
```

### Android Best Practices Compliance

Our ID implementation follows [Google's official recommendations](https://developer.android.com/identity/user-data-ids) for user data identifiers:

✅ **User-resettable**: UUIDs are reset when app data is cleared  
✅ **No hardware identifiers**: We don't use IMEI, MAC addresses, or Android ID  
✅ **App-scoped only**: IDs are limited to this application  
✅ **Privacy-friendly**: No cross-app tracking or advertising use  
✅ **Validation**: UUID format is validated at creation and usage  

### ID Validation

Each ID type includes validation methods:

```kotlin
// Generate new ID
val clientId = BaseId.generate()

// Create from string with validation
val clientId = BaseId.fromString("550e8400-e29b-41d4-a716-446655440000")

// Validate existing ID
if (clientId.isValid()) {
    // Use the ID
}
```

## Prestation Status

- `PLANNED` : Service planned but not yet started
- `IN_PROGRESS` : Service in progress
- `COMPLETED` : Service completed
- `CANCELLED` : Service cancelled

## Utilities

### IdGenerator
Unique identifier generator based on UUID, following Android best practices.

### Typed ID Generation
Each ID type has a `generate()` method for creating new instances:
```kotlin
val clientId = BaseId.generate()
val animalId = BaseId.generate()
val serviceId = BaseId.generate()
val prestationId = BaseId.generate()
```

### Extensions
- `Client.fullName()` : Returns the client's full name
- `Animal.fullName(languageCode)` : Returns the animal's name with localized species
- `Animal.getLocalizedSpeciesName(languageCode)` : Returns the localized species name

### Constants
- `SpeciesIds` : Language-independent species identifiers

## Usage

```kotlin
import com.gtreb.cleopattesmanager.data.model.*

// Create a client
val client = Client(
    id = BaseId.generate(),
    firstName = "John",
    lastName = "Doe",
    email = "john.doe@email.com",
    phone = "0123456789",
    address = "123 Peace Street",
    postalCode = "75001",
    city = "Paris",
    createdAt = System.currentTimeMillis()
)

// Create an animal
val animal = Animal(
    id = BaseId.generate(),
    name = "Rex",
    speciesId = SpeciesIds.DOG, // Language-independent ID
    breed = "Golden Retriever",
    clientId = client.id // Type-safe reference
)

// Create a service
val service = Service(
    id = BaseId.generate(),
    name = "Walk",
    description = "30-minute walk",
    durationMinutes = 30,
    price = 15.0
)

// Create a prestation
val prestation = Prestation(
    id = BaseId.generate(),
    clientId = client.id,    // Type-safe reference
    animalId = animal.id,    // Type-safe reference
    serviceId = service.id,  // Type-safe reference
    startDate = System.currentTimeMillis(),
    endDate = System.currentTimeMillis() + (30 * 60 * 1000L),
    price = 15.0,
    status = PrestationStatus.PLANNED
)

// Display with localization
println(animal.fullName("fr")) // "Rex (Chien)"
println(animal.fullName("es")) // "Rex (Perro)"
```

## Serialization

All models are serializable with Kotlinx Serialization to allow data persistence and transmission.

```kotlin
import kotlinx.serialization.json.Json

// Serialization
val json = Json.encodeToString(Client.serializer(), client)

// Deserialization
val clientDeserialized = Json.decodeFromString(Client.serializer(), json)
```

## Privacy & Security

This implementation prioritizes user privacy by:

- **No hardware identifiers**: We don't collect device-specific information
- **User control**: Users can reset all data by clearing app storage
- **App-scoped**: Data doesn't leak to other applications
- **No tracking**: No cross-app or advertising tracking
- **Local-first**: Data is primarily stored locally with optional sync

## Adding New ID Types

To add a new ID type for a new entity, simply add a typealias:

```kotlin
// In BaseId.kt
typealias NewEntityId = BaseId<NewEntity>

// In IdExtensions.kt
fun NewEntityId.Companion.generate(): NewEntityId = BaseId.generate<NewEntity>()
fun NewEntityId.Companion.fromString(id: String): NewEntityId = BaseId.fromString<NewEntity>(id)
```

## Adding New Languages

To add support for a new language:

1. Create a new resource file: `resources/MR/[language_code]/strings.xml`
2. Add translations for all species:
```xml
<string name="species_dog">[translation]</string>
<string name="species_cat">[translation]</string>
<!-- etc. -->
```
3. Update `SpeciesTranslations.kt` to include the new language code 